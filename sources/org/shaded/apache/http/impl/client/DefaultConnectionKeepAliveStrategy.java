package org.shaded.apache.http.impl.client;

import org.shaded.apache.http.HeaderElement;
import org.shaded.apache.http.HeaderElementIterator;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.conn.ConnectionKeepAliveStrategy;
import org.shaded.apache.http.message.BasicHeaderElementIterator;
import org.shaded.apache.http.protocol.HTTP;
import org.shaded.apache.http.protocol.HttpContext;

@Immutable
public class DefaultConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && param.equalsIgnoreCase("timeout")) {
                try {
                    return Long.parseLong(value) * 1000;
                } catch (NumberFormatException e) {
                }
            }
        }
        return -1;
    }
}
