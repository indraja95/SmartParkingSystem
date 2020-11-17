package org.shaded.apache.http.impl.conn.tsccm;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.shaded.apache.commons.logging.Log;
import org.shaded.apache.commons.logging.LogFactory;
import org.shaded.apache.http.annotation.GuardedBy;
import org.shaded.apache.http.annotation.ThreadSafe;
import org.shaded.apache.http.conn.ConnectionPoolTimeoutException;
import org.shaded.apache.http.conn.OperatedClientConnection;
import org.shaded.apache.http.conn.routing.HttpRoute;
import org.shaded.apache.http.impl.conn.IdleConnectionHandler;

@ThreadSafe
public abstract class AbstractConnPool implements RefQueueHandler {
    @GuardedBy("poolLock")
    protected IdleConnectionHandler idleConnHandler = new IdleConnectionHandler();
    protected volatile boolean isShutDown;
    @Deprecated
    protected Set<BasicPoolEntryRef> issuedConnections;
    @GuardedBy("poolLock")
    protected Set<BasicPoolEntry> leasedConnections = new HashSet();
    private final Log log = LogFactory.getLog(getClass());
    @GuardedBy("poolLock")
    protected int numConnections;
    protected final Lock poolLock = new ReentrantLock(false);
    @Deprecated
    protected ReferenceQueue<Object> refQueue;

    public abstract void deleteClosedConnections();

    public abstract void freeEntry(BasicPoolEntry basicPoolEntry, boolean z, long j, TimeUnit timeUnit);

    /* access modifiers changed from: protected */
    @Deprecated
    public abstract void handleLostEntry(HttpRoute httpRoute);

    public abstract PoolEntryRequest requestPoolEntry(HttpRoute httpRoute, Object obj);

    protected AbstractConnPool() {
    }

    @Deprecated
    public void enableConnectionGC() throws IllegalStateException {
    }

    public final BasicPoolEntry getEntry(HttpRoute route, Object state, long timeout, TimeUnit tunit) throws ConnectionPoolTimeoutException, InterruptedException {
        return requestPoolEntry(route, state).getPoolEntry(timeout, tunit);
    }

    @Deprecated
    public void handleReference(Reference<?> reference) {
    }

    public void closeIdleConnections(long idletime, TimeUnit tunit) {
        if (tunit == null) {
            throw new IllegalArgumentException("Time unit must not be null.");
        }
        this.poolLock.lock();
        try {
            this.idleConnHandler.closeIdleConnections(tunit.toMillis(idletime));
        } finally {
            this.poolLock.unlock();
        }
    }

    public void closeExpiredConnections() {
        this.poolLock.lock();
        try {
            this.idleConnHandler.closeExpiredConnections();
        } finally {
            this.poolLock.unlock();
        }
    }

    public void shutdown() {
        this.poolLock.lock();
        try {
            if (!this.isShutDown) {
                Iterator<BasicPoolEntry> iter = this.leasedConnections.iterator();
                while (iter.hasNext()) {
                    BasicPoolEntry entry = (BasicPoolEntry) iter.next();
                    iter.remove();
                    closeConnection(entry.getConnection());
                }
                this.idleConnHandler.removeAll();
                this.isShutDown = true;
                this.poolLock.unlock();
            }
        } finally {
            this.poolLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public void closeConnection(OperatedClientConnection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (IOException ex) {
                this.log.debug("I/O error closing connection", ex);
            }
        }
    }
}
