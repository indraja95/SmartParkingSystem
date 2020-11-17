package com.firebase.client.core;

import androidx.core.app.NotificationCompat;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.AuthListener;
import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.FirebaseError;
import com.firebase.client.core.SyncTree.SyncTreeHash;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.realtime.Connection;
import com.firebase.client.realtime.Connection.DisconnectReason;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.Utilities;
import com.shaded.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

public class PersistentConnection implements com.firebase.client.realtime.Connection.Delegate {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final long RECONNECT_MAX_DELAY = 30000;
    private static final long RECONNECT_MIN_DELAY = 1000;
    private static final double RECONNECT_MULTIPLIER = 1.3d;
    private static final long RECONNECT_RESET_TIMEOUT = 30000;
    private static final String REQUEST_ACTION = "a";
    private static final String REQUEST_ACTION_AUTH = "auth";
    private static final String REQUEST_ACTION_LISTEN = "l";
    private static final String REQUEST_ACTION_MERGE = "m";
    private static final String REQUEST_ACTION_ONDISCONNECT_CANCEL = "oc";
    private static final String REQUEST_ACTION_ONDISCONNECT_MERGE = "om";
    private static final String REQUEST_ACTION_ONDISCONNECT_PUT = "o";
    private static final String REQUEST_ACTION_PUT = "p";
    private static final String REQUEST_ACTION_QUERY = "q";
    private static final String REQUEST_ACTION_QUERY_UNLISTEN = "n";
    private static final String REQUEST_ACTION_STATS = "s";
    private static final String REQUEST_ACTION_UNAUTH = "unauth";
    private static final String REQUEST_ACTION_UNLISTEN = "u";
    private static final String REQUEST_COMPOUND_HASH = "ch";
    private static final String REQUEST_COMPOUND_HASH_HASHES = "hs";
    private static final String REQUEST_COMPOUND_HASH_PATHS = "ps";
    private static final String REQUEST_COUNTERS = "c";
    private static final String REQUEST_CREDENTIAL = "cred";
    private static final String REQUEST_DATA_HASH = "h";
    private static final String REQUEST_DATA_PAYLOAD = "d";
    private static final String REQUEST_ERROR = "error";
    private static final String REQUEST_NUMBER = "r";
    private static final String REQUEST_PATH = "p";
    private static final String REQUEST_PAYLOAD = "b";
    private static final String REQUEST_QUERIES = "q";
    private static final String REQUEST_STATUS = "s";
    private static final String REQUEST_TAG = "t";
    private static final String RESPONSE_FOR_REQUEST = "b";
    private static final String SERVER_ASYNC_ACTION = "a";
    private static final String SERVER_ASYNC_AUTH_REVOKED = "ac";
    private static final String SERVER_ASYNC_DATA_MERGE = "m";
    private static final String SERVER_ASYNC_DATA_RANGE_MERGE = "rm";
    private static final String SERVER_ASYNC_DATA_UPDATE = "d";
    private static final String SERVER_ASYNC_LISTEN_CANCELLED = "c";
    private static final String SERVER_ASYNC_PAYLOAD = "b";
    private static final String SERVER_ASYNC_SECURITY_DEBUG = "sd";
    private static final String SERVER_DATA_END_PATH = "e";
    private static final String SERVER_DATA_RANGE_MERGE = "m";
    private static final String SERVER_DATA_START_PATH = "s";
    private static final String SERVER_DATA_TAG = "t";
    private static final String SERVER_DATA_UPDATE_BODY = "d";
    private static final String SERVER_DATA_UPDATE_PATH = "p";
    private static final String SERVER_DATA_WARNINGS = "w";
    private static final String SERVER_RESPONSE_DATA = "d";
    private static long connectionIds = 0;
    /* access modifiers changed from: private */
    public AuthCredential authCredential;
    /* access modifiers changed from: private */
    public ConnectionState connectionState = ConnectionState.Disconnected;
    private Context ctx;
    /* access modifiers changed from: private */
    public Delegate delegate;
    private boolean firstConnection = true;
    private long lastConnectionAttemptTime;
    private long lastConnectionEstablishedTime;
    private String lastSessionId;
    /* access modifiers changed from: private */
    public Map<QuerySpec, OutstandingListen> listens;
    /* access modifiers changed from: private */
    public LogWrapper logger;
    private List<OutstandingDisconnect> onDisconnectRequestQueue;
    /* access modifiers changed from: private */
    public Map<Long, OutstandingPut> outstandingPuts;
    private Random random;
    private Connection realtime;
    private long reconnectDelay = 1000;
    private ScheduledFuture reconnectFuture;
    private RepoInfo repoInfo;
    private Map<Long, ResponseListener> requestCBHash;
    private long requestCounter = 0;
    private boolean shouldReconnect = true;
    private long writeCounter = 0;
    private boolean writesPaused;

