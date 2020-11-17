package org.shaded.apache.http.impl.entity;

import org.shaded.apache.http.Header;
import org.shaded.apache.http.HeaderElement;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpMessage;
import org.shaded.apache.http.ParseException;
import org.shaded.apache.http.ProtocolException;
import org.shaded.apache.http.entity.ContentLengthStrategy;
import org.shaded.apache.http.params.CoreProtocolPNames;
import org.shaded.apache.http.protocol.HTTP;

public class LaxContentLengthStrategy implements ContentLengthStrategy {
    public long determineLength(HttpMessage message) throws HttpException {
        if (message == null) {
            throw new IllegalArgumentException("HTTP message may not be null");
        }
        boolean strict = message.getParams().isParameterTrue(CoreProtocolPNames.STRICT_TRANSFER_ENCODING);
        Header transferEncodingHeader = message.getFirstHeader(HTTP.TRANSFER_ENCODING);
        Header contentLengthHeader = message.getFirstHeader(HTTP.CONTENT_LEN);
        if (transferEncodingHeader != null) {
            try {
                HeaderElement[] encodings = transferEncodingHeader.getElements();
                if (strict) {
                    int i = 0;
                    while (i < encodings.length) {
                        String encoding = encodings[i].getName();
                        if (encoding == null || encoding.length() <= 0 || encoding.equalsIgnoreCase(HTTP.CHUNK_CODING) || encoding.equalsIgnoreCase(HTTP.IDENTITY_CODING)) {
                            i++;
                        } else {
                            throw new ProtocolException(new StringBuffer().append("Unsupported transfer encoding: ").append(encoding).toString());
                        }
                    }
                }
                int len = encodings.length;
                if (HTTP.IDENTITY_CODING.equalsIgnoreCase(transferEncodingHeader.getValue())) {
                    return -1;
                }
                if (len > 0 && HTTP.CHUNK_CODING.equalsIgnoreCase(encodings[len - 1].getName())) {
                    return -2;
                }
                if (!strict) {
                    return -1;
                }
                throw new ProtocolException("Chunk-encoding must be the last one applied");
            } catch (ParseException px) {
                ProtocolException protocolException = new ProtocolException(new StringBuffer().append("Invalid Transfer-Encoding header value: ").append(transferEncodingHeader).toString(), px);
                throw protocolException;
            }
        } else if (contentLengthHeader == null) {
            return -1;
        } else {
            long contentlen = -1;
            Header[] headers = message.getHeaders(HTTP.CONTENT_LEN);
            if (!strict || headers.length <= 1) {
                int i2 = headers.length - 1;
                while (i2 >= 0) {
                    Header header = headers[i2];
                    try {
                        contentlen = Long.parseLong(header.getValue());
                        break;
                    } catch (NumberFormatException e) {
                        if (strict) {
                            throw new ProtocolException(new StringBuffer().append("Invalid content length: ").append(header.getValue()).toString());
                        }
                        i2--;
                    }
                }
                if (contentlen < 0) {
                    return -1;
                }
                return contentlen;
            }
            throw new ProtocolException("Multiple content length headers");
        }
    }
}
