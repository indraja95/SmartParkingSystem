package com.firebase.tubesock;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.shaded.apache.http.conn.ssl.StrictHostnameVerifier;
import org.shaded.apache.http.protocol.HTTP;

public class WebSocket {
    static final byte OPCODE_BINARY = 2;
    static final byte OPCODE_CLOSE = 8;
    static final byte OPCODE_NONE = 0;
    static final byte OPCODE_PING = 9;
    static final byte OPCODE_PONG = 10;
    static final byte OPCODE_TEXT = 1;
    private static final String THREAD_BASE_NAME = "TubeSock";
    private static final Charset UTF8 = Charset.forName(HTTP.UTF_8);
    private static final AtomicInteger clientCount = new AtomicInteger(0);
    private static ThreadInitializer intializer = new ThreadInitializer() {
        public void setName(Thread t, String name) {
            t.setName(name);
        }
    };
    private static ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private final int clientId;
    private WebSocketEventHandler eventHandler;
    private final WebSocketHandshake handshake;
    private final Thread innerThread;
    private final WebSocketReceiver receiver;
    private volatile Socket socket;
    private volatile State state;
    private final URI url;
    private final WebSocketWriter writer;

    private enum State {
        NONE,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    static ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    static ThreadInitializer getIntializer() {
        return intializer;
    }

    public static void setThreadFactory(ThreadFactory threadFactory2, ThreadInitializer intializer2) {
        threadFactory = threadFactory2;
        intializer = intializer2;
    }

    public WebSocket(URI url2) {
        this(url2, null);
    }

    public WebSocket(URI url2, String protocol) {
        this(url2, protocol, null);
    }

    public WebSocket(URI url2, String protocol, Map<String, String> extraHeaders) {
        this.state = State.NONE;
        this.socket = null;
        this.eventHandler = null;
        this.clientId = clientCount.incrementAndGet();
        this.innerThread = getThreadFactory().newThread(new Runnable() {
            public void run() {
                WebSocket.this.runReader();
            }
        });
        this.url = url2;
        this.handshake = new WebSocketHandshake(url2, protocol, extraHeaders);
        this.receiver = new WebSocketReceiver(this);
        this.writer = new WebSocketWriter(this, THREAD_BASE_NAME, this.clientId);
    }

    public void setEventHandler(WebSocketEventHandler eventHandler2) {
        this.eventHandler = eventHandler2;
    }

    /* access modifiers changed from: 0000 */
    public WebSocketEventHandler getEventHandler() {
        return this.eventHandler;
    }

    public synchronized void connect() {
        if (this.state != State.NONE) {
            this.eventHandler.onError(new WebSocketException("connect() already called"));
            close();
        } else {
            getIntializer().setName(getInnerThread(), "TubeSockReader-" + this.clientId);
            this.state = State.CONNECTING;
            getInnerThread().start();
        }
    }

    public synchronized void send(String data) {
        send(1, data.getBytes(UTF8));
    }

    public synchronized void send(byte[] data) {
        send(2, data);
    }

    /* access modifiers changed from: 0000 */
    public synchronized void pong(byte[] data) {
        send(10, data);
    }

    private synchronized void send(byte opcode, byte[] data) {
        if (this.state != State.CONNECTED) {
            this.eventHandler.onError(new WebSocketException("error while sending data: not connected"));
        } else {
            try {
                this.writer.send(opcode, true, data);
            } catch (IOException e) {
                this.eventHandler.onError(new WebSocketException("Failed to send frame", e));
                close();
            }
        }
        return;
    }

    /* access modifiers changed from: 0000 */
    public void handleReceiverError(WebSocketException e) {
        this.eventHandler.onError(e);
        if (this.state == State.CONNECTED) {
            close();
        }
        closeSocket();
    }

    public synchronized void close() {
        switch (this.state) {
            case NONE:
                this.state = State.DISCONNECTED;
                break;
            case CONNECTING:
                closeSocket();
                break;
            case CONNECTED:
                sendCloseHandshake();
                break;
        }
    }

    /* access modifiers changed from: 0000 */
    public void onCloseOpReceived() {
        closeSocket();
    }