    private static class AuthCredential {
        static final /* synthetic */ boolean $assertionsDisabled = (!PersistentConnection.class.desiredAssertionStatus() ? true : PersistentConnection.$assertionsDisabled);
        private Object authData;
        private String credential;
        private List<AuthListener> listeners = new ArrayList();
        private boolean onSuccessCalled = PersistentConnection.$assertionsDisabled;

        AuthCredential(AuthListener listener, String credential2) {
            this.listeners.add(listener);
            this.credential = credential2;
        }

        public boolean matches(String credential2) {
            return this.credential.equals(credential2);
        }

        public void preempt() {
            FirebaseError error = FirebaseError.fromStatus("preempted");
            for (AuthListener listener : this.listeners) {
                listener.onAuthError(error);
            }
        }

        public void addListener(AuthListener listener) {
            this.listeners.add(listener);
        }

        public void replay(AuthListener listener) {
            if ($assertionsDisabled || this.authData != null) {
                listener.onAuthSuccess(this.authData);
                return;
            }
            throw new AssertionError();
        }

        public boolean isComplete() {
            return this.onSuccessCalled;
        }

        public String getCredential() {
            return this.credential;
        }

        public void onCancel(FirebaseError error) {
            if (this.onSuccessCalled) {
                onRevoked(error);
                return;
            }
            for (AuthListener listener : this.listeners) {
                listener.onAuthError(error);
            }
        }

        public void onRevoked(FirebaseError error) {
            for (AuthListener listener : this.listeners) {
                listener.onAuthRevoked(error);
            }
        }

        public void onSuccess(Object authData2) {
            if (!this.onSuccessCalled) {
                this.onSuccessCalled = true;
                this.authData = authData2;
                for (AuthListener listener : this.listeners) {
                    listener.onAuthSuccess(authData2);
                }
            }
        }
    }

    private enum ConnectionState {
        Disconnected,
        Authenticating,
        Connected
    }

    public interface Delegate {
        void onAuthStatus(boolean z);

        void onConnect();

        void onDataUpdate(String str, Object obj, boolean z, Tag tag);

        void onDisconnect();

        void onRangeMergeUpdate(Path path, List<RangeMerge> list, Tag tag);

        void onServerInfoUpdate(Map<ChildKey, Object> map);
    }

    private static class OutstandingDisconnect {
        private final String action;
        private final Object data;
        /* access modifiers changed from: private */
        public final CompletionListener onComplete;
        private final Path path;

        private OutstandingDisconnect(String action2, Path path2, Object data2, CompletionListener onComplete2) {
            this.action = action2;
            this.path = path2;
            this.data = data2;
            this.onComplete = onComplete2;
        }

        public String getAction() {
            return this.action;
        }

        public Path getPath() {
            return this.path;
        }

        public Object getData() {
            return this.data;
        }

        public CompletionListener getOnComplete() {
            return this.onComplete;
        }
    }

    static class OutstandingListen {
        private final SyncTreeHash hashFunction;
        /* access modifiers changed from: private */
        public final QuerySpec query;
        /* access modifiers changed from: private */
        public final RequestResultListener resultListener;
        private final Tag tag;

        private OutstandingListen(RequestResultListener listener, QuerySpec query2, Tag tag2, SyncTreeHash hashFunction2) {
            this.resultListener = listener;
            this.query = query2;
            this.hashFunction = hashFunction2;
            this.tag = tag2;
        }

        public QuerySpec getQuery() {
            return this.query;
        }

        public Tag getTag() {
            return this.tag;
        }

        public SyncTreeHash getHashFunction() {
            return this.hashFunction;
        }

        public String toString() {
            return this.query.toString() + " (Tag: " + this.tag + ")";
        }
    }

    private static class OutstandingPut {
        private String action;
        /* access modifiers changed from: private */
        public CompletionListener onComplete;
        private Map<String, Object> request;

        private OutstandingPut(String action2, Map<String, Object> request2, CompletionListener onComplete2) {
            this.action = action2;
            this.request = request2;
            this.onComplete = onComplete2;
        }

