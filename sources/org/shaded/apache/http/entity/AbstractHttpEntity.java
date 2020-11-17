package org.shaded.apache.http.entity;

import java.io.IOException;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpEntity;
import org.shaded.apache.http.message.BasicHeader;
import org.shaded.apache.http.protocol.HTTP;

public abstract class AbstractHttpEntity implements HttpEntity {
    protected boolean chunked;
    protected Header contentEncoding;
    protected Header contentType;

    protected AbstractHttpEntity() {
    }

    public Header getContentType() {
        return this.contentType;
    }

    public Header getContentEncoding() {
        return this.contentEncoding;
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public void setContentType(Header contentType2) {
        this.contentType = contentType2;
    }

    public void setContentType(String ctString) {
        Header h = null;
        if (ctString != null) {
            h = new BasicHeader(HTTP.CONTENT_TYPE, ctString);
        }
        setContentType(h);
    }

    public void setContentEncoding(Header contentEncoding2) {
        this.contentEncoding = contentEncoding2;
    }

    public void setContentEncoding(String ceString) {
        Header h = null;
        if (ceString != null) {
            h = new BasicHeader(HTTP.CONTENT_ENCODING, ceString);
        }
        setContentEncoding(h);
    }

    public void setChunked(boolean b) {
        this.chunked = b;
    }

    public void consumeContent() throws IOException, UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException("streaming entity does not implement consumeContent()");
        }
    }
}
