package org.shaded.apache.http.impl.conn;

import java.io.IOException;
import org.shaded.apache.http.HttpHost;
import org.shaded.apache.http.conn.ClientConnectionManager;
import org.shaded.apache.http.conn.OperatedClientConnection;
import org.shaded.apache.http.conn.routing.HttpRoute;
import org.shaded.apache.http.params.HttpParams;
import org.shaded.apache.http.protocol.HttpContext;

public abstract class AbstractPooledConnAdapter extends AbstractClientConnAdapter {
    protected volatile AbstractPoolEntry poolEntry;

    protected AbstractPooledConnAdapter(ClientConnectionManager manager, AbstractPoolEntry entry) {
        super(manager, entry.connection);
        this.poolEntry = entry;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public final void assertAttached() {
        if (this.poolEntry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void detach() {
        super.detach();
        this.poolEntry = null;
    }

    public HttpRoute getRoute() {
        AbstractPoolEntry entry = this.poolEntry;
        if (entry == null) {
            throw new IllegalStateException("Adapter is detached.");
        } else if (entry.tracker == null) {
            return null;
        } else {
            return entry.tracker.toRoute();
        }
    }

    public void open(HttpRoute route, HttpContext context, HttpParams params) throws IOException {
        assertNotAborted();
        AbstractPoolEntry entry = this.poolEntry;
        if (entry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
        entry.open(route, context, params);
    }

    public void tunnelTarget(boolean secure, HttpParams params) throws IOException {
        assertNotAborted();
        AbstractPoolEntry entry = this.poolEntry;
        if (entry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
        entry.tunnelTarget(secure, params);
    }

    public void tunnelProxy(HttpHost next, boolean secure, HttpParams params) throws IOException {
        assertNotAborted();
        AbstractPoolEntry entry = this.poolEntry;
        if (entry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
        entry.tunnelProxy(next, secure, params);
    }

    public void layerProtocol(HttpContext context, HttpParams params) throws IOException {
        assertNotAborted();
        AbstractPoolEntry entry = this.poolEntry;
        if (entry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
        entry.layerProtocol(context, params);
    }

    public void close() throws IOException {
        AbstractPoolEntry entry = this.poolEntry;
        if (entry != null) {
            entry.shutdownEntry();
        }
        OperatedClientConnection conn = getWrappedConnection();
        if (conn != null) {
            conn.close();
        }
    }

    public void shutdown() throws IOException {
        AbstractPoolEntry entry = this.poolEntry;
        if (entry != null) {
            entry.shutdownEntry();
        }
        OperatedClientConnection conn = getWrappedConnection();
        if (conn != null) {
            conn.shutdown();
        }
    }

    public Object getState() {
        AbstractPoolEntry entry = this.poolEntry;
        if (entry != null) {
            return entry.getState();
        }
        throw new IllegalStateException("Adapter is detached.");
    }

    public void setState(Object state) {
        AbstractPoolEntry entry = this.poolEntry;
        if (entry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
        entry.setState(state);
    }
}
