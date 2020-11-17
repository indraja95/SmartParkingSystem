package org.shaded.apache.http.protocol;

import java.io.IOException;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpEntity;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.HttpResponseInterceptor;
import org.shaded.apache.http.HttpVersion;
import org.shaded.apache.http.ProtocolVersion;

public class ResponseConnControl implements HttpResponseInterceptor {
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else {
            int status = response.getStatusLine().getStatusCode();
            if (status == 400 || status == 408 || status == 411 || status == 413 || status == 414 || status == 503 || status == 501) {
                response.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
                return;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                ProtocolVersion ver = response.getStatusLine().getProtocolVersion();
                if (entity.getContentLength() < 0 && (!entity.isChunked() || ver.lessEquals(HttpVersion.HTTP_1_0))) {
                    response.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
                    return;
                }
            }
            HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
            if (request != null) {
                Header header = request.getFirstHeader(HTTP.CONN_DIRECTIVE);
                if (header != null) {
                    response.setHeader(HTTP.CONN_DIRECTIVE, header.getValue());
                }
            }
        }
    }
}
