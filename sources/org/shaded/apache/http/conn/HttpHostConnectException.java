package org.shaded.apache.http.conn;

import java.net.ConnectException;
import org.shaded.apache.http.HttpHost;
import org.shaded.apache.http.annotation.Immutable;

@Immutable
public class HttpHostConnectException extends ConnectException {
    private static final long serialVersionUID = -3194482710275220224L;
    private final HttpHost host;

    public HttpHostConnectException(HttpHost host2, ConnectException cause) {
        super("Connection to " + host2 + " refused");
        this.host = host2;
        initCause(cause);
    }

    public HttpHost getHost() {
        return this.host;
    }
}
