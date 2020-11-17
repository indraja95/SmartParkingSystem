package com.firebase.client.core;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseApp;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.Transaction.Handler;
import com.firebase.client.Transaction.Result;
import com.firebase.client.ValueEventListener;
import com.firebase.client.annotations.NotNull;
import com.firebase.client.authentication.AuthenticationManager;
import com.firebase.client.core.PersistentConnection.Delegate;
import com.firebase.client.core.SparseSnapshotTree.SparseSnapshotTreeVisitor;
import com.firebase.client.core.SyncTree.CompletionListener;
import com.firebase.client.core.SyncTree.ListenProvider;
import com.firebase.client.core.SyncTree.SyncTreeHash;
import com.firebase.client.core.persistence.NoopPersistenceManager;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.core.utilities.Tree;
import com.firebase.client.core.utilities.Tree.TreeFilter;
import com.firebase.client.core.utilities.Tree.TreeVisitor;
import com.firebase.client.core.view.Event;
import com.firebase.client.core.view.EventRaiser;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.utilities.DefaultClock;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.OffsetClock;
import com.firebase.client.utilities.Utilities;
import com.shaded.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Repo implements Delegate {
    static final /* synthetic */ boolean $assertionsDisabled = (!Repo.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final int TRANSACTION_MAX_RETRIES = 25;
    private static final String TRANSACTION_OVERRIDE_BY_SET = "overriddenBySet";
    private static final String TRANSACTION_TOO_MANY_RETRIES = "maxretries";
    private FirebaseApp app;
    private final AuthenticationManager authenticationManager;
    /* access modifiers changed from: private */
    public final PersistentConnection connection;
    private final Context ctx;
    private final LogWrapper dataLogger;
    public long dataUpdateCount = 0;
    private final EventRaiser eventRaiser;
    private boolean hijackHash = $assertionsDisabled;
    /* access modifiers changed from: private */
    public SnapshotHolder infoData;
    /* access modifiers changed from: private */
    public SyncTree infoSyncTree;
    private boolean loggedTransactionPersistenceWarning = $assertionsDisabled;
    private long nextWriteId = 1;
    /* access modifiers changed from: private */
    public SparseSnapshotTree onDisconnect;
    private final LogWrapper operationLogger;
    private final RepoInfo repoInfo;
    /* access modifiers changed from: private */
    public final OffsetClock serverClock = new OffsetClock(new DefaultClock(), 0);
    /* access modifiers changed from: private */
    public SyncTree serverSyncTree;
    private final LogWrapper transactionLogger;
    private long transactionOrder = 0;
    /* access modifiers changed from: private */
    public Tree<List<TransactionData>> transactionQueueTree;

    private static class FirebaseAppImpl extends FirebaseApp {
        protected FirebaseAppImpl(Repo repo) {
            super(repo);
        }
    }

    private static class TransactionData implements Comparable<TransactionData> {
        /* access modifiers changed from: private */
        public FirebaseError abortReason;
        /* access modifiers changed from: private */
        public boolean applyLocally;
        /* access modifiers changed from: private */
        public Node currentInputSnapshot;
        /* access modifiers changed from: private */
        public Node currentOutputSnapshotRaw;
        /* access modifiers changed from: private */
        public Node currentOutputSnapshotResolved;
        /* access modifiers changed from: private */
        public long currentWriteId;
        /* access modifiers changed from: private */
        public Handler handler;
        private long order;
        /* access modifiers changed from: private */
        public ValueEventListener outstandingListener;
        /* access modifiers changed from: private */
        public Path path;
        /* access modifiers changed from: private */
        public int retryCount;
        /* access modifiers changed from: private */
        public TransactionStatus status;

        private TransactionData(Path path2, Handler handler2, ValueEventListener outstandingListener2, TransactionStatus status2, boolean applyLocally2, long order2) {
            this.path = path2;
            this.handler = handler2;
            this.outstandingListener = outstandingListener2;
            this.status = status2;
            this.retryCount = 0;
            this.applyLocally = applyLocally2;
            this.order = order2;
            this.abortReason = null;
            this.currentInputSnapshot = null;
            this.currentOutputSnapshotRaw = null;
            this.currentOutputSnapshotResolved = null;
        }

        public int compareTo(TransactionData o) {
            if (this.order < o.order) {
                return -1;
            }
            if (this.order == o.order) {
                return 0;
            }
            return 1;
        }
    }

    private enum TransactionStatus {
        INITIALIZING,
        RUN,
        SENT,
        COMPLETED,
        SENT_NEEDS_ABORT,
        NEEDS_ABORT
    }

    Repo(RepoInfo repoInfo2, Context ctx2) {
        this.repoInfo = repoInfo2;
        this.ctx = ctx2;
        this.app = new FirebaseAppImpl(this);
        this.operationLogger = this.ctx.getLogger("RepoOperation");
        this.transactionLogger = this.ctx.getLogger("Transaction");
        this.dataLogger = this.ctx.getLogger("DataOperation");
        this.eventRaiser = new EventRaiser(this.ctx);
        this.connection = new PersistentConnection(ctx2, repoInfo2, this);
        this.authenticationManager = new AuthenticationManager(ctx2, this, repoInfo2, this.connection);
        this.authenticationManager.resumeSession();
        scheduleNow(new Runnable() {
            public void run() {
                Repo.this.deferredInitialization();
            }
        });
    }

    /* access modifiers changed from: private */
    public void deferredInitialization() {
        boolean authenticated;
        this.connection.establishConnection();
        PersistenceManager persistenceManager = this.ctx.getPersistenceManager(this.repoInfo.host);
        this.infoData = new SnapshotHolder();
        this.onDisconnect = new SparseSnapshotTree();
        this.transactionQueueTree = new Tree<>();
        this.infoSyncTree = new SyncTree(this.ctx, new NoopPersistenceManager(), new ListenProvider() {
            public void startListening(final QuerySpec query, Tag tag, SyncTreeHash hash, final CompletionListener onComplete) {
                Repo.this.scheduleNow(new Runnable() {
                    public void run() {
                        Node node = Repo.this.infoData.getNode(query.getPath());
                        if (!node.isEmpty()) {
                            Repo.this.postEvents(Repo.this.infoSyncTree.applyServerOverwrite(query.getPath(), node));
                            onComplete.onListenComplete(null);
                        }
                    }
                });
            }

            public void stopListening(QuerySpec query, Tag tag) {
            }
        });
        this.serverSyncTree = new SyncTree(this.ctx, persistenceManager, new ListenProvider() {
            public void startListening(QuerySpec query, Tag tag, SyncTreeHash hash, final CompletionListener onListenComplete) {
                Repo.this.connection.listen(query, hash, tag, new RequestResultListener() {
                    public void onRequestResult(FirebaseError error) {
                        Repo.this.postEvents(onListenComplete.onListenComplete(error));
                    }
                });
            }

            public void stopListening(QuerySpec query, Tag tag) {
                Repo.this.connection.unlisten(query);
            }
        });
        restoreWrites(persistenceManager);
        if (this.authenticationManager.getAuth() != null) {
            authenticated = true;
        } else {
            authenticated = false;
        }
        updateInfo(Constants.DOT_INFO_AUTHENTICATED, Boolean.valueOf(authenticated));
        updateInfo(Constants.DOT_INFO_CONNECTED, Boolean.valueOf($assertionsDisabled));
    }

    private void restoreWrites(PersistenceManager persistenceManager) {
        List<UserWriteRecord> writes = persistenceManager.loadUserWrites();
        Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
        long lastWriteId = Long.MIN_VALUE;
        for (final UserWriteRecord write : writes) {
            Firebase.CompletionListener onComplete = new Firebase.CompletionListener() {
                public void onComplete(FirebaseError error, Firebase ref) {
                    Repo.this.warnIfWriteFailed("Persisted write", write.getPath(), error);
                    Repo.this.ackWriteAndRerunTransactions(write.getWriteId(), write.getPath(), error);
                }
            };
            if (lastWriteId >= write.getWriteId()) {
                throw new IllegalStateException("Write ids were not in order.");
            }
            lastWriteId = write.getWriteId();
            this.nextWriteId = write.getWriteId() + 1;
            if (write.isOverwrite()) {
                if (this.operationLogger.logsDebug()) {
                    this.operationLogger.debug("Restoring overwrite with id " + write.getWriteId());
                }
                this.connection.put(write.getPath().toString(), write.getOverwrite().getValue(true), null, onComplete);
                this.serverSyncTree.applyUserOverwrite(write.getPath(), write.getOverwrite(), ServerValues.resolveDeferredValueSnapshot(write.getOverwrite(), serverValues), write.getWriteId(), true, $assertionsDisabled);
            } else {
                if (this.operationLogger.logsDebug()) {
                    this.operationLogger.debug("Restoring merge with id " + write.getWriteId());
                }
                this.connection.merge(write.getPath().toString(), write.getMerge().getValue(true), onComplete);
                this.serverSyncTree.applyUserMerge(write.getPath(), write.getMerge(), ServerValues.resolveDeferredValueMerge(write.getMerge(), serverValues), write.getWriteId(), $assertionsDisabled);
            }
        }
    }

    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    public FirebaseApp getFirebaseApp() {
        return this.app;
    }

    public String toString() {
        return this.repoInfo.toString();
    }

    public void scheduleNow(Runnable r) {
        this.ctx.requireStarted();
        this.ctx.getRunLoop().scheduleNow(r);
    }

    public void postEvent(Runnable r) {
        this.ctx.requireStarted();
        this.ctx.getEventTarget().postEvent(r);
    }

    /* access modifiers changed from: private */
    public void postEvents(List<? extends Event> events) {
        if (!events.isEmpty()) {
            this.eventRaiser.raiseEvents(events);
        }
    }

    public long getServerTime() {
        return this.serverClock.millis();
    }

    /* access modifiers changed from: 0000 */
    public boolean hasListeners() {
        if (!this.infoSyncTree.isEmpty() || !this.serverSyncTree.isEmpty()) {
            return true;
        }
        return $assertionsDisabled;
    }

    public void onDataUpdate(String pathString, Object message, boolean isMerge, Tag tag) {
        List<? extends Event> events;
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("onDataUpdate: " + pathString);
        }
        if (this.dataLogger.logsDebug()) {
            this.operationLogger.debug("onDataUpdate: " + pathString + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + message);
        }
        this.dataUpdateCount++;
        Path path = new Path(pathString);
        if (tag != null) {
            if (isMerge) {
                try {
                    Map<Path, Node> taggedChildren = new HashMap<>();
                    for (Entry<String, Object> entry : ((Map) message).entrySet()) {
                        taggedChildren.put(new Path((String) entry.getKey()), NodeUtilities.NodeFromJSON(entry.getValue()));
                    }
                    events = this.serverSyncTree.applyTaggedQueryMerge(path, taggedChildren, tag);
                } catch (FirebaseException e) {
                    this.operationLogger.error("FIREBASE INTERNAL ERROR", e);
                    return;
                }
            } else {
                events = this.serverSyncTree.applyTaggedQueryOverwrite(path, NodeUtilities.NodeFromJSON(message), tag);
            }
        } else if (isMerge) {
            Map<Path, Node> changedChildren = new HashMap<>();
            for (Entry<String, Object> entry2 : ((Map) message).entrySet()) {
                changedChildren.put(new Path((String) entry2.getKey()), NodeUtilities.NodeFromJSON(entry2.getValue()));
            }
            events = this.serverSyncTree.applyServerMerge(path, changedChildren);
        } else {
            events = this.serverSyncTree.applyServerOverwrite(path, NodeUtilities.NodeFromJSON(message));
        }
        if (events.size() > 0) {
            rerunTransactions(path);
        }
        postEvents(events);
    }

    public void onRangeMergeUpdate(Path path, List<RangeMerge> merges, Tag tag) {
        List<? extends Event> events;
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("onRangeMergeUpdate: " + path);
        }
        if (this.dataLogger.logsDebug()) {
            this.operationLogger.debug("onRangeMergeUpdate: " + path + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + merges);
        }
        this.dataUpdateCount++;
        if (tag != null) {
            events = this.serverSyncTree.applyTaggedRangeMerges(path, merges, tag);
        } else {
            events = this.serverSyncTree.applyServerRangeMerges(path, merges);
        }
        if (events.size() > 0) {
            rerunTransactions(path);
        }
        postEvents(events);
    }

    /* access modifiers changed from: 0000 */
    public void callOnComplete(final Firebase.CompletionListener onComplete, final FirebaseError error, Path path) {
        final Firebase ref;
        if (onComplete != null) {
            ChildKey last = path.getBack();
            if (last == null || !last.isPriorityChildName()) {
                ref = new Firebase(this, path);
            } else {
                ref = new Firebase(this, path.getParent());
            }
            postEvent(new Runnable() {
                public void run() {
                    onComplete.onComplete(error, ref);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void ackWriteAndRerunTransactions(long writeId, Path path, FirebaseError error) {
        boolean success;
        boolean z;
        if (error == null || error.getCode() != -25) {
            if (error == null) {
                success = true;
            } else {
                success = false;
            }
            SyncTree syncTree = this.serverSyncTree;
            if (!success) {
                z = true;
            } else {
                z = false;
            }
            List<? extends Event> clearEvents = syncTree.ackUserWrite(writeId, z, true, this.serverClock);
            if (clearEvents.size() > 0) {
                rerunTransactions(path);
            }
            postEvents(clearEvents);
        }
    }

    public void setValue(Path path, Node newValueUnresolved, Firebase.CompletionListener onComplete) {
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("set: " + path);
        }
        if (this.dataLogger.logsDebug()) {
            this.dataLogger.debug("set: " + path + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + newValueUnresolved);
        }
        Node newValue = ServerValues.resolveDeferredValueSnapshot(newValueUnresolved, ServerValues.generateServerValues(this.serverClock));
        long writeId = getNextWriteId();
        postEvents(this.serverSyncTree.applyUserOverwrite(path, newValueUnresolved, newValue, writeId, true, true));
        final Path path2 = path;
        final long j = writeId;
        final Firebase.CompletionListener completionListener = onComplete;
        this.connection.put(path.toString(), newValueUnresolved.getValue(true), new Firebase.CompletionListener() {
            public void onComplete(FirebaseError error, Firebase ref) {
                Repo.this.warnIfWriteFailed("setValue", path2, error);
                Repo.this.ackWriteAndRerunTransactions(j, path2, error);
                Repo.this.callOnComplete(completionListener, error, path2);
            }
        });
        rerunTransactions(abortTransactions(path, -9));
    }

    public void updateChildren(Path path, CompoundWrite updates, Firebase.CompletionListener onComplete, Map<String, Object> unParsedUpdates) {
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("update: " + path);
        }
        if (this.dataLogger.logsDebug()) {
            this.dataLogger.debug("update: " + path + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + unParsedUpdates);
        }
        if (updates.isEmpty()) {
            if (this.operationLogger.logsDebug()) {
                this.operationLogger.debug("update called with no changes. No-op");
            }
            callOnComplete(onComplete, null, path);
            return;
        }
        CompoundWrite resolved = ServerValues.resolveDeferredValueMerge(updates, ServerValues.generateServerValues(this.serverClock));
        long writeId = getNextWriteId();
        postEvents(this.serverSyncTree.applyUserMerge(path, updates, resolved, writeId, true));
        final Path path2 = path;
        final long j = writeId;
        final Firebase.CompletionListener completionListener = onComplete;
        this.connection.merge(path.toString(), unParsedUpdates, new Firebase.CompletionListener() {
            public void onComplete(FirebaseError error, Firebase ref) {
                Repo.this.warnIfWriteFailed("updateChildren", path2, error);
                Repo.this.ackWriteAndRerunTransactions(j, path2, error);
                Repo.this.callOnComplete(completionListener, error, path2);
            }
        });
        rerunTransactions(abortTransactions(path, -9));
    }

    public void purgeOutstandingWrites() {
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("Purging writes");
        }
        postEvents(this.serverSyncTree.removeAllWrites());
        abortTransactions(Path.getEmptyPath(), -25);
        this.connection.purgeOutstandingWrites();
    }

    public void removeEventCallback(@NotNull EventRegistration eventRegistration) {
        List<Event> events;
        if (Constants.DOT_INFO.equals(eventRegistration.getQuerySpec().getPath().getFront())) {
            events = this.infoSyncTree.removeEventRegistration(eventRegistration);
        } else {
            events = this.serverSyncTree.removeEventRegistration(eventRegistration);
        }
        postEvents(events);
    }

    public void onDisconnectSetValue(final Path path, final Node newValue, final Firebase.CompletionListener onComplete) {
        this.connection.onDisconnectPut(path, newValue.getValue(true), new Firebase.CompletionListener() {
            public void onComplete(FirebaseError error, Firebase ref) {
                Repo.this.warnIfWriteFailed("onDisconnect().setValue", path, error);
                if (error == null) {
                    Repo.this.onDisconnect.remember(path, newValue);
                }
                Repo.this.callOnComplete(onComplete, error, path);
            }
        });
    }

    public void onDisconnectUpdate(final Path path, final Map<Path, Node> newChildren, final Firebase.CompletionListener listener, Map<String, Object> unParsedUpdates) {
        this.connection.onDisconnectMerge(path, unParsedUpdates, new Firebase.CompletionListener() {
            public void onComplete(FirebaseError error, Firebase ref) {
                Repo.this.warnIfWriteFailed("onDisconnect().updateChildren", path, error);
                if (error == null) {
                    for (Entry<Path, Node> entry : newChildren.entrySet()) {
                        Repo.this.onDisconnect.remember(path.child((Path) entry.getKey()), (Node) entry.getValue());
                    }
                }
                Repo.this.callOnComplete(listener, error, path);
            }
        });
    }

    public void onDisconnectCancel(final Path path, final Firebase.CompletionListener onComplete) {
        this.connection.onDisconnectCancel(path, new Firebase.CompletionListener() {
            public void onComplete(FirebaseError error, Firebase ref) {
                if (error == null) {
                    Repo.this.onDisconnect.forget(path);
                }
                Repo.this.callOnComplete(onComplete, error, path);
            }
        });
    }

    public void onConnect() {
        onServerInfoUpdate(Constants.DOT_INFO_CONNECTED, Boolean.valueOf(true));
    }

    public void onDisconnect() {
        onServerInfoUpdate(Constants.DOT_INFO_CONNECTED, Boolean.valueOf($assertionsDisabled));
        runOnDisconnectEvents();
    }

    public void onAuthStatus(boolean authOk) {
        onServerInfoUpdate(Constants.DOT_INFO_AUTHENTICATED, Boolean.valueOf(authOk));
    }

    public void onServerInfoUpdate(ChildKey key, Object value) {
        updateInfo(key, value);
    }

    public void onServerInfoUpdate(Map<ChildKey, Object> updates) {
        for (Entry<ChildKey, Object> entry : updates.entrySet()) {
            updateInfo((ChildKey) entry.getKey(), entry.getValue());
        }
    }

    /* access modifiers changed from: 0000 */
    public void interrupt() {
        this.connection.interrupt();
    }

    /* access modifiers changed from: 0000 */
    public void resume() {
        this.connection.resume();
    }

    public void addEventCallback(@NotNull EventRegistration eventRegistration) {
        List<? extends Event> events;
        ChildKey front = eventRegistration.getQuerySpec().getPath().getFront();
        if (front == null || !front.equals(Constants.DOT_INFO)) {
            events = this.serverSyncTree.addEventRegistration(eventRegistration);
        } else {
            events = this.infoSyncTree.addEventRegistration(eventRegistration);
        }
        postEvents(events);
    }

    public void keepSynced(QuerySpec query, boolean keep) {
        if ($assertionsDisabled || query.getPath().isEmpty() || !query.getPath().getFront().equals(Constants.DOT_INFO)) {
            this.serverSyncTree.keepSynced(query, keep);
            return;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: 0000 */
    public PersistentConnection getConnection() {
        return this.connection;
    }

    private void updateInfo(ChildKey childKey, Object value) {
        if (childKey.equals(Constants.DOT_INFO_SERVERTIME_OFFSET)) {
            this.serverClock.setOffset(((Long) value).longValue());
        }
        Path path = new Path(Constants.DOT_INFO, childKey);
        try {
            Node node = NodeUtilities.NodeFromJSON(value);
            this.infoData.update(path, node);
            postEvents(this.infoSyncTree.applyServerOverwrite(path, node));
        } catch (FirebaseException e) {
            this.operationLogger.error("Failed to parse info update", e);
        }
    }

    private long getNextWriteId() {
        long j = this.nextWriteId;
        this.nextWriteId = 1 + j;
        return j;
    }

    private void runOnDisconnectEvents() {
        SparseSnapshotTree resolvedTree = ServerValues.resolveDeferredValueTree(this.onDisconnect, ServerValues.generateServerValues(this.serverClock));
        final List<Event> events = new ArrayList<>();
        resolvedTree.forEachTree(Path.getEmptyPath(), new SparseSnapshotTreeVisitor() {
            public void visitTree(Path prefixPath, Node node) {
                events.addAll(Repo.this.serverSyncTree.applyServerOverwrite(prefixPath, node));
                Repo.this.rerunTransactions(Repo.this.abortTransactions(prefixPath, -9));
            }
        });
        this.onDisconnect = new SparseSnapshotTree();
        postEvents(events);
    }

    /* access modifiers changed from: private */
    public void warnIfWriteFailed(String writeType, Path path, FirebaseError error) {
        if (error != null && error.getCode() != -1 && error.getCode() != -25) {
            this.operationLogger.warn(writeType + " at " + path.toString() + " failed: " + error.toString());
        }
    }

    public void startTransaction(Path path, Handler handler, boolean applyLocally) {
        Result result;
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("transaction: " + path);
        }
        if (this.dataLogger.logsDebug()) {
            this.operationLogger.debug("transaction: " + path);
        }
        if (this.ctx.isPersistenceEnabled() && !this.loggedTransactionPersistenceWarning) {
            this.loggedTransactionPersistenceWarning = true;
            this.transactionLogger.info("runTransaction() usage detected while persistence is enabled. Please be aware that transactions *will not* be persisted across app restarts.  See https://www.firebase.com/docs/android/guide/offline-capabilities.html#section-handling-transactions-offline for more details.");
        }
        Firebase watchRef = new Firebase(this, path);
        ValueEventListener listener = new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
            }

            public void onCancelled(FirebaseError error) {
            }
        };
        addEventCallback(new ValueEventRegistration(this, listener, watchRef.getSpec()));
        TransactionData transaction = new TransactionData(path, handler, listener, TransactionStatus.INITIALIZING, applyLocally, nextTransactionOrder());
        Node currentState = getLatestState(path);
        transaction.currentInputSnapshot = currentState;
        MutableData mutableData = new MutableData(currentState);
        FirebaseError error = null;
        try {
            result = handler.doTransaction(mutableData);
            if (result == null) {
                throw new NullPointerException("Transaction returned null as result");
            }
        } catch (Throwable e) {
            error = FirebaseError.fromException(e);
            result = Transaction.abort();
        }
        if (!result.isSuccess()) {
            transaction.currentOutputSnapshotRaw = null;
            transaction.currentOutputSnapshotResolved = null;
            FirebaseError innerClassError = error;
            DataSnapshot dataSnapshot = new DataSnapshot(watchRef, IndexedNode.from(transaction.currentInputSnapshot));
            final Handler handler2 = handler;
            final FirebaseError firebaseError = innerClassError;
            final DataSnapshot dataSnapshot2 = dataSnapshot;
            postEvent(new Runnable() {
                public void run() {
                    handler2.onComplete(firebaseError, Repo.$assertionsDisabled, dataSnapshot2);
                }
            });
            return;
        }
        transaction.status = TransactionStatus.RUN;
        Tree<List<TransactionData>> queueNode = this.transactionQueueTree.subTree(path);
        List<TransactionData> nodeQueue = (List) queueNode.getValue();
        if (nodeQueue == null) {
            nodeQueue = new ArrayList<>();
        }
        nodeQueue.add(transaction);
        queueNode.setValue(nodeQueue);
        Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
        Node newNodeUnresolved = result.getNode();
        Node newNode = ServerValues.resolveDeferredValueSnapshot(newNodeUnresolved, serverValues);
        transaction.currentOutputSnapshotRaw = newNodeUnresolved;
        transaction.currentOutputSnapshotResolved = newNode;
        transaction.currentWriteId = getNextWriteId();
        postEvents(this.serverSyncTree.applyUserOverwrite(path, newNodeUnresolved, newNode, transaction.currentWriteId, applyLocally, $assertionsDisabled));
        sendAllReadyTransactions();
    }

    private Node getLatestState(Path path) {
        return getLatestState(path, new ArrayList());
    }

    private Node getLatestState(Path path, List<Long> excudeSets) {
        Node state = this.serverSyncTree.calcCompleteEventCache(path, excudeSets);
        if (state == null) {
            return EmptyNode.Empty();
        }
        return state;
    }

    public void setHijackHash(boolean hijackHash2) {
        this.hijackHash = hijackHash2;
    }

    /* access modifiers changed from: private */
    public void sendAllReadyTransactions() {
        Tree<List<TransactionData>> node = this.transactionQueueTree;
        pruneCompletedTransactions(node);
        sendReadyTransactions(node);
    }

    /* access modifiers changed from: private */
    public void sendReadyTransactions(Tree<List<TransactionData>> node) {
        if (((List) node.getValue()) != null) {
            List<TransactionData> queue = buildTransactionQueue(node);
            if ($assertionsDisabled || queue.size() > 0) {
                Boolean allRun = Boolean.valueOf(true);
                Iterator i$ = queue.iterator();
                while (true) {
                    if (i$.hasNext()) {
                        if (((TransactionData) i$.next()).status != TransactionStatus.RUN) {
                            allRun = Boolean.valueOf($assertionsDisabled);
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (allRun.booleanValue()) {
                    sendTransactionQueue(queue, node.getPath());
                    return;
                }
                return;
            }
            throw new AssertionError();
        } else if (node.hasChildren()) {
            node.forEachChild(new TreeVisitor<List<TransactionData>>() {
                public void visitTree(Tree<List<TransactionData>> tree) {
                    Repo.this.sendReadyTransactions(tree);
                }
            });
        }
    }

    private void sendTransactionQueue(List<TransactionData> queue, Path path) {
        List<Long> setsToIgnore = new ArrayList<>();
        for (TransactionData txn : queue) {
            setsToIgnore.add(Long.valueOf(txn.currentWriteId));
        }
        Node latestState = getLatestState(path, setsToIgnore);
        Node snapToSend = latestState;
        String latestHash = "badhash";
        if (!this.hijackHash) {
            latestHash = latestState.getHash();
        }
        for (TransactionData txn2 : queue) {
            if ($assertionsDisabled || txn2.status == TransactionStatus.RUN) {
                txn2.status = TransactionStatus.SENT;
                txn2.retryCount = txn2.retryCount + 1;
                snapToSend = snapToSend.updateChild(Path.getRelative(path, txn2.path), txn2.currentOutputSnapshotRaw);
            } else {
                throw new AssertionError();
            }
        }
        Object dataToSend = snapToSend.getValue(true);
        long nextWriteId2 = getNextWriteId();
        PersistentConnection persistentConnection = this.connection;
        String path2 = path.toString();
        final Path path3 = path;
        final List<TransactionData> list = queue;
        AnonymousClass15 r0 = new Firebase.CompletionListener() {
            public void onComplete(FirebaseError error, Firebase ref) {
                Repo.this.warnIfWriteFailed("Transaction", path3, error);
                List<Event> events = new ArrayList<>();
                if (error == null) {
                    List<Runnable> callbacks = new ArrayList<>();
                    for (final TransactionData txn : list) {
                        txn.status = TransactionStatus.COMPLETED;
                        events.addAll(Repo.this.serverSyncTree.ackUserWrite(txn.currentWriteId, Repo.$assertionsDisabled, Repo.$assertionsDisabled, Repo.this.serverClock));
                        final DataSnapshot snap = new DataSnapshot(new Firebase(this, txn.path), IndexedNode.from(txn.currentOutputSnapshotResolved));
                        callbacks.add(new Runnable() {
                            public void run() {
                                txn.handler.onComplete(null, true, snap);
                            }
                        });
                        Repo.this.removeEventCallback(new ValueEventRegistration(Repo.this, txn.outstandingListener, QuerySpec.defaultQueryAtPath(txn.path)));
                    }
                    Repo.this.pruneCompletedTransactions(Repo.this.transactionQueueTree.subTree(path3));
                    Repo.this.sendAllReadyTransactions();
                    this.postEvents(events);
                    for (int i = 0; i < callbacks.size(); i++) {
                        Repo.this.postEvent((Runnable) callbacks.get(i));
                    }
                    return;
                }
                if (error.getCode() == -1) {
                    for (TransactionData transaction : list) {
                        if (transaction.status == TransactionStatus.SENT_NEEDS_ABORT) {
                            transaction.status = TransactionStatus.NEEDS_ABORT;
                        } else {
                            transaction.status = TransactionStatus.RUN;
                        }
                    }
                } else {
                    for (TransactionData transaction2 : list) {
                        transaction2.status = TransactionStatus.NEEDS_ABORT;
                        transaction2.abortReason = error;
                    }
                }
                Repo.this.rerunTransactions(path3);
            }
        };
        persistentConnection.put(path2, dataToSend, latestHash, r0);
    }

    /* access modifiers changed from: private */
    public void pruneCompletedTransactions(Tree<List<TransactionData>> node) {
        List<TransactionData> queue = (List) node.getValue();
        if (queue != null) {
            int i = 0;
            while (i < queue.size()) {
                if (((TransactionData) queue.get(i)).status == TransactionStatus.COMPLETED) {
                    queue.remove(i);
                } else {
                    i++;
                }
            }
            if (queue.size() > 0) {
                node.setValue(queue);
            } else {
                node.setValue(null);
            }
        }
        node.forEachChild(new TreeVisitor<List<TransactionData>>() {
            public void visitTree(Tree<List<TransactionData>> tree) {
                Repo.this.pruneCompletedTransactions(tree);
            }
        });
    }

    private long nextTransactionOrder() {
        long j = this.transactionOrder;
        this.transactionOrder = 1 + j;
        return j;
    }

    /* access modifiers changed from: private */
    public Path rerunTransactions(Path changedPath) {
        Tree<List<TransactionData>> rootMostTransactionNode = getAncestorTransactionNode(changedPath);
        Path path = rootMostTransactionNode.getPath();
        rerunTransactionQueue(buildTransactionQueue(rootMostTransactionNode), path);
        return path;
    }

    private void rerunTransactionQueue(List<TransactionData> queue, Path path) {
        Result result;
        if (!queue.isEmpty()) {
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            for (TransactionData transaction : queue) {
                arrayList2.add(Long.valueOf(transaction.currentWriteId));
            }
            for (TransactionData transaction2 : queue) {
                Path relativePath = Path.getRelative(path, transaction2.path);
                boolean abortTransaction = $assertionsDisabled;
                FirebaseError abortReason = null;
                ArrayList arrayList3 = new ArrayList();
                if ($assertionsDisabled || relativePath != null) {
                    if (transaction2.status == TransactionStatus.NEEDS_ABORT) {
                        abortTransaction = true;
                        abortReason = transaction2.abortReason;
                        if (abortReason.getCode() != -25) {
                            arrayList3.addAll(this.serverSyncTree.ackUserWrite(transaction2.currentWriteId, true, $assertionsDisabled, this.serverClock));
                        }
                    } else if (transaction2.status == TransactionStatus.RUN) {
                        if (transaction2.retryCount >= 25) {
                            abortTransaction = true;
                            abortReason = FirebaseError.fromStatus(TRANSACTION_TOO_MANY_RETRIES);
                            arrayList3.addAll(this.serverSyncTree.ackUserWrite(transaction2.currentWriteId, true, $assertionsDisabled, this.serverClock));
                        } else {
                            Node currentNode = getLatestState(transaction2.path, arrayList2);
                            transaction2.currentInputSnapshot = currentNode;
                            MutableData mutableData = new MutableData(currentNode);
                            FirebaseError error = null;
                            try {
                                result = transaction2.handler.doTransaction(mutableData);
                            } catch (Throwable e) {
                                error = FirebaseError.fromException(e);
                                result = Transaction.abort();
                            }
                            if (result.isSuccess()) {
                                Long oldWriteId = Long.valueOf(transaction2.currentWriteId);
                                Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
                                Node newDataNode = result.getNode();
                                Node newNodeResolved = ServerValues.resolveDeferredValueSnapshot(newDataNode, serverValues);
                                transaction2.currentOutputSnapshotRaw = newDataNode;
                                transaction2.currentOutputSnapshotResolved = newNodeResolved;
                                transaction2.currentWriteId = getNextWriteId();
                                arrayList2.remove(oldWriteId);
                                arrayList3.addAll(this.serverSyncTree.applyUserOverwrite(transaction2.path, newDataNode, newNodeResolved, transaction2.currentWriteId, transaction2.applyLocally, $assertionsDisabled));
                                arrayList3.addAll(this.serverSyncTree.ackUserWrite(oldWriteId.longValue(), true, $assertionsDisabled, this.serverClock));
                            } else {
                                abortTransaction = true;
                                abortReason = error;
                                arrayList3.addAll(this.serverSyncTree.ackUserWrite(transaction2.currentWriteId, true, $assertionsDisabled, this.serverClock));
                            }
                        }
                    }
                    postEvents(arrayList3);
                    if (abortTransaction) {
                        transaction2.status = TransactionStatus.COMPLETED;
                        Firebase firebase = new Firebase(this, transaction2.path);
                        DataSnapshot dataSnapshot = new DataSnapshot(firebase, IndexedNode.from(transaction2.currentInputSnapshot));
                        final TransactionData transactionData = transaction2;
                        scheduleNow(new Runnable() {
                            public void run() {
                                Repo.this.removeEventCallback(new ValueEventRegistration(Repo.this, transactionData.outstandingListener, QuerySpec.defaultQueryAtPath(transactionData.path)));
                            }
                        });
                        final TransactionData transactionData2 = transaction2;
                        final FirebaseError firebaseError = abortReason;
                        final DataSnapshot dataSnapshot2 = dataSnapshot;
                        arrayList.add(new Runnable() {
                            public void run() {
                                transactionData2.handler.onComplete(firebaseError, Repo.$assertionsDisabled, dataSnapshot2);
                            }
                        });
                    }
                } else {
                    throw new AssertionError();
                }
            }
            pruneCompletedTransactions(this.transactionQueueTree);
            for (int i = 0; i < arrayList.size(); i++) {
                postEvent((Runnable) arrayList.get(i));
            }
            sendAllReadyTransactions();
        }
    }

    private Tree<List<TransactionData>> getAncestorTransactionNode(Path path) {
        Tree<List<TransactionData>> transactionNode = this.transactionQueueTree;
        while (!path.isEmpty() && transactionNode.getValue() == null) {
            transactionNode = transactionNode.subTree(new Path(path.getFront()));
            path = path.popFront();
        }
        return transactionNode;
    }

    private List<TransactionData> buildTransactionQueue(Tree<List<TransactionData>> transactionNode) {
        List<TransactionData> queue = new ArrayList<>();
        aggregateTransactionQueues(queue, transactionNode);
        Collections.sort(queue);
        return queue;
    }

    /* access modifiers changed from: private */
    public void aggregateTransactionQueues(final List<TransactionData> queue, Tree<List<TransactionData>> node) {
        List<TransactionData> childQueue = (List) node.getValue();
        if (childQueue != null) {
            queue.addAll(childQueue);
        }
        node.forEachChild(new TreeVisitor<List<TransactionData>>() {
            public void visitTree(Tree<List<TransactionData>> tree) {
                Repo.this.aggregateTransactionQueues(queue, tree);
            }
        });
    }

    /* access modifiers changed from: private */
    public Path abortTransactions(Path path, final int reason) {
        Path affectedPath = getAncestorTransactionNode(path).getPath();
        if (this.transactionLogger.logsDebug()) {
            this.operationLogger.debug("Aborting transactions for path: " + path + ". Affected: " + affectedPath);
        }
        Tree<List<TransactionData>> transactionNode = this.transactionQueueTree.subTree(path);
        transactionNode.forEachAncestor(new TreeFilter<List<TransactionData>>() {
            public boolean filterTreeNode(Tree<List<TransactionData>> tree) {
                Repo.this.abortTransactionsAtNode(tree, reason);
                return Repo.$assertionsDisabled;
            }
        });
        abortTransactionsAtNode(transactionNode, reason);
        transactionNode.forEachDescendant(new TreeVisitor<List<TransactionData>>() {
            public void visitTree(Tree<List<TransactionData>> tree) {
                Repo.this.abortTransactionsAtNode(tree, reason);
            }
        });
        return affectedPath;
    }

    /* access modifiers changed from: private */
    public void abortTransactionsAtNode(Tree<List<TransactionData>> node, int reason) {
        final FirebaseError abortError;
        List<TransactionData> queue = (List) node.getValue();
        List<Event> events = new ArrayList<>();
        if (queue != null) {
            List<Runnable> callbacks = new ArrayList<>();
            if (reason == -9) {
                abortError = FirebaseError.fromStatus(TRANSACTION_OVERRIDE_BY_SET);
            } else {
                Utilities.hardAssert(reason == -25 ? true : $assertionsDisabled, "Unknown transaction abort reason: " + reason);
                abortError = FirebaseError.fromCode(-25);
            }
            int lastSent = -1;
            for (int i = 0; i < queue.size(); i++) {
                TransactionData transaction = (TransactionData) queue.get(i);
                if (transaction.status != TransactionStatus.SENT_NEEDS_ABORT) {
                    if (transaction.status == TransactionStatus.SENT) {
                        if ($assertionsDisabled || lastSent == i - 1) {
                            lastSent = i;
                            transaction.status = TransactionStatus.SENT_NEEDS_ABORT;
                            transaction.abortReason = abortError;
                        } else {
                            throw new AssertionError();
                        }
                    } else if ($assertionsDisabled || transaction.status == TransactionStatus.RUN) {
                        removeEventCallback(new ValueEventRegistration(this, transaction.outstandingListener, QuerySpec.defaultQueryAtPath(transaction.path)));
                        if (reason == -9) {
                            events.addAll(this.serverSyncTree.ackUserWrite(transaction.currentWriteId, true, $assertionsDisabled, this.serverClock));
                        } else {
                            Utilities.hardAssert(reason == -25 ? true : $assertionsDisabled, "Unknown transaction abort reason: " + reason);
                        }
                        final TransactionData transactionData = transaction;
                        callbacks.add(new Runnable() {
                            public void run() {
                                transactionData.handler.onComplete(abortError, Repo.$assertionsDisabled, null);
                            }
                        });
                    } else {
                        throw new AssertionError();
                    }
                }
            }
            if (lastSent == -1) {
                node.setValue(null);
            } else {
                node.setValue(queue.subList(0, lastSent + 1));
            }
            postEvents(events);
            for (Runnable r : callbacks) {
                postEvent(r);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public SyncTree getServerSyncTree() {
        return this.serverSyncTree;
    }

    /* access modifiers changed from: 0000 */
    public SyncTree getInfoSyncTree() {
        return this.infoSyncTree;
    }
}
