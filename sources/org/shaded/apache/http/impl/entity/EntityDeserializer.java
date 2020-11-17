package org.shaded.apache.http.impl.entity;

import java.io.IOException;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpEntity;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpMessage;
import org.shaded.apache.http.entity.BasicHttpEntity;
import org.shaded.apache.http.entity.ContentLengthStrategy;
import org.shaded.apache.http.impl.io.ChunkedInputStream;
import org.shaded.apache.http.impl.io.ContentLengthInputStream;
import org.shaded.apache.http.impl.io.IdentityInputStream;
import org.shaded.apache.http.io.SessionInputBuffer;
import org.shaded.apache.http.protocol.HTTP;

public class EntityDeserializer {
    private final ContentLengthStrategy lenStrategy;

    public EntityDeserializer(ContentLengthStrategy lenStrategy2) {
        if (lenStrategy2 == null) {
            throw new IllegalArgumentException("Content length strategy may not be null");
        }
        this.lenStrategy = lenStrategy2;
    }

    /* access modifiers changed from: protected */
    public BasicHttpEntity doDeserialize(SessionInputBuffer inbuffer, HttpMessage message) throws HttpException, IOException {
        BasicHttpEntity entity = new BasicHttpEntity();
        long len = this.lenStrategy.determineLength(message);
        if (len == -2) {
            entity.setChunked(true);
            entity.setContentLength(-1);
            entity.setContent(new ChunkedInputStream(inbuffer));
        } else if (len == -1) {
            entity.setChunked(false);
            entity.setContentLength(-1);
            entity.setContent(new IdentityInputStream(inbuffer));
        } else {
            entity.setChunked(false);
            entity.setContentLength(len);
            entity.setContent(new ContentLengthInputStream(inbuffer, len));
        }
        Header contentTypeHeader = message.getFirstHeader(HTTP.CONTENT_TYPE);
        if (contentTypeHeader != null) {
            entity.setContentType(contentTypeHeader);
        }
        Header contentEncodingHeader = message.getFirstHeader(HTTP.CONTENT_ENCODING);
        if (contentEncodingHeader != null) {
            entity.setContentEncoding(contentEncodingHeader);
        }
        return entity;
    }

    public HttpEntity deserialize(SessionInputBuffer inbuffer, HttpMessage message) throws HttpException, IOException {
        if (inbuffer == null) {
            throw new IllegalArgumentException("Session input buffer may not be null");
        } else if (message != null) {
            return doDeserialize(inbuffer, message);
        } else {
            throw new IllegalArgumentException("HTTP message may not be null");
        }
    }
}
