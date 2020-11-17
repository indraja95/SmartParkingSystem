package org.shaded.apache.http.impl.client;

import java.util.Map;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.auth.AUTH;
import org.shaded.apache.http.auth.MalformedChallengeException;
import org.shaded.apache.http.protocol.HttpContext;

@Immutable
public class DefaultProxyAuthenticationHandler extends AbstractAuthenticationHandler {
    public boolean isAuthenticationRequested(HttpResponse response, HttpContext context) {
        if (response != null) {
            return response.getStatusLine().getStatusCode() == 407;
        }
        throw new IllegalArgumentException("HTTP response may not be null");
    }

    public Map<String, Header> getChallenges(HttpResponse response, HttpContext context) throws MalformedChallengeException {
        if (response != null) {
            return parseChallenges(response.getHeaders(AUTH.PROXY_AUTH));
        }
        throw new IllegalArgumentException("HTTP response may not be null");
    }
}
