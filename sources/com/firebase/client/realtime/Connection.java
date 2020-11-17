package com.firebase.client.realtime;

import com.firebase.client.core.Context;
import com.firebase.client.core.RepoInfo;
import com.firebase.client.utilities.LogWrapper;
import java.util.HashMap;
import java.util.Map;
import org.shaded.apache.http.protocol.HTTP;

public class Connection implements com.firebase.client.realtime.WebsocketConnection.Delegate {
    private static final String REQUEST_PAYLOAD = "d";
    private static final String REQUEST_TYPE = "t";
    private static final String REQUEST_TYPE_DATA = "d";
    private static final String SERVER_CONTROL_MESSAGE = "c";
    private static final String SERVER_CONTROL_MESSAGE_DATA = "d";
    private static final String SERVER_CONTROL_MESSAGE_HELLO = "h";
    private static final String SERVER_CONTROL_MESSAGE_RESET = "r";
    private static final String SERVER_CONTROL_MESSAGE_SHUTDOWN = "s";
    private static final String SERVER_CONTROL_MESSAGE_TYPE = "t";
    private static final String SERVER_DATA_MESSAGE = "d";
    private static final String SERVER_ENVELOPE_DATA = "d";
    private static final String SERVER_ENVELOPE_TYPE = "t";
    private static final String SERVER_HELLO_HOST = "h";
    private static final String SERVER_HELLO_SESSION_ID = "s";
    private static final String SERVER_HELLO_TIMESTAMP = "ts";
    private static long connectionIds = 0;
    private WebsocketConnection conn;
    private Delegate delegate;
    private LogWrapper logger;
    private RepoInfo repoInfo;
    private State state = State.REALTIME_CONNECTING;

    public interface Delegate {
        void onDataMessage(Map<String, Object> map);

        void onDisconnect(DisconnectReason disconnectReason);

        void onKill(String str);

        void onReady(long j, String str);
    }

    public enum DisconnectReason {
        SERVER_RESET,
        OTHER
    }

    private enum State {
        REALTIME_CONNECTING,
        REALTIME_CONNECTED,
        REALTIME_DISCONNECTED
    }

    public Connection(Context ctx, RepoInfo repoInfo2, Delegate delegate2, String optLastSessionId) {
        long connId = connectionIds;
        connectionIds = 1 + connId;
        this.repoInfo = repoInfo2;
        this.delegate = delegate2;
        this.logger = ctx.getLogger(HTTP.CONN_DIRECTIVE, "conn_" + connId);
        this.conn = new WebsocketConnection(ctx, repoInfo2, this, optLastSessionId);
    }

    public void open() {
        if (this.logger.logsDebug()) {
            this.logger.debug("Opening a connection");
        }
        this.conn.open();
    }

    public void close(DisconnectReason reason) {
        if (this.state != State.REALTIME_DISCONNECTED) {
            if (this.logger.logsDebug()) {
                this.logger.debug("closing realtime connection");
            }
            this.state = State.REALTIME_DISCONNECTED;
            if (this.conn != null) {
                this.conn.close();
                this.conn = null;
            }
            this.delegate.onDisconnect(reason);
        }
    }

    public void close() {
        close(DisconnectReason.OTHER);
    }

    public void sendRequest(Map<String, Object> message) {
        Map<String, Object> request = new HashMap<>();
        request.put("t", "d");
        request.put("d", message);
        sendData(request);
    }

    public void onMessage(Map<String, Object> message) {
        try {
            String messageType = (String) message.get("t");
            if (messageType == null) {
                if (this.logger.logsDebug()) {
                    this.logger.debug("Failed to parse server message: missing message type:" + message.toString());
                }
                close();
            } else if (messageType.equals("d")) {
                onDataMessage((Map) message.get("d"));
            } else if (messageType.equals(SERVER_CONTROL_MESSAGE)) {
                onControlMessage((Map) message.get("d"));
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring unknown server message type: " + messageType);
            }
        } catch (ClassCastException e) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Failed to parse server message: " + e.toString());
            }
            close();
        }
    }

    public void onDisconnect(boolean wasEverConnected) {
        this.conn = null;
        if (!wasEverConnected && this.state == State.REALTIME_CONNECTING) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Realtime connection failed");
            }
            if (this.repoInfo.isCacheableHost()) {
            }
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Realtime connection lost");
        }
        close();
    }

    private void onDataMessage(Map<String, Object> data) {
        if (this.logger.logsDebug()) {
            this.logger.debug("received data message: " + data.toString());
        }
        this.delegate.onDataMessage(data);
    }

    private void onControlMessage(Map<String, Object> data) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Got control message: " + data.toString());
        }
        try {
            String messageType = (String) data.get("t");
            if (messageType == null) {
                if (this.logger.logsDebug()) {
                    this.logger.debug("Got invalid control message: " + data.toString());
                }
                close();
            } else if (messageType.equals("s")) {
                onConnectionShutdown((String) data.get("d"));
            } else if (messageType.equals(SERVER_CONTROL_MESSAGE_RESET)) {
                onReset((String) data.get("d"));
            } else if (messageType.equals("h")) {
                onHandshake((Map) data.get("d"));
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring unknown control message: " + messageType);
            }
        } catch (ClassCastException e) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Failed to parse control message: " + e.toString());
            }
            close();
        }
    }

    private void onConnectionShutdown(String reason) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Connection shutdown command received. Shutting down...");
        }
        this.delegate.onKill(reason);
        close();
    }

    private void onHandshake(Map<String, Object> handshake) {
        long timestamp = ((Long) handshake.get(SERVER_HELLO_TIMESTAMP)).longValue();
        this.repoInfo.internalHost = (String) handshake.get("h");
        String sessionId = (String) handshake.get("s");
        if (this.state == State.REALTIME_CONNECTING) {
            this.conn.start();
            onConnectionReady(timestamp, sessionId);
        }
    }

    private void onConnectionReady(long timestamp, String sessionId) {
        if (this.logger.logsDebug()) {
            this.logger.debug("realtime connection established");
        }
        this.state = State.REALTIME_CONNECTED;
        this.delegate.onReady(timestamp, sessionId);
    }

    private void onReset(String host) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Got a reset; killing connection to " + this.repoInfo.internalHost + "; Updating internalHost to " + host);
        }
        this.repoInfo.internalHost = host;
        close(DisconnectReason.SERVER_RESET);
    }

    private void sendData(Map<String, Object> data) {
        if (this.state == State.REALTIME_CONNECTED) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Sending data: " + data.toString());
            }
            this.conn.send(data);
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Tried to send on an unconnected connection");
        }
    }
}