        public String getAction() {
            return this.action;
        }

        public Map<String, Object> getRequest() {
            return this.request;
        }

        public CompletionListener getOnComplete() {
            return this.onComplete;
        }
    }

    interface RequestResultListener {
        void onRequestResult(FirebaseError firebaseError);
    }

    private interface ResponseListener {
        void onResponse(Map<String, Object> map);
    }

    static {
        boolean z;
        if (!PersistentConnection.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = $assertionsDisabled;
        }
        $assertionsDisabled = z;
    }

    public PersistentConnection(Context ctx2, RepoInfo info, Delegate delegate2) {
        this.delegate = delegate2;
        this.ctx = ctx2;
        this.repoInfo = info;
        this.listens = new HashMap();
        this.requestCBHash = new HashMap();
        this.writesPaused = $assertionsDisabled;
        this.outstandingPuts = new HashMap();
        this.onDisconnectRequestQueue = new ArrayList();
        this.random = new Random();
        long connId = connectionIds;
        connectionIds = 1 + connId;
        this.logger = this.ctx.getLogger("PersistentConnection", "pc_" + connId);
        this.lastSessionId = null;
    }

    public void establishConnection() {
        if (this.shouldReconnect) {
            this.lastConnectionAttemptTime = System.currentTimeMillis();
            this.lastConnectionEstablishedTime = 0;
            this.realtime = new Connection(this.ctx, this.repoInfo, this, this.lastSessionId);
            this.realtime.open();
        }
    }

    public void onReady(long timestamp, String sessionId) {
        if (this.logger.logsDebug()) {
            this.logger.debug("onReady");
        }
        this.lastConnectionEstablishedTime = System.currentTimeMillis();
        handleTimestamp(timestamp);
        if (this.firstConnection) {
            sendConnectStats();
        }
        restoreState();
        this.firstConnection = $assertionsDisabled;
        this.lastSessionId = sessionId;
        this.delegate.onConnect();
    }

    public void listen(QuerySpec query, SyncTreeHash currentHashFn, Tag tag, RequestResultListener listener) {
        boolean z;
        boolean z2 = true;
        if (this.logger.logsDebug()) {
            this.logger.debug("Listening on " + query);
        }
        if (query.isDefault() || !query.loadsAllData()) {
            z = true;
        } else {
            z = false;
        }
        Utilities.hardAssert(z, "listen() called for non-default but complete query");
        if (this.listens.containsKey(query)) {
            z2 = false;
        }
        Utilities.hardAssert(z2, "listen() called twice for same QuerySpec.");
        if (this.logger.logsDebug()) {
            this.logger.debug("Adding listen query: " + query);
        }
        OutstandingListen outstandingListen = new OutstandingListen(listener, query, tag, currentHashFn);
        this.listens.put(query, outstandingListen);
        if (connected()) {
            sendListen(outstandingListen);
        }
    }

    public Map<QuerySpec, OutstandingListen> getListens() {
        return this.listens;
    }

    public void put(String pathString, Object data, CompletionListener onComplete) {
        put(pathString, data, null, onComplete);
    }

    public void put(String pathString, Object data, String hash, CompletionListener onComplete) {
        putInternal("p", pathString, data, hash, onComplete);
    }

    public void merge(String pathString, Object data, CompletionListener onComplete) {
        putInternal("m", pathString, data, null, onComplete);
    }

    public void purgeOutstandingWrites() {
        FirebaseError error = FirebaseError.fromCode(-25);
        for (OutstandingPut put : this.outstandingPuts.values()) {
            if (put.onComplete != null) {
                put.onComplete.onComplete(error, null);
            }
        }
        for (OutstandingDisconnect onDisconnect : this.onDisconnectRequestQueue) {
            if (onDisconnect.onComplete != null) {
                onDisconnect.onComplete.onComplete(error, null);
            }
        }
        this.outstandingPuts.clear();
        this.onDisconnectRequestQueue.clear();
    }

    public void onDataMessage(Map<String, Object> message) {
        if (message.containsKey(REQUEST_NUMBER)) {
            ResponseListener responseListener = (ResponseListener) this.requestCBHash.remove(Long.valueOf((long) ((Integer) message.get(REQUEST_NUMBER)).intValue()));
            if (responseListener != null) {
                responseListener.onResponse((Map) message.get("b"));
            }
        } else if (message.containsKey(REQUEST_ERROR)) {
        } else {
            if (message.containsKey("a")) {
                onDataPush((String) message.get("a"), (Map) message.get("b"));
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring unknown message: " + message);
            }
        }
    }

