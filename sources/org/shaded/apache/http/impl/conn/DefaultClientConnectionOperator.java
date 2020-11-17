package org.shaded.apache.http.impl.conn;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import org.shaded.apache.http.HttpHost;
import org.shaded.apache.http.annotation.ThreadSafe;
import org.shaded.apache.http.conn.ClientConnectionOperator;
import org.shaded.apache.http.conn.HttpHostConnectException;
import org.shaded.apache.http.conn.OperatedClientConnection;
import org.shaded.apache.http.conn.scheme.LayeredSocketFactory;
import org.shaded.apache.http.conn.scheme.Scheme;
import org.shaded.apache.http.conn.scheme.SchemeRegistry;
import org.shaded.apache.http.conn.scheme.SocketFactory;
import org.shaded.apache.http.params.HttpConnectionParams;
import org.shaded.apache.http.params.HttpParams;
import org.shaded.apache.http.protocol.HttpContext;

@ThreadSafe
public class DefaultClientConnectionOperator implements ClientConnectionOperator {
    protected final SchemeRegistry schemeRegistry;

    public DefaultClientConnectionOperator(SchemeRegistry schemes) {
        if (schemes == null) {
            throw new IllegalArgumentException("Scheme registry must not be null.");
        }
        this.schemeRegistry = schemes;
    }

    public OperatedClientConnection createConnection() {
        return new DefaultClientConnection();
    }

    public void openConnection(OperatedClientConnection conn, HttpHost target, InetAddress local, HttpContext context, HttpParams params) throws IOException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection must not be null.");
        } else if (target == null) {
            throw new IllegalArgumentException("Target host must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (conn.isOpen()) {
            throw new IllegalArgumentException("Connection must not be open.");
        } else {
            Scheme schm = this.schemeRegistry.getScheme(target.getSchemeName());
            SocketFactory sf = schm.getSocketFactory();
            Socket sock = sf.createSocket();
            conn.opening(sock, target);
            try {
                Socket sock2 = sf.connectSocket(sock, target.getHostName(), schm.resolvePort(target.getPort()), local, 0, params);
                prepareSocket(sock2, context, params);
                conn.openCompleted(sf.isSecure(sock2), params);
            } catch (ConnectException ex) {
                throw new HttpHostConnectException(target, ex);
            }
        }
    }

    public void updateSecureConnection(OperatedClientConnection conn, HttpHost target, HttpContext context, HttpParams params) throws IOException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection must not be null.");
        } else if (target == null) {
            throw new IllegalArgumentException("Target host must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (!conn.isOpen()) {
            throw new IllegalArgumentException("Connection must be open.");
        } else {
            Scheme schm = this.schemeRegistry.getScheme(target.getSchemeName());
            if (!(schm.getSocketFactory() instanceof LayeredSocketFactory)) {
                throw new IllegalArgumentException("Target scheme (" + schm.getName() + ") must have layered socket factory.");
            }
            LayeredSocketFactory lsf = (LayeredSocketFactory) schm.getSocketFactory();
            try {
                Socket sock = lsf.createSocket(conn.getSocket(), target.getHostName(), target.getPort(), true);
                prepareSocket(sock, context, params);
                conn.update(sock, target, lsf.isSecure(sock), params);
            } catch (ConnectException ex) {
                throw new HttpHostConnectException(target, ex);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void prepareSocket(Socket sock, HttpContext context, HttpParams params) throws IOException {
        sock.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
        sock.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
        int linger = HttpConnectionParams.getLinger(params);
        if (linger >= 0) {
            sock.setSoLinger(linger > 0, linger);
        }
    }
}
