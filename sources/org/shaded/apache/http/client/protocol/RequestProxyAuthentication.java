package org.shaded.apache.http.client.protocol;

import java.io.IOException;
import org.shaded.apache.commons.logging.Log;
import org.shaded.apache.commons.logging.LogFactory;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.HttpRequestInterceptor;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.auth.AUTH;
import org.shaded.apache.http.auth.AuthScheme;
import org.shaded.apache.http.auth.AuthState;
import org.shaded.apache.http.auth.AuthenticationException;
import org.shaded.apache.http.auth.Credentials;
import org.shaded.apache.http.protocol.HttpContext;

@Immutable
public class RequestProxyAuthentication implements HttpRequestInterceptor {
    private final Log log = LogFactory.getLog(getClass());

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else if (!request.containsHeader(AUTH.PROXY_AUTH_RESP)) {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.PROXY_AUTH_STATE);
            if (authState != null) {
                AuthScheme authScheme = authState.getAuthScheme();
                if (authScheme != null) {
                    Credentials creds = authState.getCredentials();
                    if (creds == null) {
                        this.log.debug("User credentials not available");
                    } else if (authState.getAuthScope() != null || !authScheme.isConnectionBased()) {
                        try {
                            request.addHeader(authScheme.authenticate(creds, request));
                        } catch (AuthenticationException ex) {
                            if (this.log.isErrorEnabled()) {
                                this.log.error("Proxy authentication error: " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }
}
