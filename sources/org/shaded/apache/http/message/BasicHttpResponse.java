package org.shaded.apache.http.message;

import java.util.Locale;
import org.shaded.apache.http.HttpEntity;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.ProtocolVersion;
import org.shaded.apache.http.ReasonPhraseCatalog;
import org.shaded.apache.http.StatusLine;

public class BasicHttpResponse extends AbstractHttpMessage implements HttpResponse {
    private HttpEntity entity;
    private Locale locale;
    private ReasonPhraseCatalog reasonCatalog;
    private StatusLine statusline;

    public BasicHttpResponse(StatusLine statusline2, ReasonPhraseCatalog catalog, Locale locale2) {
        if (statusline2 == null) {
            throw new IllegalArgumentException("Status line may not be null.");
        }
        this.statusline = statusline2;
        this.reasonCatalog = catalog;
        if (locale2 == null) {
            locale2 = Locale.getDefault();
        }
        this.locale = locale2;
    }

    public BasicHttpResponse(StatusLine statusline2) {
        this(statusline2, (ReasonPhraseCatalog) null, (Locale) null);
    }

    public BasicHttpResponse(ProtocolVersion ver, int code, String reason) {
        this((StatusLine) new BasicStatusLine(ver, code, reason), (ReasonPhraseCatalog) null, (Locale) null);
    }

    public ProtocolVersion getProtocolVersion() {
        return this.statusline.getProtocolVersion();
    }

    public StatusLine getStatusLine() {
        return this.statusline;
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setStatusLine(StatusLine statusline2) {
        if (statusline2 == null) {
            throw new IllegalArgumentException("Status line may not be null");
        }
        this.statusline = statusline2;
    }

    public void setStatusLine(ProtocolVersion ver, int code) {
        this.statusline = new BasicStatusLine(ver, code, getReason(code));
    }

    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        this.statusline = new BasicStatusLine(ver, code, reason);
    }

    public void setStatusCode(int code) {
        this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), code, getReason(code));
    }

    public void setReasonPhrase(String reason) {
        if (reason == null || (reason.indexOf(10) < 0 && reason.indexOf(13) < 0)) {
            this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), this.statusline.getStatusCode(), reason);
            return;
        }
        throw new IllegalArgumentException("Line break in reason phrase.");
    }

    public void setEntity(HttpEntity entity2) {
        this.entity = entity2;
    }

    public void setLocale(Locale loc) {
        if (loc == null) {
            throw new IllegalArgumentException("Locale may not be null.");
        }
        this.locale = loc;
        int code = this.statusline.getStatusCode();
        this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), code, getReason(code));
    }

    /* access modifiers changed from: protected */
    public String getReason(int code) {
        if (this.reasonCatalog == null) {
            return null;
        }
        return this.reasonCatalog.getReason(code, this.locale);
    }
}