    private synchronized void closeSocket() {
        if (this.state != State.DISCONNECTED) {
            this.receiver.stopit();
            this.writer.stopIt();
            if (this.socket != null) {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            this.state = State.DISCONNECTED;
            this.eventHandler.onClose();
        }
    }

    private void sendCloseHandshake() {
        try {
            this.state = State.DISCONNECTING;
            this.writer.stopIt();
            this.writer.send(8, true, new byte[0]);
        } catch (IOException e) {
            this.eventHandler.onError(new WebSocketException("Failed to send close frame", e));
        }
    }

    private Socket createSocket() {
        String scheme = this.url.getScheme();
        String host = this.url.getHost();
        int port = this.url.getPort();
        if (scheme != null && scheme.equals("ws")) {
            if (port == -1) {
                port = 80;
            }
            try {
                return new Socket(host, port);
            } catch (UnknownHostException uhe) {
                throw new WebSocketException("unknown host: " + host, uhe);
            } catch (IOException ioe) {
                throw new WebSocketException("error while creating socket to " + this.url, ioe);
            }
        } else if (scheme == null || !scheme.equals("wss")) {
            throw new WebSocketException("unsupported protocol: " + scheme);
        } else {
            if (port == -1) {
                port = 443;
            }
            try {
                Socket socket2 = SSLSocketFactory.getDefault().createSocket(host, port);
                verifyHost((SSLSocket) socket2, host);
                return socket2;
            } catch (UnknownHostException uhe2) {
                throw new WebSocketException("unknown host: " + host, uhe2);
            } catch (IOException ioe2) {
                throw new WebSocketException("error while creating secure socket to " + this.url, ioe2);
            }
        }
    }

    private void verifyHost(SSLSocket socket2, String host) throws SSLException {
        new StrictHostnameVerifier().verify(host, (X509Certificate) socket2.getSession().getPeerCertificates()[0]);
    }

    public void blockClose() throws InterruptedException {
        if (this.writer.getInnerThread().getState() != java.lang.Thread.State.NEW) {
            this.writer.getInnerThread().join();
        }
        getInnerThread().join();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r9 = new java.io.DataInputStream(r16.getInputStream());
        r14 = r16.getOutputStream();
        r14.write(r22.handshake.getHandshake());
        r5 = false;
        r3 = new byte[1000];
        r15 = 0;
        r6 = new java.util.ArrayList<>();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0077, code lost:
        if (r5 != false) goto L_0x012c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0079, code lost:
        r2 = r9.read();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0081, code lost:
        if (r2 != -1) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008a, code lost:
        throw new com.firebase.tubesock.WebSocketException("Connection closed before handshake was complete");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r3[r15] = (byte) r2;
        r15 = r15 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00cb, code lost:
        if (r3[r15 - 1] != 10) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d7, code lost:
        if (r3[r15 - 2] != 13) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d9, code lost:
        r13 = new java.lang.String(r3, UTF8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ec, code lost:
        if (r13.trim().equals("") == false) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ee, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00ef, code lost:
        r3 = new byte[1000];
        r15 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00f3, code lost:
        r6.add(r13.trim());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0106, code lost:
        if (r15 != 1000) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x012b, code lost:
        throw new com.firebase.tubesock.WebSocketException("Unexpected long line in handshake: " + new java.lang.String(r3, UTF8));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x012c, code lost:
        r22.handshake.verifyServerStatusLine((java.lang.String) r6.get(0));
        r6.remove(0);
        r7 = new java.util.HashMap<>();
        r8 = r6.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0157, code lost:
        if (r8.hasNext() == false) goto L_0x017b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0159, code lost:
        r11 = ((java.lang.String) r8.next()).split(": ", 2);
        r7.put(r11[0], r11[1]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x017b, code lost:
        r22.handshake.verifyServerHandshakeHeaders(r7);
        r22.writer.setOutput(r14);
        r22.receiver.setInput(r9);
        r22.state = com.firebase.tubesock.WebSocket.State.CONNECTED;
        r22.writer.getInnerThread().start();
        r22.eventHandler.onOpen();
        r22.receiver.run();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01c3, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        return;
     */
    public void runReader() {
        try {
            Socket socket2 = createSocket();
            synchronized (this) {
                this.socket = socket2;
                if (this.state == State.DISCONNECTED) {
                    try {
                        this.socket.close();
                        this.socket = null;
                        close();
                    } catch (IOException e) {
                        RuntimeException runtimeException = new RuntimeException(e);
                        throw runtimeException;
                    }
                }
            }
        } catch (WebSocketException wse) {
            try {
                this.eventHandler.onError(wse);
            } finally {
                close();
            }
        } catch (IOException ioe) {
            WebSocketEventHandler webSocketEventHandler = this.eventHandler;
            WebSocketException webSocketException = new WebSocketException("error while connecting: " + ioe.getMessage(), ioe);
            webSocketEventHandler.onError(webSocketException);
            close();
        }
    }

    /* access modifiers changed from: 0000 */
    public Thread getInnerThread() {
        return this.innerThread;
    }
}
