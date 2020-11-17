package org.shaded.apache.http.params;

public abstract class HttpAbstractParamBean {
    protected final HttpParams params;

    public HttpAbstractParamBean(HttpParams params2) {
        if (params2 == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.params = params2;
    }
}
