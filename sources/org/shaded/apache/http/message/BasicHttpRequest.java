package org.shaded.apache.http.message;

import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.ProtocolVersion;
import org.shaded.apache.http.RequestLine;
import org.shaded.apache.http.params.HttpProtocolParams;

public class BasicHttpRequest extends AbstractHttpMessage implements HttpRequest {
    private final String method;
    private RequestLine requestline;
    private final String uri;

    public BasicHttpRequest(String method2, String uri2) {
        if (method2 == null) {
            throw new IllegalArgumentException("Method name may not be null");
        } else if (uri2 == null) {
            throw new IllegalArgumentException("Request URI may not be null");
        } else {
            this.method = method2;
            this.uri = uri2;
            this.requestline = null;
        }
    }

    public BasicHttpRequest(String method2, String uri2, ProtocolVersion ver) {
        this(new BasicRequestLine(method2, uri2, ver));
    }

    public BasicHttpRequest(RequestLine requestline2) {
        if (requestline2 == null) {
            throw new IllegalArgumentException("Request line may not be null");
        }
        this.requestline = requestline2;
        this.method = requestline2.getMethod();
        this.uri = requestline2.getUri();
    }

    public ProtocolVersion getProtocolVersion() {
        return getRequestLine().getProtocolVersion();
    }

    public RequestLine getRequestLine() {
        if (this.requestline == null) {
            this.requestline = new BasicRequestLine(this.method, this.uri, HttpProtocolParams.getVersion(getParams()));
        }
        return this.requestline;
    }
}