    public void onDisconnect(DisconnectReason reason) {
        long recDelay;
        if (this.logger.logsDebug()) {
            this.logger.debug("Got on disconnect due to " + reason.name());
        }
        this.connectionState = ConnectionState.Disconnected;
        if (!this.shouldReconnect) {
            cancelTransactions();
            this.requestCBHash.clear();
        } else {
            if (reason == DisconnectReason.SERVER_RESET) {
                recDelay = 0;
            } else {
                if (this.lastConnectionEstablishedTime > 0) {
                    if (System.currentTimeMillis() - this.lastConnectionEstablishedTime > 30000) {
                        this.reconnectDelay = 1000;
                    }
                    this.lastConnectionEstablishedTime = 0;
                }
                recDelay = (long) this.random.nextInt((int) Math.max(1, this.reconnectDelay - (System.currentTimeMillis() - this.lastConnectionAttemptTime)));
            }
            if (this.logger.logsDebug()) {
                this.logger.debug("Reconnecting in " + recDelay + "ms");
            }
            this.reconnectFuture = this.ctx.getRunLoop().schedule(new Runnable() {
                public void run() {
                    PersistentConnection.this.establishConnection();
                }
            }, recDelay);
            this.reconnectDelay = Math.min(30000, (long) (((double) this.reconnectDelay) * RECONNECT_MULTIPLIER));
        }
        this.delegate.onDisconnect();
    }

