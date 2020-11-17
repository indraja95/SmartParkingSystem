package org.shaded.apache.http.client.protocol;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.shaded.apache.commons.logging.Log;
import org.shaded.apache.commons.logging.LogFactory;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpHost;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.HttpRequestInterceptor;
import org.shaded.apache.http.ProtocolException;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.client.CookieStore;
import org.shaded.apache.http.client.methods.HttpUriRequest;
import org.shaded.apache.http.client.params.HttpClientParams;
import org.shaded.apache.http.conn.ManagedClientConnection;
import org.shaded.apache.http.conn.scheme.SchemeRegistry;
import org.shaded.apache.http.cookie.Cookie;
import org.shaded.apache.http.cookie.CookieOrigin;
import org.shaded.apache.http.cookie.CookieSpec;
import org.shaded.apache.http.cookie.CookieSpecRegistry;
import org.shaded.apache.http.cookie.SetCookie2;
import org.shaded.apache.http.protocol.ExecutionContext;
import org.shaded.apache.http.protocol.HttpContext;

@Immutable
public class RequestAddCookies implements HttpRequestInterceptor {
    private final Log log = LogFactory.getLog(getClass());

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        URI requestURI;
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else if (!request.getRequestLine().getMethod().equalsIgnoreCase("CONNECT")) {
            CookieStore cookieStore = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
            if (cookieStore == null) {
                this.log.info("Cookie store not available in HTTP context");
                return;
            }
            CookieSpecRegistry registry = (CookieSpecRegistry) context.getAttribute(ClientContext.COOKIESPEC_REGISTRY);
            if (registry == null) {
                this.log.info("CookieSpec registry not available in HTTP context");
                return;
            }
            HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            if (targetHost == null) {
                throw new IllegalStateException("Target host not specified in HTTP context");
            }
            ManagedClientConnection conn = (ManagedClientConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
            if (conn == null) {
                throw new IllegalStateException("Client connection not specified in HTTP context");
            }
            String policy = HttpClientParams.getCookiePolicy(request.getParams());
            if (this.log.isDebugEnabled()) {
                this.log.debug("CookieSpec selected: " + policy);
            }
            if (request instanceof HttpUriRequest) {
                requestURI = ((HttpUriRequest) request).getURI();
            } else {
                try {
                    requestURI = new URI(request.getRequestLine().getUri());
                } catch (URISyntaxException ex) {
                    ProtocolException protocolException = new ProtocolException("Invalid request URI: " + request.getRequestLine().getUri(), ex);
                    throw protocolException;
                }
            }
            String hostName = targetHost.getHostName();
            int port = targetHost.getPort();
            if (port < 0) {
                SchemeRegistry sr = (SchemeRegistry) context.getAttribute(ClientContext.SCHEME_REGISTRY);
                if (sr != null) {
                    port = sr.get(targetHost.getSchemeName()).resolvePort(port);
                } else {
                    port = conn.getRemotePort();
                }
            }
            CookieOrigin cookieOrigin = new CookieOrigin(hostName, port, requestURI.getPath(), conn.isSecure());
            CookieSpec cookieSpec = registry.getCookieSpec(policy, request.getParams());
            List<Cookie> cookies = new ArrayList<>(cookieStore.getCookies());
            List<Cookie> matchedCookies = new ArrayList<>();
            Date now = new Date();
            for (Cookie cookie : cookies) {
                if (!cookie.isExpired(now)) {
                    if (cookieSpec.match(cookie, cookieOrigin)) {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("Cookie " + cookie + " match " + cookieOrigin);
                        }
                        matchedCookies.add(cookie);
                    }
                } else if (this.log.isDebugEnabled()) {
                    this.log.debug("Cookie " + cookie + " expired");
                }
            }
            if (!matchedCookies.isEmpty()) {
                for (Header header : cookieSpec.formatCookies(matchedCookies)) {
                    request.addHeader(header);
                }
            }
            int ver = cookieSpec.getVersion();
            if (ver > 0) {
                boolean needVersionHeader = false;
                for (Cookie cookie2 : matchedCookies) {
                    if (ver != cookie2.getVersion() || !(cookie2 instanceof SetCookie2)) {
                        needVersionHeader = true;
                    }
                }
                if (needVersionHeader) {
                    Header header2 = cookieSpec.getVersionHeader();
                    if (header2 != null) {
                        request.addHeader(header2);
                    }
                }
            }
            context.setAttribute(ClientContext.COOKIE_SPEC, cookieSpec);
            context.setAttribute(ClientContext.COOKIE_ORIGIN, cookieOrigin);
        }
    }
}
