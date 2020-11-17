package org.shaded.apache.http.impl;

import java.util.Locale;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.HttpResponseFactory;
import org.shaded.apache.http.ProtocolVersion;
import org.shaded.apache.http.ReasonPhraseCatalog;
import org.shaded.apache.http.StatusLine;
import org.shaded.apache.http.message.BasicHttpResponse;
import org.shaded.apache.http.message.BasicStatusLine;
import org.shaded.apache.http.protocol.HttpContext;

public class DefaultHttpResponseFactory implements HttpResponseFactory {
    protected final ReasonPhraseCatalog reasonCatalog;

    public DefaultHttpResponseFactory(ReasonPhraseCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Reason phrase catalog must not be null.");
        }
        this.reasonCatalog = catalog;
    }

    public DefaultHttpResponseFactory() {
        this(EnglishReasonPhraseCatalog.INSTANCE);
    }

    public HttpResponse newHttpResponse(ProtocolVersion ver, int status, HttpContext context) {
        if (ver == null) {
            throw new IllegalArgumentException("HTTP version may not be null");
        }
        Locale loc = determineLocale(context);
        return new BasicHttpResponse(new BasicStatusLine(ver, status, this.reasonCatalog.getReason(status, loc)), this.reasonCatalog, loc);
    }

    public HttpResponse newHttpResponse(StatusLine statusline, HttpContext context) {
        if (statusline == null) {
            throw new IllegalArgumentException("Status line may not be null");
        }
        return new BasicHttpResponse(statusline, this.reasonCatalog, determineLocale(context));
    }

    /* access modifiers changed from: protected */
    public Locale determineLocale(HttpContext context) {
        return Locale.getDefault();
    }
}