    public void onKill(String reason) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Firebase connection was forcefully killed by the server. Will not attempt reconnect. Reason: " + reason);
        }
        this.shouldReconnect = $assertionsDisabled;
    }

    /* access modifiers changed from: 0000 */
    public void unlisten(QuerySpec query) {
        if (this.logger.logsDebug()) {
            this.logger.debug("unlistening on " + query);
        }
        Utilities.hardAssert((query.isDefault() || !query.loadsAllData()) ? true : $assertionsDisabled, "unlisten() called for non-default but complete query");
        OutstandingListen listen = removeListen(query);
        if (listen != null && connected()) {
            sendUnlisten(listen);
        }
    }

    private boolean connected() {
        if (this.connectionState != ConnectionState.Disconnected) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: 0000 */
    public void onDisconnectPut(Path path, Object data, CompletionListener onComplete) {
        if (canSendWrites()) {
            sendOnDisconnect(REQUEST_ACTION_ONDISCONNECT_PUT, path, data, onComplete);
        } else {
            this.onDisconnectRequestQueue.add(new OutstandingDisconnect(REQUEST_ACTION_ONDISCONNECT_PUT, path, data, onComplete));
        }
    }

    private boolean canSendWrites() {
        if (this.connectionState != ConnectionState.Connected || this.writesPaused) {
            return $assertionsDisabled;
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public void onDisconnectMerge(Path path, Map<String, Object> updates, CompletionListener onComplete) {
        if (canSendWrites()) {
            sendOnDisconnect(REQUEST_ACTION_ONDISCONNECT_MERGE, path, updates, onComplete);
        } else {
            this.onDisconnectRequestQueue.add(new OutstandingDisconnect(REQUEST_ACTION_ONDISCONNECT_MERGE, path, updates, onComplete));
        }
    }

    /* access modifiers changed from: 0000 */
    public void onDisconnectCancel(Path path, CompletionListener onComplete) {
        if (canSendWrites()) {
            sendOnDisconnect(REQUEST_ACTION_ONDISCONNECT_CANCEL, path, null, onComplete);
        } else {
            this.onDisconnectRequestQueue.add(new OutstandingDisconnect(REQUEST_ACTION_ONDISCONNECT_CANCEL, path, null, onComplete));
        }
    }

    /* access modifiers changed from: 0000 */
    public void interrupt() {
        this.shouldReconnect = $assertionsDisabled;
        if (this.realtime != null) {
            this.realtime.close();
            this.realtime = null;
            return;
        }
        if (this.reconnectFuture != null) {
            this.reconnectFuture.cancel($assertionsDisabled);
            this.reconnectFuture = null;
        }
        onDisconnect(DisconnectReason.OTHER);
    }

    public void resume() {
        this.shouldReconnect = true;
        if (this.realtime == null) {
            establishConnection();
        }
    }

    public void auth(String credential, AuthListener listener) {
        if (this.authCredential == null) {
            this.authCredential = new AuthCredential(listener, credential);
        } else if (this.authCredential.matches(credential)) {
            this.authCredential.addListener(listener);
            if (this.authCredential.isComplete()) {
                this.authCredential.replay(listener);
            }
        } else {
            this.authCredential.preempt();
            this.authCredential = new AuthCredential(listener, credential);
        }
        if (connected()) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Authenticating with credential: " + credential);
            }
            sendAuth();
        }
    }

    public void unauth(final CompletionListener listener) {
        this.authCredential = null;
        this.delegate.onAuthStatus($assertionsDisabled);
        if (connected()) {
            sendAction(REQUEST_ACTION_UNAUTH, new HashMap(), new ResponseListener() {
                public void onResponse(Map<String, Object> response) {
                    String status = (String) response.get("s");
                    FirebaseError error = null;
                    if (!status.equals("ok")) {
                        error = FirebaseError.fromStatus(status, (String) response.get("d"));
                    }
                    listener.onComplete(error, null);
                }
            });
        }
    }

    public void pauseWrites() {
        if (this.logger.logsDebug()) {
            this.logger.debug("Writes paused.");
        }
        this.writesPaused = true;
    }

    public void unpauseWrites() {
        if (this.logger.logsDebug()) {
            this.logger.debug("Writes unpaused.");
        }
        this.writesPaused = $assertionsDisabled;
        if (canSendWrites()) {
            restoreWrites();
        }
    }

    public boolean writesPaused() {
        return this.writesPaused;
    }

    private void sendOnDisconnect(String action, Path path, Object data, final CompletionListener onComplete) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", path.toString());
        request.put("d", data);
        if (this.logger.logsDebug()) {
            this.logger.debug("onDisconnect " + action + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + request);
        }
        sendAction(action, request, new ResponseListener() {
            public void onResponse(Map<String, Object> response) {
                String status = (String) response.get("s");
                FirebaseError error = null;
                if (!status.equals("ok")) {
                    error = FirebaseError.fromStatus(status, (String) response.get("d"));
                }
                if (onComplete != null) {
                    onComplete.onComplete(error, null);
                }
            }
        });
    }

    private void cancelTransactions() {
        Iterator<Entry<Long, OutstandingPut>> iter = this.outstandingPuts.entrySet().iterator();
        while (iter.hasNext()) {
            OutstandingPut put = (OutstandingPut) ((Entry) iter.next()).getValue();
            if (put.getRequest().containsKey(REQUEST_DATA_HASH)) {
                put.getOnComplete().onComplete(FirebaseError.fromStatus("disconnected"), null);
                iter.remove();
            }
        }
    }

    private void sendUnlisten(OutstandingListen listen) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", listen.query.getPath().toString());
        Tag tag = listen.getTag();
        if (tag != null) {
            request.put("q", listen.getQuery().getParams().getWireProtocolParams());
            request.put("t", Long.valueOf(tag.getTagNumber()));
        }
        sendAction(REQUEST_ACTION_QUERY_UNLISTEN, request, null);
    }

    /* access modifiers changed from: private */
    public OutstandingListen removeListen(QuerySpec query) {
        if (this.logger.logsDebug()) {
            this.logger.debug("removing query " + query);
        }
        if (!this.listens.containsKey(query)) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Trying to remove listener for QuerySpec " + query + " but no listener exists.");
            }
            return null;
        }
        OutstandingListen outstandingListen = (OutstandingListen) this.listens.get(query);
        this.listens.remove(query);
        return outstandingListen;
    }

    public Collection<OutstandingListen> removeListens(Path path) {
        if (this.logger.logsDebug()) {
            this.logger.debug("removing all listens at path " + path);
        }
        List<OutstandingListen> removedListens = new ArrayList<>();
        for (Entry<QuerySpec, OutstandingListen> entry : this.listens.entrySet()) {
            OutstandingListen listen = (OutstandingListen) entry.getValue();
            if (((QuerySpec) entry.getKey()).getPath().equals(path)) {
                removedListens.add(listen);
            }
        }
        for (OutstandingListen toRemove : removedListens) {
            this.listens.remove(toRemove.getQuery());
        }
        return removedListens;
    }

    private void onDataPush(String action, Map<String, Object> body) {
        if (this.logger.logsDebug()) {
            this.logger.debug("handleServerMessage: " + action + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + body);
        }
        if (action.equals("d") || action.equals("m")) {
            boolean isMerge = action.equals("m");
            String pathString = (String) body.get("p");
            Object payloadData = body.get("d");
            Long tagNumber = Utilities.longFromObject(body.get("t"));
            Tag tag = tagNumber != null ? new Tag(tagNumber.longValue()) : null;
            if (!isMerge || !(payloadData instanceof Map) || ((Map) payloadData).size() != 0) {
                this.delegate.onDataUpdate(pathString, payloadData, isMerge, tag);
            } else if (this.logger.logsDebug()) {
                this.logger.debug("ignoring empty merge for path " + pathString);
            }
        } else if (action.equals(SERVER_ASYNC_DATA_RANGE_MERGE)) {
            String pathString2 = (String) body.get("p");
            Object payloadData2 = body.get("d");
            Long tagNumber2 = Utilities.longFromObject(body.get("t"));
            Tag tag2 = tagNumber2 != null ? new Tag(tagNumber2.longValue()) : null;
            List<Map<String, Object>> ranges = (List) payloadData2;
            List<RangeMerge> rangeMerges = new ArrayList<>();
            for (Map<String, Object> range : ranges) {
                String startString = (String) range.get("s");
                String endString = (String) range.get(SERVER_DATA_END_PATH);
                RangeMerge rangeMerge = new RangeMerge(startString != null ? new Path(startString) : null, endString != null ? new Path(endString) : null, NodeUtilities.NodeFromJSON(range.get("m")));
                rangeMerges.add(rangeMerge);
            }
            if (!rangeMerges.isEmpty()) {
                Delegate delegate2 = this.delegate;
                Path path = new Path(pathString2);
                delegate2.onRangeMergeUpdate(path, rangeMerges, tag2);
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring empty range merge for path " + pathString2);
            }
        } else if (action.equals("c")) {
            Path path2 = new Path((String) body.get("p"));
            onListenRevoked(path2);
        } else if (action.equals(SERVER_ASYNC_AUTH_REVOKED)) {
            onAuthRevoked((String) body.get("s"), (String) body.get("d"));
        } else if (action.equals(SERVER_ASYNC_SECURITY_DEBUG)) {
            onSecurityDebugPacket(body);
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Unrecognized action from server: " + action);
        }
    }

    private void onListenRevoked(Path path) {
        Collection<OutstandingListen> listens2 = removeListens(path);
        if (listens2 != null) {
            FirebaseError error = FirebaseError.fromStatus("permission_denied");
            for (OutstandingListen listen : listens2) {
                listen.resultListener.onRequestResult(error);
            }
        }
    }

    private void onAuthRevoked(String status, String reason) {
        if (this.authCredential != null) {
            this.authCredential.onRevoked(FirebaseError.fromStatus(status, reason));
            this.authCredential = null;
        }
    }

    private void onSecurityDebugPacket(Map<String, Object> message) {
        this.logger.info((String) message.get(NotificationCompat.CATEGORY_MESSAGE));
    }

    private void sendAuth() {
        sendAuthHelper($assertionsDisabled);
    }

    private void sendAuthAndRestoreWrites() {
        sendAuthHelper(true);
    }

    private void sendAuthHelper(final boolean restoreWritesAfterComplete) {
        if (!$assertionsDisabled && !connected()) {
            throw new AssertionError("Must be connected to send auth.");
        } else if ($assertionsDisabled || this.authCredential != null) {
            Map<String, Object> request = new HashMap<>();
            request.put(REQUEST_CREDENTIAL, this.authCredential.getCredential());
            final AuthCredential credential = this.authCredential;
            sendAction(REQUEST_ACTION_AUTH, request, new ResponseListener() {
                public void onResponse(Map<String, Object> response) {
                    PersistentConnection.this.connectionState = ConnectionState.Connected;
                    if (credential == PersistentConnection.this.authCredential) {
                        String status = (String) response.get("s");
                        if (status.equals("ok")) {
                            PersistentConnection.this.delegate.onAuthStatus(true);
                            credential.onSuccess(response.get("d"));
                        } else {
                            PersistentConnection.this.authCredential = null;
                            PersistentConnection.this.delegate.onAuthStatus(PersistentConnection.$assertionsDisabled);
                            credential.onCancel(FirebaseError.fromStatus(status, (String) response.get("d")));
                        }
                    }
                    if (restoreWritesAfterComplete) {
                        PersistentConnection.this.restoreWrites();
                    }
                }
            });
        } else {
            throw new AssertionError("Can't send auth if it's null.");
        }
    }

    private void restoreState() {
        if (this.logger.logsDebug()) {
            this.logger.debug("calling restore state");
        }
        if (this.authCredential != null) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Restoring auth.");
            }
            this.connectionState = ConnectionState.Authenticating;
            sendAuthAndRestoreWrites();
        } else {
            this.connectionState = ConnectionState.Connected;
        }
        if (this.logger.logsDebug()) {
            this.logger.debug("Restoring outstanding listens");
        }
        for (OutstandingListen listen : this.listens.values()) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Restoring listen " + listen.getQuery());
            }
            sendListen(listen);
        }
        if (this.connectionState == ConnectionState.Connected) {
            restoreWrites();
        }
    }

    /* access modifiers changed from: private */
    public void restoreWrites() {
        if (!$assertionsDisabled && this.connectionState != ConnectionState.Connected) {
            throw new AssertionError("Should be connected if we're restoring writes.");
        } else if (!this.writesPaused) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Restoring writes.");
            }
            ArrayList<Long> outstanding = new ArrayList<>(this.outstandingPuts.keySet());
            Collections.sort(outstanding);
            Iterator i$ = outstanding.iterator();
            while (i$.hasNext()) {
                sendPut(((Long) i$.next()).longValue());
            }
            for (OutstandingDisconnect disconnect : this.onDisconnectRequestQueue) {
                sendOnDisconnect(disconnect.getAction(), disconnect.getPath(), disconnect.getData(), disconnect.getOnComplete());
            }
            this.onDisconnectRequestQueue.clear();
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Writes are paused; skip restoring writes.");
        }
    }

    private void handleTimestamp(long timestamp) {
        if (this.logger.logsDebug()) {
            this.logger.debug("handling timestamp");
        }
        long timestampDelta = timestamp - System.currentTimeMillis();
        Map<ChildKey, Object> updates = new HashMap<>();
        updates.put(Constants.DOT_INFO_SERVERTIME_OFFSET, Long.valueOf(timestampDelta));
        this.delegate.onServerInfoUpdate(updates);
    }

    private Map<String, Object> getPutObject(String pathString, Object data, String hash) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", pathString);
        request.put("d", data);
        if (hash != null) {
            request.put(REQUEST_DATA_HASH, hash);
        }
        return request;
    }

    private void putInternal(String action, String pathString, Object data, String hash, CompletionListener onComplete) {
        Map<String, Object> request = getPutObject(pathString, data, hash);
        long writeId = this.writeCounter;
        this.writeCounter = 1 + writeId;
        this.outstandingPuts.put(Long.valueOf(writeId), new OutstandingPut(action, request, onComplete));
        if (canSendWrites()) {
            sendPut(writeId);
        }
    }

    private void sendPut(long putId) {
        if ($assertionsDisabled || canSendWrites()) {
            final OutstandingPut put = (OutstandingPut) this.outstandingPuts.get(Long.valueOf(putId));
            final CompletionListener onComplete = put.getOnComplete();
            final String action = put.getAction();
            final long j = putId;
            sendAction(action, put.getRequest(), new ResponseListener() {
                public void onResponse(Map<String, Object> response) {
                    if (PersistentConnection.this.logger.logsDebug()) {
                        PersistentConnection.this.logger.debug(action + " response: " + response);
                    }
                    if (((OutstandingPut) PersistentConnection.this.outstandingPuts.get(Long.valueOf(j))) == put) {
                        PersistentConnection.this.outstandingPuts.remove(Long.valueOf(j));
                        if (onComplete != null) {
                            String status = (String) response.get("s");
                            if (status.equals("ok")) {
                                onComplete.onComplete(null, null);
                            } else {
                                onComplete.onComplete(FirebaseError.fromStatus(status, (String) response.get("d")), null);
                            }
                        }
                    } else if (PersistentConnection.this.logger.logsDebug()) {
                        PersistentConnection.this.logger.debug("Ignoring on complete for put " + j + " because it was removed already.");
                    }
                }
            });
            return;
        }
        throw new AssertionError("sendPut called when we can't send writes (we're disconnected or writes are paused).");
    }

    private void sendListen(final OutstandingListen listen) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", listen.getQuery().getPath().toString());
        Tag tag = listen.getTag();
        if (tag != null) {
            request.put("q", listen.getQuery().getParams().getWireProtocolParams());
            request.put("t", Long.valueOf(tag.getTagNumber()));
        }
        SyncTreeHash hashFunction = listen.getHashFunction();
        request.put(REQUEST_DATA_HASH, hashFunction.getSimpleHash());
        if (hashFunction.shouldIncludeCompoundHash()) {
            CompoundHash compoundHash = hashFunction.getCompoundHash();
            List<String> posts = new ArrayList<>();
            for (Path path : compoundHash.getPosts()) {
                posts.add(path.wireFormat());
            }
            Map<String, Object> hash = new HashMap<>();
            hash.put(REQUEST_COMPOUND_HASH_HASHES, compoundHash.getHashes());
            hash.put(REQUEST_COMPOUND_HASH_PATHS, posts);
            request.put(REQUEST_COMPOUND_HASH, hash);
        }
        sendAction("q", request, new ResponseListener() {
            public void onResponse(Map<String, Object> response) {
                String status = (String) response.get("s");
                if (status.equals("ok")) {
                    Map<String, Object> serverBody = (Map) response.get("d");
                    if (serverBody.containsKey(PersistentConnection.SERVER_DATA_WARNINGS)) {
                        PersistentConnection.this.warnOnListenerWarnings((List) serverBody.get(PersistentConnection.SERVER_DATA_WARNINGS), listen.getQuery());
                    }
                }
                if (((OutstandingListen) PersistentConnection.this.listens.get(listen.getQuery())) != listen) {
                    return;
                }
                if (!status.equals("ok")) {
                    PersistentConnection.this.removeListen(listen.getQuery());
                    listen.resultListener.onRequestResult(FirebaseError.fromStatus(status, (String) response.get("d")));
                    return;
                }
                listen.resultListener.onRequestResult(null);
            }
        });
    }

    private void sendStats(Map<String, Integer> stats) {
        if (!stats.isEmpty()) {
            Map<String, Object> request = new HashMap<>();
            request.put("c", stats);
            sendAction("s", request, new ResponseListener() {
                public void onResponse(Map<String, Object> response) {
                    String status = (String) response.get("s");
                    if (!status.equals("ok")) {
                        FirebaseError error = FirebaseError.fromStatus(status, (String) response.get("d"));
                        if (PersistentConnection.this.logger.logsDebug()) {
                            PersistentConnection.this.logger.debug("Failed to send stats: " + error);
                        }
                    }
                }
            });
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Not sending stats because stats are empty");
        }
    }

    /* access modifiers changed from: private */
    public void warnOnListenerWarnings(List<String> warnings, QuerySpec query) {
        if (warnings.contains("no_index")) {
            this.logger.warn("Using an unspecified index. Consider adding '" + ("\".indexOn\": \"" + query.getIndex().getQueryDefinition() + '\"') + "' at " + query.getPath() + " to your security and Firebase rules for better performance");
        }
    }

    private void sendConnectStats() {
        Map<String, Integer> stats = new HashMap<>();
        if (AndroidSupport.isAndroid()) {
            if (this.ctx.isPersistenceEnabled()) {
                stats.put("persistence.android.enabled", Integer.valueOf(1));
            }
            stats.put("sdk.android." + Firebase.getSdkVersion().replace('.', '-'), Integer.valueOf(1));
        } else if ($assertionsDisabled || !this.ctx.isPersistenceEnabled()) {
            stats.put("sdk.java." + Firebase.getSdkVersion().replace('.', '-'), Integer.valueOf(1));
        } else {
            throw new AssertionError("Stats for persistence on JVM missing (persistence not yet supported)");
        }
        if (this.logger.logsDebug()) {
            this.logger.debug("Sending first connection stats");
        }
        sendStats(stats);
    }

    private void sendAction(String action, Map<String, Object> message, ResponseListener onResponse) {
        long rn = nextRequestNumber();
        Map<String, Object> request = new HashMap<>();
        request.put(REQUEST_NUMBER, Long.valueOf(rn));
        request.put("a", action);
        request.put("b", message);
        this.realtime.sendRequest(request);
        this.requestCBHash.put(Long.valueOf(rn), onResponse);
    }

    private long nextRequestNumber() {
        long j = this.requestCounter;
        this.requestCounter = 1 + j;
        return j;
    }
}
