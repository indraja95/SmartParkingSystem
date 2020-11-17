package org.shaded.apache.http.impl.entity;

import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpMessage;
import org.shaded.apache.http.HttpVersion;
import org.shaded.apache.http.ProtocolException;
import org.shaded.apache.http.entity.ContentLengthStrategy;
import org.shaded.apache.http.protocol.HTTP;

public class StrictContentLengthStrategy implements ContentLengthStrategy {
    public long determineLength(HttpMessage message) throws HttpException {
        if (message == null) {
            throw new IllegalArgumentException("HTTP message may not be null");
        }
        Header transferEncodingHeader = message.getFirstHeader(HTTP.TRANSFER_ENCODING);
        Header contentLengthHeader = message.getFirstHeader(HTTP.CONTENT_LEN);
        if (transferEncodingHeader != null) {
            String s = transferEncodingHeader.getValue();
            if (HTTP.CHUNK_CODING.equalsIgnoreCase(s)) {
                if (!message.getProtocolVersion().lessEquals(HttpVersion.HTTP_1_0)) {
                    return -2;
                }
                throw new ProtocolException(new StringBuffer().append("Chunked transfer encoding not allowed for ").append(message.getProtocolVersion()).toString());
            } else if (HTTP.IDENTITY_CODING.equalsIgnoreCase(s)) {
                return -1;
            } else {
                throw new ProtocolException(new StringBuffer().append("Unsupported transfer encoding: ").append(s).toString());
            }
        } else if (contentLengthHeader == null) {
            return -1;
        } else {
            String s2 = contentLengthHeader.getValue();
            try {
                return Long.parseLong(s2);
            } catch (NumberFormatException e) {
                throw new ProtocolException(new StringBuffer().append("Invalid content length: ").append(s2).toString());
            }
        }
    }
}
