package org.shaded.apache.http.protocol;

import java.io.IOException;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.HttpRequestInterceptor;
import org.shaded.apache.http.params.HttpProtocolParams;

public class RequestUserAgent implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (!request.containsHeader(HTTP.USER_AGENT)) {
            String useragent = HttpProtocolParams.getUserAgent(request.getParams());
            if (useragent != null) {
                request.addHeader(HTTP.USER_AGENT, useragent);
            }
        }
    }
}
