package com.firebase.client.core;

import com.firebase.client.FirebaseError;
import com.firebase.client.annotations.NotNull;
import com.firebase.client.annotations.Nullable;
import com.firebase.client.collection.LLRBNode.NodeVisitor;
import com.firebase.client.core.operation.AckUserWrite;
import com.firebase.client.core.operation.ListenComplete;
import com.firebase.client.core.operation.Merge;
import com.firebase.client.core.operation.Operation;
import com.firebase.client.core.operation.OperationSource;
import com.firebase.client.core.operation.Overwrite;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.core.utilities.ImmutableTree;
import com.firebase.client.core.utilities.ImmutableTree.TreeVisitor;
import com.firebase.client.core.view.CacheNode;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.core.view.View;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.utilities.Clock;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.NodeSizeEstimator;
import com.firebase.client.utilities.Pair;
import com.firebase.client.utilities.Utilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

public class SyncTree {
    static final /* synthetic */ boolean $assertionsDisabled = (!SyncTree.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final long SIZE_THRESHOLD_FOR_COMPOUND_HASH = 1024;
    private final Set<QuerySpec> keepSyncedQueries = new HashSet();
    /* access modifiers changed from: private */
    public final ListenProvider listenProvider;
    /* access modifiers changed from: private */
    public final LogWrapper logger;
    private long nextQueryTag = 1;
    /* access modifiers changed from: private */
    public final WriteTree pendingWriteTree = new WriteTree();
    /* access modifiers changed from: private */
    public final PersistenceManager persistenceManager;
    /* access modifiers changed from: private */
    public final Map<QuerySpec, Tag> queryToTagMap = new HashMap();
    /* access modifiers changed from: private */
    public ImmutableTree<SyncPoint> syncPointTree = ImmutableTree.emptyInstance();
    /* access modifiers changed from: private */
    public final Map<Tag, QuerySpec> tagToQueryMap = new HashMap();

    public interface CompletionListener {
        List<? extends Event> onListenComplete(FirebaseError firebaseError);
    }

    private static class KeepSyncedEventRegistration extends EventRegistration {
        private QuerySpec spec;

        public KeepSyncedEventRegistration(@NotNull QuerySpec spec2) {
            this.spec = spec2;
        }

        public boolean respondsTo(EventType eventType) {
            return SyncTree.$assertionsDisabled;
        }

        public DataEvent createEvent(Change change, QuerySpec query) {
            return null;
        }

        public void fireEvent(DataEvent dataEvent) {
        }

        public void fireCancelEvent(FirebaseError error) {
        }

        public EventRegistration clone(QuerySpec newQuery) {
            return new KeepSyncedEventRegistration(newQuery);
        }

        public boolean isSameListener(EventRegistration other) {
            return other instanceof KeepSyncedEventRegistration;
        }

        @NotNull
        public QuerySpec getQuerySpec() {
            return this.spec;
        }

        public boolean equals(Object other) {
            if (!(other instanceof KeepSyncedEventRegistration) || !((KeepSyncedEventRegistration) other).spec.equals(this.spec)) {
                return SyncTree.$assertionsDisabled;
            }
            return true;
        }

        public int hashCode() {
            return this.spec.hashCode();
        }
    }

    private class ListenContainer implements SyncTreeHash, CompletionListener {
        /* access modifiers changed from: private */
        public final Tag tag;
        private final View view;

        public ListenContainer(View view2) {
            this.view = view2;
            this.tag = SyncTree.this.tagForQuery(view2.getQuery());
        }

        public CompoundHash getCompoundHash() {
            return CompoundHash.fromNode(this.view.getServerCache());
        }

        public String getSimpleHash() {
            return this.view.getServerCache().getHash();
        }

        public boolean shouldIncludeCompoundHash() {
            if (NodeSizeEstimator.estimateSerializedNodeSize(this.view.getServerCache()) > SyncTree.SIZE_THRESHOLD_FOR_COMPOUND_HASH) {
                return true;
            }
            return SyncTree.$assertionsDisabled;
        }

        public List<? extends Event> onListenComplete(FirebaseError error) {
            if (error == null) {
                QuerySpec query = this.view.getQuery();
                if (this.tag != null) {
                    return SyncTree.this.applyTaggedListenComplete(this.tag);
                }
                return SyncTree.this.applyListenComplete(query.getPath());
            }
            SyncTree.this.logger.warn("Listen at " + this.view.getQuery().getPath() + " failed: " + error.toString());
            return SyncTree.this.removeAllEventRegistrations(this.view.getQuery(), error);
        }
    }

    public interface ListenProvider {
        void startListening(QuerySpec querySpec, Tag tag, SyncTreeHash syncTreeHash, CompletionListener completionListener);

        void stopListening(QuerySpec querySpec, Tag tag);
    }

    public interface SyncTreeHash {
        CompoundHash getCompoundHash();

        String getSimpleHash();

        boolean shouldIncludeCompoundHash();
    }

    public SyncTree(Context context, PersistenceManager persistenceManager2, ListenProvider listenProvider2) {
        this.listenProvider = listenProvider2;
        this.persistenceManager = persistenceManager2;
        this.logger = context.getLogger("SyncTree");
    }

    public boolean isEmpty() {
        return this.syncPointTree.isEmpty();
    }

    public List<? extends Event> applyUserOverwrite(Path path, Node newDataUnresolved, Node newData, long writeId, boolean visible, boolean persist) {
        Utilities.hardAssert((visible || !persist) ? true : $assertionsDisabled, "We shouldn't be persisting non-visible writes.");
        final boolean z = persist;
        final Path path2 = path;
        final Node node = newDataUnresolved;
        final long j = writeId;
        final Node node2 = newData;
        final boolean z2 = visible;
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                if (z) {
                    SyncTree.this.persistenceManager.saveUserOverwrite(path2, node, j);
                }
                SyncTree.this.pendingWriteTree.addOverwrite(path2, node2, Long.valueOf(j), z2);
                if (!z2) {
                    return Collections.emptyList();
                }
                return SyncTree.this.applyOperationToSyncPoints(new Overwrite(OperationSource.USER, path2, node2));
            }
        });
    }

    public List<? extends Event> applyUserMerge(Path path, CompoundWrite unresolvedChildren, CompoundWrite children, long writeId, boolean persist) {
        final boolean z = persist;
        final Path path2 = path;
        final CompoundWrite compoundWrite = unresolvedChildren;
        final long j = writeId;
        final CompoundWrite compoundWrite2 = children;
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() throws Exception {
                if (z) {
                    SyncTree.this.persistenceManager.saveUserMerge(path2, compoundWrite, j);
                }
                SyncTree.this.pendingWriteTree.addMerge(path2, compoundWrite2, Long.valueOf(j));
                return SyncTree.this.applyOperationToSyncPoints(new Merge(OperationSource.USER, path2, compoundWrite2));
            }
        });
    }

    public List<? extends Event> ackUserWrite(long writeId, boolean revert, boolean persist, Clock serverClock) {
        final boolean z = persist;
        final long j = writeId;
        final boolean z2 = revert;
        final Clock clock = serverClock;
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                if (z) {
                    SyncTree.this.persistenceManager.removeUserWrite(j);
                }
                UserWriteRecord write = SyncTree.this.pendingWriteTree.getWrite(j);
                boolean needToReevaluate = SyncTree.this.pendingWriteTree.removeWrite(j);
                if (write.isVisible() && !z2) {
                    Map<String, Object> serverValues = ServerValues.generateServerValues(clock);
                    if (write.isOverwrite()) {
                        SyncTree.this.persistenceManager.applyUserWriteToServerCache(write.getPath(), ServerValues.resolveDeferredValueSnapshot(write.getOverwrite(), serverValues));
                    } else {
                        SyncTree.this.persistenceManager.applyUserWriteToServerCache(write.getPath(), ServerValues.resolveDeferredValueMerge(write.getMerge(), serverValues));
                    }
                }
                if (!needToReevaluate) {
                    return Collections.emptyList();
                }
                ImmutableTree<Boolean> affectedTree = ImmutableTree.emptyInstance();
                if (write.isOverwrite()) {
                    affectedTree = affectedTree.set(Path.getEmptyPath(), Boolean.valueOf(true));
                } else {
                    Iterator i$ = write.getMerge().iterator();
                    while (i$.hasNext()) {
                        affectedTree = affectedTree.set((Path) ((Entry) i$.next()).getKey(), Boolean.valueOf(true));
                    }
                }
                return SyncTree.this.applyOperationToSyncPoints(new AckUserWrite(write.getPath(), affectedTree, z2));
            }
        });
    }

    public List<? extends Event> removeAllWrites() {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() throws Exception {
                SyncTree.this.persistenceManager.removeAllUserWrites();
                if (SyncTree.this.pendingWriteTree.purgeAllWrites().isEmpty()) {
                    return Collections.emptyList();
                }
                return SyncTree.this.applyOperationToSyncPoints(new AckUserWrite(Path.getEmptyPath(), new ImmutableTree<>(Boolean.valueOf(true)), true));
            }
        });
    }

    public List<? extends Event> applyServerOverwrite(final Path path, final Node newData) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                SyncTree.this.persistenceManager.updateServerCache(QuerySpec.defaultQueryAtPath(path), newData);
                return SyncTree.this.applyOperationToSyncPoints(new Overwrite(OperationSource.SERVER, path, newData));
            }
        });
    }

    public List<? extends Event> applyServerMerge(final Path path, final Map<Path, Node> changedChildren) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                CompoundWrite merge = CompoundWrite.fromPathMerge(changedChildren);
                SyncTree.this.persistenceManager.updateServerCache(path, merge);
                return SyncTree.this.applyOperationToSyncPoints(new Merge(OperationSource.SERVER, path, merge));
            }
        });
    }

    public List<? extends Event> applyServerRangeMerges(Path path, List<RangeMerge> rangeMerges) {
        SyncPoint syncPoint = (SyncPoint) this.syncPointTree.get(path);
        if (syncPoint == null) {
            return Collections.emptyList();
        }
        View view = syncPoint.viewForQuery(QuerySpec.defaultQueryAtPath(path));
        if (view == null) {
            return Collections.emptyList();
        }
        Node serverNode = view.getServerCache();
        for (RangeMerge merge : rangeMerges) {
            serverNode = merge.applyTo(serverNode);
        }
        return applyServerOverwrite(path, serverNode);
    }

    public List<? extends Event> applyTaggedRangeMerges(Path path, List<RangeMerge> rangeMerges, Tag tag) {
        QuerySpec query = queryForTag(tag);
        if (query == null) {
            return Collections.emptyList();
        }
        if ($assertionsDisabled || path.equals(query.getPath())) {
            SyncPoint syncPoint = (SyncPoint) this.syncPointTree.get(query.getPath());
            if ($assertionsDisabled || syncPoint != null) {
                View view = syncPoint.viewForQuery(query);
                if ($assertionsDisabled || view != null) {
                    Node serverNode = view.getServerCache();
                    for (RangeMerge merge : rangeMerges) {
                        serverNode = merge.applyTo(serverNode);
                    }
                    return applyTaggedQueryOverwrite(path, serverNode, tag);
                }
                throw new AssertionError("Missing view for query tag that we're tracking");
            }
            throw new AssertionError("Missing sync point for query tag that we're tracking");
        }
        throw new AssertionError();
    }

    public List<? extends Event> applyListenComplete(final Path path) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                SyncTree.this.persistenceManager.setQueryComplete(QuerySpec.defaultQueryAtPath(path));
                return SyncTree.this.applyOperationToSyncPoints(new ListenComplete(OperationSource.SERVER, path));
            }
        });
    }

    public List<? extends Event> applyTaggedListenComplete(final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query == null) {
                    return Collections.emptyList();
                }
                SyncTree.this.persistenceManager.setQueryComplete(query);
                return SyncTree.this.applyTaggedOperation(query, new ListenComplete(OperationSource.forServerTaggedQuery(query.getParams()), Path.getEmptyPath()));
            }
        });
    }

    /* access modifiers changed from: private */
    public List<? extends Event> applyTaggedOperation(QuerySpec query, Operation operation) {
        Path queryPath = query.getPath();
        SyncPoint syncPoint = (SyncPoint) this.syncPointTree.get(queryPath);
        if ($assertionsDisabled || syncPoint != null) {
            return syncPoint.applyOperation(operation, this.pendingWriteTree.childWrites(queryPath), null);
        }
        throw new AssertionError("Missing sync point for query tag that we're tracking");
    }

    public List<? extends Event> applyTaggedQueryOverwrite(final Path path, final Node snap, final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query == null) {
                    return Collections.emptyList();
                }
                Path relativePath = Path.getRelative(query.getPath(), path);
                SyncTree.this.persistenceManager.updateServerCache(relativePath.isEmpty() ? query : QuerySpec.defaultQueryAtPath(path), snap);
                return SyncTree.this.applyTaggedOperation(query, new Overwrite(OperationSource.forServerTaggedQuery(query.getParams()), relativePath, snap));
            }
        });
    }

    public List<? extends Event> applyTaggedQueryMerge(final Path path, final Map<Path, Node> changedChildren, final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query == null) {
                    return Collections.emptyList();
                }
                Path relativePath = Path.getRelative(query.getPath(), path);
                CompoundWrite merge = CompoundWrite.fromPathMerge(changedChildren);
                SyncTree.this.persistenceManager.updateServerCache(path, merge);
                return SyncTree.this.applyTaggedOperation(query, new Merge(OperationSource.forServerTaggedQuery(query.getParams()), relativePath, merge));
            }
        });
    }

    public List<? extends Event> addEventRegistration(@NotNull final EventRegistration eventRegistration) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            static final /* synthetic */ boolean $assertionsDisabled = (!SyncTree.class.desiredAssertionStatus() ? true : SyncTree.$assertionsDisabled);

            public List<? extends Event> call() {
                CacheNode serverCache;
                QuerySpec query = eventRegistration.getQuerySpec();
                Path path = query.getPath();
                Node serverCacheNode = null;
                boolean foundAncestorDefaultView = SyncTree.$assertionsDisabled;
                ImmutableTree<SyncPoint> tree = SyncTree.this.syncPointTree;
                Path currentPath = path;
                while (!tree.isEmpty()) {
                    SyncPoint currentSyncPoint = (SyncPoint) tree.getValue();
                    if (currentSyncPoint != null) {
                        if (serverCacheNode == null) {
                            serverCacheNode = currentSyncPoint.getCompleteServerCache(currentPath);
                        }
                        foundAncestorDefaultView = (foundAncestorDefaultView || currentSyncPoint.hasCompleteView()) ? true : SyncTree.$assertionsDisabled;
                    }
                    tree = tree.getChild(currentPath.isEmpty() ? ChildKey.fromString("") : currentPath.getFront());
                    currentPath = currentPath.popFront();
                }
                SyncPoint syncPoint = (SyncPoint) SyncTree.this.syncPointTree.get(path);
                if (syncPoint == null) {
                    syncPoint = new SyncPoint(SyncTree.this.persistenceManager);
                    SyncTree.this.syncPointTree = SyncTree.this.syncPointTree.set(path, syncPoint);
                } else {
                    foundAncestorDefaultView = (foundAncestorDefaultView || syncPoint.hasCompleteView()) ? true : SyncTree.$assertionsDisabled;
                    if (serverCacheNode == null) {
                        serverCacheNode = syncPoint.getCompleteServerCache(Path.getEmptyPath());
                    }
                }
                SyncTree.this.persistenceManager.setQueryActive(query);
                if (serverCacheNode != null) {
                    serverCache = new CacheNode(IndexedNode.from(serverCacheNode, query.getIndex()), true, SyncTree.$assertionsDisabled);
                } else {
                    CacheNode persistentServerCache = SyncTree.this.persistenceManager.serverCache(query);
                    if (persistentServerCache.isFullyInitialized()) {
                        serverCache = persistentServerCache;
                    } else {
                        Node serverCacheNode2 = EmptyNode.Empty();
                        Iterator i$ = SyncTree.this.syncPointTree.subtree(path).getChildren().iterator();
                        while (i$.hasNext()) {
                            Entry<ChildKey, ImmutableTree<SyncPoint>> child = (Entry) i$.next();
                            SyncPoint childSyncPoint = (SyncPoint) ((ImmutableTree) child.getValue()).getValue();
                            if (childSyncPoint != null) {
                                Node completeCache = childSyncPoint.getCompleteServerCache(Path.getEmptyPath());
                                if (completeCache != null) {
                                    serverCacheNode2 = serverCacheNode2.updateImmediateChild((ChildKey) child.getKey(), completeCache);
                                }
                            }
                        }
                        for (NamedNode child2 : persistentServerCache.getNode()) {
                            if (!serverCacheNode2.hasChild(child2.getName())) {
                                serverCacheNode2 = serverCacheNode2.updateImmediateChild(child2.getName(), child2.getNode());
                            }
                        }
                        serverCache = new CacheNode(IndexedNode.from(serverCacheNode2, query.getIndex()), SyncTree.$assertionsDisabled, SyncTree.$assertionsDisabled);
                    }
                }
                boolean viewAlreadyExists = syncPoint.viewExistsForQuery(query);
                if (!viewAlreadyExists && !query.loadsAllData()) {
                    if ($assertionsDisabled || !SyncTree.this.queryToTagMap.containsKey(query)) {
                        Tag tag = SyncTree.this.getNextQueryTag();
                        SyncTree.this.queryToTagMap.put(query, tag);
                        SyncTree.this.tagToQueryMap.put(tag, query);
                    } else {
                        throw new AssertionError("View does not exist but we have a tag");
                    }
                }
                List<? extends Event> events = syncPoint.addEventRegistration(eventRegistration, SyncTree.this.pendingWriteTree.childWrites(path), serverCache);
                if (!viewAlreadyExists && !foundAncestorDefaultView) {
                    SyncTree.this.setupListener(query, syncPoint.viewForQuery(query));
                }
                return events;
            }
        });
    }

    public List<Event> removeEventRegistration(@NotNull EventRegistration eventRegistration) {
        return removeEventRegistration(eventRegistration.getQuerySpec(), eventRegistration, null);
    }

    public List<Event> removeAllEventRegistrations(@NotNull QuerySpec query, @NotNull FirebaseError error) {
        return removeEventRegistration(query, null, error);
    }

    private List<Event> removeEventRegistration(@NotNull final QuerySpec query, @Nullable final EventRegistration eventRegistration, @Nullable final FirebaseError cancelError) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<Event>>() {
            static final /* synthetic */ boolean $assertionsDisabled = (!SyncTree.class.desiredAssertionStatus() ? true : SyncTree.$assertionsDisabled);

            public List<Event> call() {
                Path path = query.getPath();
                SyncPoint maybeSyncPoint = (SyncPoint) SyncTree.this.syncPointTree.get(path);
                List<Event> cancelEvents = new ArrayList<>();
                if (maybeSyncPoint != null && (query.isDefault() || maybeSyncPoint.viewExistsForQuery(query))) {
                    Pair<List<QuerySpec>, List<Event>> removedAndEvents = maybeSyncPoint.removeEventRegistration(query, eventRegistration, cancelError);
                    if (maybeSyncPoint.isEmpty()) {
                        SyncTree.this.syncPointTree = SyncTree.this.syncPointTree.remove(path);
                    }
                    List<QuerySpec> removed = (List) removedAndEvents.getFirst();
                    cancelEvents = (List) removedAndEvents.getSecond();
                    boolean removingDefault = SyncTree.$assertionsDisabled;
                    for (QuerySpec queryRemoved : removed) {
                        SyncTree.this.persistenceManager.setQueryInactive(query);
                        removingDefault = (removingDefault || queryRemoved.loadsAllData()) ? true : SyncTree.$assertionsDisabled;
                    }
                    ImmutableTree<SyncPoint> currentTree = SyncTree.this.syncPointTree;
                    boolean covered = (currentTree.getValue() == null || !((SyncPoint) currentTree.getValue()).hasCompleteView()) ? SyncTree.$assertionsDisabled : true;
                    Iterator i$ = path.iterator();
                    while (i$.hasNext()) {
                        currentTree = currentTree.getChild((ChildKey) i$.next());
                        covered = (covered || (currentTree.getValue() != null && ((SyncPoint) currentTree.getValue()).hasCompleteView())) ? true : SyncTree.$assertionsDisabled;
                        if (!covered) {
                            if (currentTree.isEmpty()) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (removingDefault && !covered) {
                        ImmutableTree<SyncPoint> subtree = SyncTree.this.syncPointTree.subtree(path);
                        if (!subtree.isEmpty()) {
                            for (View view : SyncTree.this.collectDistinctViewsForSubTree(subtree)) {
                                ListenContainer container = new ListenContainer(view);
                                SyncTree.this.listenProvider.startListening(SyncTree.this.queryForListening(view.getQuery()), container.tag, container, container);
                            }
                        }
                    }
                    if (!covered && !removed.isEmpty() && cancelError == null) {
                        if (removingDefault) {
                            SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(query), null);
                        } else {
                            for (QuerySpec queryToRemove : removed) {
                                Tag tag = SyncTree.this.tagForQuery(queryToRemove);
                                if ($assertionsDisabled || tag != null) {
                                    SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(queryToRemove), tag);
                                } else {
                                    throw new AssertionError();
                                }
                            }
                        }
                    }
                    SyncTree.this.removeTags(removed);
                }
                return cancelEvents;
            }
        });
    }

    public void keepSynced(QuerySpec query, boolean keep) {
        if (keep && !this.keepSyncedQueries.contains(query)) {
            addEventRegistration(new KeepSyncedEventRegistration(query));
            this.keepSyncedQueries.add(query);
        } else if (!keep && this.keepSyncedQueries.contains(query)) {
            removeEventRegistration(new KeepSyncedEventRegistration(query));
            this.keepSyncedQueries.remove(query);
        }
    }

    /* access modifiers changed from: private */
    public List<View> collectDistinctViewsForSubTree(ImmutableTree<SyncPoint> subtree) {
        ArrayList<View> accumulator = new ArrayList<>();
        collectDistinctViewsForSubTree(subtree, accumulator);
        return accumulator;
    }

    private void collectDistinctViewsForSubTree(ImmutableTree<SyncPoint> subtree, List<View> accumulator) {
        SyncPoint maybeSyncPoint = (SyncPoint) subtree.getValue();
        if (maybeSyncPoint == null || !maybeSyncPoint.hasCompleteView()) {
            if (maybeSyncPoint != null) {
                accumulator.addAll(maybeSyncPoint.getQueryViews());
            }
            Iterator i$ = subtree.getChildren().iterator();
            while (i$.hasNext()) {
                collectDistinctViewsForSubTree((ImmutableTree) ((Entry) i$.next()).getValue(), accumulator);
            }
            return;
        }
        accumulator.add(maybeSyncPoint.getCompleteView());
    }

    /* access modifiers changed from: private */
    public void removeTags(List<QuerySpec> queries) {
        for (QuerySpec removedQuery : queries) {
            if (!removedQuery.loadsAllData()) {
                Tag tag = tagForQuery(removedQuery);
                if ($assertionsDisabled || tag != null) {
                    this.queryToTagMap.remove(removedQuery);
                    this.tagToQueryMap.remove(tag);
                } else {
                    throw new AssertionError();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public QuerySpec queryForListening(QuerySpec query) {
        if (!query.loadsAllData() || query.isDefault()) {
            return query;
        }
        return QuerySpec.defaultQueryAtPath(query.getPath());
    }

    /* access modifiers changed from: private */
    public void setupListener(QuerySpec query, View view) {
        Path path = query.getPath();
        Tag tag = tagForQuery(query);
        ListenContainer container = new ListenContainer(view);
        this.listenProvider.startListening(queryForListening(query), tag, container, container);
        ImmutableTree<SyncPoint> subtree = this.syncPointTree.subtree(path);
        if (tag == null) {
            subtree.foreach(new TreeVisitor<SyncPoint, Void>() {
                public Void onNodeValue(Path relativePath, SyncPoint maybeChildSyncPoint, Void accum) {
                    if (relativePath.isEmpty() || !maybeChildSyncPoint.hasCompleteView()) {
                        for (View syncPointView : maybeChildSyncPoint.getQueryViews()) {
                            QuerySpec childQuery = syncPointView.getQuery();
                            SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(childQuery), SyncTree.this.tagForQuery(childQuery));
                        }
                    } else {
                        QuerySpec query = maybeChildSyncPoint.getCompleteView().getQuery();
                        SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(query), SyncTree.this.tagForQuery(query));
                    }
                    return null;
                }
            });
        } else if (!$assertionsDisabled && ((SyncPoint) subtree.getValue()).hasCompleteView()) {
            throw new AssertionError("If we're adding a query, it shouldn't be shadowed");
        }
    }

    /* access modifiers changed from: private */
    public QuerySpec queryForTag(Tag tag) {
        return (QuerySpec) this.tagToQueryMap.get(tag);
    }

    /* access modifiers changed from: private */
    public Tag tagForQuery(QuerySpec query) {
        return (Tag) this.queryToTagMap.get(query);
    }

    public Node calcCompleteEventCache(Path path, List<Long> writeIdsToExclude) {
        ImmutableTree<SyncPoint> tree = this.syncPointTree;
        SyncPoint syncPoint = (SyncPoint) tree.getValue();
        Node serverCache = null;
        Path pathToFollow = path;
        Path pathSoFar = Path.getEmptyPath();
        do {
            ChildKey front = pathToFollow.getFront();
            pathToFollow = pathToFollow.popFront();
            pathSoFar = pathSoFar.child(front);
            Path relativePath = Path.getRelative(pathSoFar, path);
            tree = front != null ? tree.getChild(front) : ImmutableTree.emptyInstance();
            SyncPoint currentSyncPoint = (SyncPoint) tree.getValue();
            if (currentSyncPoint != null) {
                serverCache = currentSyncPoint.getCompleteServerCache(relativePath);
            }
            if (pathToFollow.isEmpty()) {
                break;
            }
        } while (serverCache == null);
        return this.pendingWriteTree.calcCompleteEventCache(path, serverCache, writeIdsToExclude, true);
    }

    /* access modifiers changed from: private */
    public Tag getNextQueryTag() {
        long j = this.nextQueryTag;
        this.nextQueryTag = 1 + j;
        return new Tag(j);
    }

    /* access modifiers changed from: private */
    public List<Event> applyOperationToSyncPoints(Operation operation) {
        return applyOperationHelper(operation, this.syncPointTree, null, this.pendingWriteTree.childWrites(Path.getEmptyPath()));
    }

    private List<Event> applyOperationHelper(Operation operation, ImmutableTree<SyncPoint> syncPointTree2, Node serverCache, WriteTreeRef writesCache) {
        if (operation.getPath().isEmpty()) {
            return applyOperationDescendantsHelper(operation, syncPointTree2, serverCache, writesCache);
        }
        SyncPoint syncPoint = (SyncPoint) syncPointTree2.getValue();
        if (serverCache == null && syncPoint != null) {
            serverCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
        }
        List<Event> events = new ArrayList<>();
        ChildKey childKey = operation.getPath().getFront();
        Operation childOperation = operation.operationForChild(childKey);
        ImmutableTree<SyncPoint> childTree = (ImmutableTree) syncPointTree2.getChildren().get(childKey);
        if (!(childTree == null || childOperation == null)) {
            events.addAll(applyOperationHelper(childOperation, childTree, serverCache != null ? serverCache.getImmediateChild(childKey) : null, writesCache.child(childKey)));
        }
        if (syncPoint == null) {
            return events;
        }
        events.addAll(syncPoint.applyOperation(operation, writesCache, serverCache));
        return events;
    }

    /* access modifiers changed from: private */
    public List<Event> applyOperationDescendantsHelper(Operation operation, ImmutableTree<SyncPoint> syncPointTree2, Node serverCache, WriteTreeRef writesCache) {
        final Node resolvedServerCache;
        SyncPoint syncPoint = (SyncPoint) syncPointTree2.getValue();
        if (serverCache != null || syncPoint == null) {
            resolvedServerCache = serverCache;
        } else {
            resolvedServerCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
        }
        final List<Event> events = new ArrayList<>();
        final WriteTreeRef writeTreeRef = writesCache;
        final Operation operation2 = operation;
        syncPointTree2.getChildren().inOrderTraversal(new NodeVisitor<ChildKey, ImmutableTree<SyncPoint>>() {
            public void visitEntry(ChildKey key, ImmutableTree<SyncPoint> childTree) {
                Node childServerCache = null;
                if (resolvedServerCache != null) {
                    childServerCache = resolvedServerCache.getImmediateChild(key);
                }
                WriteTreeRef childWritesCache = writeTreeRef.child(key);
                Operation childOperation = operation2.operationForChild(key);
                if (childOperation != null) {
                    events.addAll(SyncTree.this.applyOperationDescendantsHelper(childOperation, childTree, childServerCache, childWritesCache));
                }
            }
        });
        if (syncPoint != null) {
            events.addAll(syncPoint.applyOperation(operation, writesCache, resolvedServerCache));
        }
        return events;
    }

    /* access modifiers changed from: 0000 */
    public ImmutableTree<SyncPoint> getSyncPointTree() {
        return this.syncPointTree;
    }
}
