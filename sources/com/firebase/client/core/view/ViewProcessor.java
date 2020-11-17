package com.firebase.client.core.view;

import com.firebase.client.core.CompoundWrite;
import com.firebase.client.core.Path;
import com.firebase.client.core.WriteTreeRef;
import com.firebase.client.core.operation.AckUserWrite;
import com.firebase.client.core.operation.Merge;
import com.firebase.client.core.operation.Operation;
import com.firebase.client.core.operation.Overwrite;
import com.firebase.client.core.utilities.ImmutableTree;
import com.firebase.client.core.view.filter.ChildChangeAccumulator;
import com.firebase.client.core.view.filter.NodeFilter;
import com.firebase.client.core.view.filter.NodeFilter.CompleteChildSource;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.ChildrenNode;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.KeyIndex;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ViewProcessor {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static CompleteChildSource NO_COMPLETE_SOURCE = new CompleteChildSource() {
        public Node getCompleteChild(ChildKey childKey) {
            return null;
        }

        public NamedNode getChildAfterChild(Index index, NamedNode child, boolean reverse) {
            return null;
        }
    };
    private final NodeFilter filter;

    public static class ProcessorResult {
        public final List<Change> changes;
        public final ViewCache viewCache;

        public ProcessorResult(ViewCache viewCache2, List<Change> changes2) {
            this.viewCache = viewCache2;
            this.changes = changes2;
        }
    }

    private static class WriteTreeCompleteChildSource implements CompleteChildSource {
        private final Node optCompleteServerCache;
        private final ViewCache viewCache;
        private final WriteTreeRef writes;

        public WriteTreeCompleteChildSource(WriteTreeRef writes2, ViewCache viewCache2, Node optCompleteServerCache2) {
            this.writes = writes2;
            this.viewCache = viewCache2;
            this.optCompleteServerCache = optCompleteServerCache2;
        }

        public Node getCompleteChild(ChildKey childKey) {
            CacheNode serverNode;
            CacheNode node = this.viewCache.getEventCache();
            if (node.isCompleteForChild(childKey)) {
                return node.getNode().getImmediateChild(childKey);
            }
            if (this.optCompleteServerCache != null) {
                serverNode = new CacheNode(IndexedNode.from(this.optCompleteServerCache, KeyIndex.getInstance()), true, false);
            } else {
                serverNode = this.viewCache.getServerCache();
            }
            return this.writes.calcCompleteChild(childKey, serverNode);
        }

        public NamedNode getChildAfterChild(Index index, NamedNode child, boolean reverse) {
            return this.writes.calcNextNodeAfterPost(this.optCompleteServerCache != null ? this.optCompleteServerCache : this.viewCache.getCompleteServerSnap(), child, reverse, index);
        }
    }

    static {
        boolean z;
        if (!ViewProcessor.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = false;
        }
        $assertionsDisabled = z;
    }

    public ViewProcessor(NodeFilter filter2) {
        this.filter = filter2;
    }

    public ProcessorResult applyOperation(ViewCache oldViewCache, Operation operation, WriteTreeRef writesCache, Node optCompleteCache) {
        ViewCache newViewCache;
        ChildChangeAccumulator accumulator = new ChildChangeAccumulator();
        switch (operation.getType()) {
            case Overwrite:
                Overwrite overwrite = (Overwrite) operation;
                if (overwrite.getSource().isFromUser()) {
                    newViewCache = applyUserOverwrite(oldViewCache, overwrite.getPath(), overwrite.getSnapshot(), writesCache, optCompleteCache, accumulator);
                    break;
                } else if ($assertionsDisabled || overwrite.getSource().isFromServer()) {
                    newViewCache = applyServerOverwrite(oldViewCache, overwrite.getPath(), overwrite.getSnapshot(), writesCache, optCompleteCache, overwrite.getSource().isTagged() || (oldViewCache.getServerCache().isFiltered() && !overwrite.getPath().isEmpty()), accumulator);
                    break;
                } else {
                    throw new AssertionError();
                }
                break;
            case Merge:
                Merge merge = (Merge) operation;
                if (merge.getSource().isFromUser()) {
                    newViewCache = applyUserMerge(oldViewCache, merge.getPath(), merge.getChildren(), writesCache, optCompleteCache, accumulator);
                    break;
                } else if ($assertionsDisabled || merge.getSource().isFromServer()) {
                    newViewCache = applyServerMerge(oldViewCache, merge.getPath(), merge.getChildren(), writesCache, optCompleteCache, merge.getSource().isTagged() || oldViewCache.getServerCache().isFiltered(), accumulator);
                    break;
                } else {
                    throw new AssertionError();
                }
                break;
            case AckUserWrite:
                AckUserWrite ackUserWrite = (AckUserWrite) operation;
                if (ackUserWrite.isRevert()) {
                    newViewCache = revertUserWrite(oldViewCache, ackUserWrite.getPath(), writesCache, optCompleteCache, accumulator);
                    break;
                } else {
                    newViewCache = ackUserWrite(oldViewCache, ackUserWrite.getPath(), ackUserWrite.getAffectedTree(), writesCache, optCompleteCache, accumulator);
                    break;
                }
            case ListenComplete:
                newViewCache = listenComplete(oldViewCache, operation.getPath(), writesCache, optCompleteCache, accumulator);
                break;
            default:
                throw new AssertionError("Unknown operation: " + operation.getType());
        }
        ArrayList arrayList = new ArrayList(accumulator.getChanges());
        maybeAddValueEvent(oldViewCache, newViewCache, arrayList);
        return new ProcessorResult(newViewCache, arrayList);
    }

    private void maybeAddValueEvent(ViewCache oldViewCache, ViewCache newViewCache, List<Change> accumulator) {
        CacheNode eventSnap = newViewCache.getEventCache();
        if (eventSnap.isFullyInitialized()) {
            boolean isLeafOrEmpty = eventSnap.getNode().isLeafNode() || eventSnap.getNode().isEmpty();
            if (!accumulator.isEmpty() || !oldViewCache.getEventCache().isFullyInitialized() || ((isLeafOrEmpty && !eventSnap.getNode().equals(oldViewCache.getCompleteEventSnap())) || !eventSnap.getNode().getPriority().equals(oldViewCache.getCompleteEventSnap().getPriority()))) {
                accumulator.add(Change.valueChange(eventSnap.getIndexedNode()));
            }
        }
    }

    private ViewCache generateEventCacheAfterServerEvent(ViewCache viewCache, Path changePath, WriteTreeRef writesCache, CompleteChildSource source, ChildChangeAccumulator accumulator) {
        Node newEventChild;
        IndexedNode newEventCache;
        boolean z;
        Node nodeWithLocalWrites;
        CacheNode oldEventSnap = viewCache.getEventCache();
        if (writesCache.shadowingWrite(changePath) != null) {
            return viewCache;
        }
        if (!changePath.isEmpty()) {
            ChildKey childKey = changePath.getFront();
            if (!childKey.isPriorityChildName()) {
                Path childChangePath = changePath.popFront();
                if (oldEventSnap.isCompleteForChild(childKey)) {
                    Node eventChildUpdate = writesCache.calcEventCacheAfterServerOverwrite(changePath, oldEventSnap.getNode(), viewCache.getServerCache().getNode());
                    if (eventChildUpdate != null) {
                        newEventChild = oldEventSnap.getNode().getImmediateChild(childKey).updateChild(childChangePath, eventChildUpdate);
                    } else {
                        newEventChild = oldEventSnap.getNode().getImmediateChild(childKey);
                    }
                } else {
                    newEventChild = writesCache.calcCompleteChild(childKey, viewCache.getServerCache());
                }
                if (newEventChild != null) {
                    newEventCache = this.filter.updateChild(oldEventSnap.getIndexedNode(), childKey, newEventChild, childChangePath, source, accumulator);
                } else {
                    newEventCache = oldEventSnap.getIndexedNode();
                }
            } else if ($assertionsDisabled || changePath.size() == 1) {
                Node updatedPriority = writesCache.calcEventCacheAfterServerOverwrite(changePath, oldEventSnap.getNode(), viewCache.getServerCache().getNode());
                if (updatedPriority != null) {
                    newEventCache = this.filter.updatePriority(oldEventSnap.getIndexedNode(), updatedPriority);
                } else {
                    newEventCache = oldEventSnap.getIndexedNode();
                }
            } else {
                throw new AssertionError("Can't have a priority with additional path components");
            }
        } else if ($assertionsDisabled || viewCache.getServerCache().isFullyInitialized()) {
            if (viewCache.getServerCache().isFiltered()) {
                Node serverCache = viewCache.getCompleteServerSnap();
                nodeWithLocalWrites = writesCache.calcCompleteEventChildren(serverCache instanceof ChildrenNode ? serverCache : EmptyNode.Empty());
            } else {
                nodeWithLocalWrites = writesCache.calcCompleteEventCache(viewCache.getCompleteServerSnap());
            }
            newEventCache = this.filter.updateFullNode(viewCache.getEventCache().getIndexedNode(), IndexedNode.from(nodeWithLocalWrites, this.filter.getIndex()), accumulator);
        } else {
            throw new AssertionError("If change path is empty, we must have complete server data");
        }
        if (oldEventSnap.isFullyInitialized() || changePath.isEmpty()) {
            z = true;
        } else {
            z = false;
        }
        return viewCache.updateEventSnap(newEventCache, z, this.filter.filtersNodes());
    }

    private ViewCache applyServerOverwrite(ViewCache oldViewCache, Path changePath, Node changedSnap, WriteTreeRef writesCache, Node optCompleteCache, boolean filterServerNode, ChildChangeAccumulator accumulator) {
        IndexedNode newServerCache;
        CacheNode oldServerSnap = oldViewCache.getServerCache();
        NodeFilter serverFilter = filterServerNode ? this.filter : this.filter.getIndexedFilter();
        if (changePath.isEmpty()) {
            newServerCache = serverFilter.updateFullNode(oldServerSnap.getIndexedNode(), IndexedNode.from(changedSnap, serverFilter.getIndex()), null);
        } else if (!serverFilter.filtersNodes() || oldServerSnap.isFiltered()) {
            ChildKey childKey = changePath.getFront();
            if (!oldServerSnap.isCompleteForPath(changePath) && changePath.size() > 1) {
                return oldViewCache;
            }
            Path childChangePath = changePath.popFront();
            Node newChildNode = oldServerSnap.getNode().getImmediateChild(childKey).updateChild(childChangePath, changedSnap);
            if (childKey.isPriorityChildName()) {
                newServerCache = serverFilter.updatePriority(oldServerSnap.getIndexedNode(), newChildNode);
            } else {
                newServerCache = serverFilter.updateChild(oldServerSnap.getIndexedNode(), childKey, newChildNode, childChangePath, NO_COMPLETE_SOURCE, null);
            }
        } else if ($assertionsDisabled || !changePath.isEmpty()) {
            ChildKey childKey2 = changePath.getFront();
            newServerCache = serverFilter.updateFullNode(oldServerSnap.getIndexedNode(), oldServerSnap.getIndexedNode().updateChild(childKey2, oldServerSnap.getNode().getImmediateChild(childKey2).updateChild(changePath.popFront(), changedSnap)), null);
        } else {
            throw new AssertionError("An empty path should have been caught in the other branch");
        }
        ViewCache newViewCache = oldViewCache.updateServerSnap(newServerCache, oldServerSnap.isFullyInitialized() || changePath.isEmpty(), serverFilter.filtersNodes());
        return generateEventCacheAfterServerEvent(newViewCache, changePath, writesCache, new WriteTreeCompleteChildSource(writesCache, newViewCache, optCompleteCache), accumulator);
    }

    private ViewCache applyUserOverwrite(ViewCache oldViewCache, Path changePath, Node changedSnap, WriteTreeRef writesCache, Node optCompleteCache, ChildChangeAccumulator accumulator) {
        Node newChild;
        CacheNode oldEventSnap = oldViewCache.getEventCache();
        CompleteChildSource source = new WriteTreeCompleteChildSource(writesCache, oldViewCache, optCompleteCache);
        if (changePath.isEmpty()) {
            return oldViewCache.updateEventSnap(this.filter.updateFullNode(oldViewCache.getEventCache().getIndexedNode(), IndexedNode.from(changedSnap, this.filter.getIndex()), accumulator), true, this.filter.filtersNodes());
        }
        ChildKey childKey = changePath.getFront();
        if (childKey.isPriorityChildName()) {
            return oldViewCache.updateEventSnap(this.filter.updatePriority(oldViewCache.getEventCache().getIndexedNode(), changedSnap), oldEventSnap.isFullyInitialized(), oldEventSnap.isFiltered());
        }
        Path childChangePath = changePath.popFront();
        Node oldChild = oldEventSnap.getNode().getImmediateChild(childKey);
        if (childChangePath.isEmpty()) {
            newChild = changedSnap;
        } else {
            Node childNode = source.getCompleteChild(childKey);
            if (childNode == null) {
                newChild = EmptyNode.Empty();
            } else if (!childChangePath.getBack().isPriorityChildName() || !childNode.getChild(childChangePath.getParent()).isEmpty()) {
                newChild = childNode.updateChild(childChangePath, changedSnap);
            } else {
                newChild = childNode;
            }
        }
        if (oldChild.equals(newChild)) {
            return oldViewCache;
        }
        return oldViewCache.updateEventSnap(this.filter.updateChild(oldEventSnap.getIndexedNode(), childKey, newChild, childChangePath, source, accumulator), oldEventSnap.isFullyInitialized(), this.filter.filtersNodes());
    }

    private static boolean cacheHasChild(ViewCache viewCache, ChildKey childKey) {
        return viewCache.getEventCache().isCompleteForChild(childKey);
    }

    private ViewCache applyUserMerge(ViewCache viewCache, Path path, CompoundWrite changedChildren, WriteTreeRef writesCache, Node serverCache, ChildChangeAccumulator accumulator) {
        if ($assertionsDisabled || changedChildren.rootWrite() == null) {
            ViewCache currentViewCache = viewCache;
            Iterator i$ = changedChildren.iterator();
            while (i$.hasNext()) {
                Entry<Path, Node> entry = (Entry) i$.next();
                Path writePath = path.child((Path) entry.getKey());
                if (cacheHasChild(viewCache, writePath.getFront())) {
                    currentViewCache = applyUserOverwrite(currentViewCache, writePath, (Node) entry.getValue(), writesCache, serverCache, accumulator);
                }
            }
            Iterator i$2 = changedChildren.iterator();
            while (i$2.hasNext()) {
                Entry<Path, Node> entry2 = (Entry) i$2.next();
                Path writePath2 = path.child((Path) entry2.getKey());
                if (!cacheHasChild(viewCache, writePath2.getFront())) {
                    currentViewCache = applyUserOverwrite(currentViewCache, writePath2, (Node) entry2.getValue(), writesCache, serverCache, accumulator);
                }
            }
            return currentViewCache;
        }
        throw new AssertionError("Can't have a merge that is an overwrite");
    }

    private ViewCache applyServerMerge(ViewCache viewCache, Path path, CompoundWrite changedChildren, WriteTreeRef writesCache, Node serverCache, boolean filterServerNode, ChildChangeAccumulator accumulator) {
        CompoundWrite actualMerge;
        if (viewCache.getServerCache().getNode().isEmpty() && !viewCache.getServerCache().isFullyInitialized()) {
            return viewCache;
        }
        ViewCache curViewCache = viewCache;
        if ($assertionsDisabled || changedChildren.rootWrite() == null) {
            if (path.isEmpty()) {
                actualMerge = changedChildren;
            } else {
                actualMerge = CompoundWrite.emptyWrite().addWrites(path, changedChildren);
            }
            Node serverNode = viewCache.getServerCache().getNode();
            Map<ChildKey, CompoundWrite> childCompoundWrites = actualMerge.childCompoundWrites();
            for (Entry<ChildKey, CompoundWrite> childMerge : childCompoundWrites.entrySet()) {
                ChildKey childKey = (ChildKey) childMerge.getKey();
                if (serverNode.hasChild(childKey)) {
                    curViewCache = applyServerOverwrite(curViewCache, new Path(childKey), ((CompoundWrite) childMerge.getValue()).apply(serverNode.getImmediateChild(childKey)), writesCache, serverCache, filterServerNode, accumulator);
                }
            }
            for (Entry<ChildKey, CompoundWrite> childMerge2 : childCompoundWrites.entrySet()) {
                ChildKey childKey2 = (ChildKey) childMerge2.getKey();
                boolean isUnknownDeepMerge = !viewCache.getServerCache().isCompleteForChild(childKey2) && ((CompoundWrite) childMerge2.getValue()).rootWrite() == null;
                if (!serverNode.hasChild(childKey2) && !isUnknownDeepMerge) {
                    curViewCache = applyServerOverwrite(curViewCache, new Path(childKey2), ((CompoundWrite) childMerge2.getValue()).apply(serverNode.getImmediateChild(childKey2)), writesCache, serverCache, filterServerNode, accumulator);
                }
            }
            return curViewCache;
        }
        throw new AssertionError("Can't have a merge that is an overwrite");
    }

    private ViewCache ackUserWrite(ViewCache viewCache, Path ackPath, ImmutableTree<Boolean> affectedTree, WriteTreeRef writesCache, Node optCompleteCache, ChildChangeAccumulator accumulator) {
        if (writesCache.shadowingWrite(ackPath) != null) {
            return viewCache;
        }
        boolean filterServerNode = viewCache.getServerCache().isFiltered();
        CacheNode serverCache = viewCache.getServerCache();
        if (affectedTree.getValue() == null) {
            CompoundWrite changedChildren = CompoundWrite.emptyWrite();
            Iterator i$ = affectedTree.iterator();
            while (i$.hasNext()) {
                Path mergePath = (Path) ((Entry) i$.next()).getKey();
                Path serverCachePath = ackPath.child(mergePath);
                if (serverCache.isCompleteForPath(serverCachePath)) {
                    changedChildren = changedChildren.addWrite(mergePath, serverCache.getNode().getChild(serverCachePath));
                }
            }
            return applyServerMerge(viewCache, ackPath, changedChildren, writesCache, optCompleteCache, filterServerNode, accumulator);
        } else if ((ackPath.isEmpty() && serverCache.isFullyInitialized()) || serverCache.isCompleteForPath(ackPath)) {
            return applyServerOverwrite(viewCache, ackPath, serverCache.getNode().getChild(ackPath), writesCache, optCompleteCache, filterServerNode, accumulator);
        } else if (!ackPath.isEmpty()) {
            return viewCache;
        } else {
            CompoundWrite changedChildren2 = CompoundWrite.emptyWrite();
            for (NamedNode child : serverCache.getNode()) {
                changedChildren2 = changedChildren2.addWrite(child.getName(), child.getNode());
            }
            return applyServerMerge(viewCache, ackPath, changedChildren2, writesCache, optCompleteCache, filterServerNode, accumulator);
        }
    }

    public ViewCache revertUserWrite(ViewCache viewCache, Path path, WriteTreeRef writesCache, Node optCompleteServerCache, ChildChangeAccumulator accumulator) {
        IndexedNode newEventCache;
        boolean complete;
        Node newNode;
        if (writesCache.shadowingWrite(path) != null) {
            return viewCache;
        }
        WriteTreeCompleteChildSource writeTreeCompleteChildSource = new WriteTreeCompleteChildSource(writesCache, viewCache, optCompleteServerCache);
        IndexedNode oldEventCache = viewCache.getEventCache().getIndexedNode();
        if (path.isEmpty() || path.getFront().isPriorityChildName()) {
            if (viewCache.getServerCache().isFullyInitialized()) {
                newNode = writesCache.calcCompleteEventCache(viewCache.getCompleteServerSnap());
            } else {
                newNode = writesCache.calcCompleteEventChildren(viewCache.getServerCache().getNode());
            }
            newEventCache = this.filter.updateFullNode(oldEventCache, IndexedNode.from(newNode, this.filter.getIndex()), accumulator);
        } else {
            ChildKey childKey = path.getFront();
            Node newChild = writesCache.calcCompleteChild(childKey, viewCache.getServerCache());
            if (newChild == null && viewCache.getServerCache().isCompleteForChild(childKey)) {
                newChild = oldEventCache.getNode().getImmediateChild(childKey);
            }
            if (newChild != null) {
                newEventCache = this.filter.updateChild(oldEventCache, childKey, newChild, path.popFront(), writeTreeCompleteChildSource, accumulator);
            } else if (newChild != null || !viewCache.getEventCache().getNode().hasChild(childKey)) {
                newEventCache = oldEventCache;
            } else {
                newEventCache = this.filter.updateChild(oldEventCache, childKey, EmptyNode.Empty(), path.popFront(), writeTreeCompleteChildSource, accumulator);
            }
            if (newEventCache.getNode().isEmpty() && viewCache.getServerCache().isFullyInitialized()) {
                Node complete2 = writesCache.calcCompleteEventCache(viewCache.getCompleteServerSnap());
                if (complete2.isLeafNode()) {
                    newEventCache = this.filter.updateFullNode(newEventCache, IndexedNode.from(complete2, this.filter.getIndex()), accumulator);
                }
            }
        }
        if (!viewCache.getServerCache().isFullyInitialized()) {
            if (writesCache.shadowingWrite(Path.getEmptyPath()) == null) {
                complete = false;
                return viewCache.updateEventSnap(newEventCache, complete, this.filter.filtersNodes());
            }
        }
        complete = true;
        return viewCache.updateEventSnap(newEventCache, complete, this.filter.filtersNodes());
    }

    private ViewCache listenComplete(ViewCache viewCache, Path path, WriteTreeRef writesCache, Node serverCache, ChildChangeAccumulator accumulator) {
        CacheNode oldServerNode = viewCache.getServerCache();
        return generateEventCacheAfterServerEvent(viewCache.updateServerSnap(oldServerNode.getIndexedNode(), oldServerNode.isFullyInitialized() || path.isEmpty(), oldServerNode.isFiltered()), path, writesCache, NO_COMPLETE_SOURCE, accumulator);
    }
}
