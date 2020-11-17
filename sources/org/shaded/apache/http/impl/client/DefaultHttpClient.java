package org.shaded.apache.http.impl.client;

import org.shaded.apache.http.ConnectionReuseStrategy;
import org.shaded.apache.http.HttpHost;
import org.shaded.apache.http.HttpRequestInterceptor;
import org.shaded.apache.http.HttpResponseInterceptor;
import org.shaded.apache.http.HttpVersion;
import org.shaded.apache.http.annotation.ThreadSafe;
import org.shaded.apache.http.auth.AuthSchemeRegistry;
import org.shaded.apache.http.client.AuthenticationHandler;
import org.shaded.apache.http.client.CookieStore;
import org.shaded.apache.http.client.CredentialsProvider;
import org.shaded.apache.http.client.HttpRequestRetryHandler;
import org.shaded.apache.http.client.RedirectHandler;
import org.shaded.apache.http.client.UserTokenHandler;
import org.shaded.apache.http.client.params.AuthPolicy;
import org.shaded.apache.http.client.params.ClientPNames;
import org.shaded.apache.http.client.params.CookiePolicy;
import org.shaded.apache.http.client.protocol.ClientContext;
import org.shaded.apache.http.client.protocol.RequestAddCookies;
import org.shaded.apache.http.client.protocol.RequestClientConnControl;
import org.shaded.apache.http.client.protocol.RequestDefaultHeaders;
import org.shaded.apache.http.client.protocol.RequestProxyAuthentication;
import org.shaded.apache.http.client.protocol.RequestTargetAuthentication;
import org.shaded.apache.http.client.protocol.ResponseProcessCookies;
import org.shaded.apache.http.conn.ClientConnectionManager;
import org.shaded.apache.http.conn.ClientConnectionManagerFactory;
import org.shaded.apache.http.conn.ConnectionKeepAliveStrategy;
import org.shaded.apache.http.conn.routing.HttpRoutePlanner;
import org.shaded.apache.http.conn.scheme.PlainSocketFactory;
import org.shaded.apache.http.conn.scheme.Scheme;
import org.shaded.apache.http.conn.scheme.SchemeRegistry;
import org.shaded.apache.http.conn.ssl.SSLSocketFactory;
import org.shaded.apache.http.cookie.CookieSpecRegistry;
import org.shaded.apache.http.impl.DefaultConnectionReuseStrategy;
import org.shaded.apache.http.impl.auth.BasicSchemeFactory;
import org.shaded.apache.http.impl.auth.DigestSchemeFactory;
import org.shaded.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.shaded.apache.http.impl.conn.SingleClientConnManager;
import org.shaded.apache.http.impl.cookie.BestMatchSpecFactory;
import org.shaded.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.shaded.apache.http.impl.cookie.NetscapeDraftSpecFactory;
import org.shaded.apache.http.impl.cookie.RFC2109SpecFactory;
import org.shaded.apache.http.impl.cookie.RFC2965SpecFactory;
import org.shaded.apache.http.params.BasicHttpParams;
import org.shaded.apache.http.params.HttpConnectionParams;
import org.shaded.apache.http.params.HttpParams;
import org.shaded.apache.http.params.HttpProtocolParams;
import org.shaded.apache.http.protocol.BasicHttpContext;
import org.shaded.apache.http.protocol.BasicHttpProcessor;
import org.shaded.apache.http.protocol.HttpContext;
import org.shaded.apache.http.protocol.HttpRequestExecutor;
import org.shaded.apache.http.protocol.RequestContent;
import org.shaded.apache.http.protocol.RequestExpectContinue;
import org.shaded.apache.http.protocol.RequestTargetHost;
import org.shaded.apache.http.protocol.RequestUserAgent;
import org.shaded.apache.http.util.VersionInfo;

@ThreadSafe
public class DefaultHttpClient extends AbstractHttpClient {
    public DefaultHttpClient(ClientConnectionManager conman, HttpParams params) {
        super(conman, params);
    }

    public DefaultHttpClient(HttpParams params) {
        super(null, params);
    }

    public DefaultHttpClient() {
        super(null, null);
    }

    /* access modifiers changed from: protected */
    public HttpParams createHttpParams() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "ISO-8859-1");
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        VersionInfo vi = VersionInfo.loadVersionInfo("org.shaded.apache.http.client", getClass().getClassLoader());
        HttpProtocolParams.setUserAgent(params, "Apache-HttpClient/" + (vi != null ? vi.getRelease() : VersionInfo.UNAVAILABLE) + " (java 1.5)");
        return params;
    }

    /* access modifiers changed from: protected */
    public HttpRequestExecutor createRequestExecutor() {
        return new HttpRequestExecutor();
    }

    /* access modifiers changed from: protected */
    public ClientConnectionManager createClientConnectionManager() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(HttpHost.DEFAULT_SCHEME_NAME, PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        HttpParams params = getParams();
        ClientConnectionManagerFactory factory = null;
        String className = (String) params.getParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME);
        if (className != null) {
            try {
                factory = (ClientConnectionManagerFactory) Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Invalid class name: " + className);
            } catch (IllegalAccessException ex) {
                throw new IllegalAccessError(ex.getMessage());
            } catch (InstantiationException ex2) {
                throw new InstantiationError(ex2.getMessage());
            }
        }
        if (factory != null) {
            return factory.newInstance(params, registry);
        }
        return new SingleClientConnManager(getParams(), registry);
    }

    /* access modifiers changed from: protected */
    public HttpContext createHttpContext() {
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.SCHEME_REGISTRY, getConnectionManager().getSchemeRegistry());
        context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, getAuthSchemes());
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, getCookieSpecs());
        context.setAttribute(ClientContext.COOKIE_STORE, getCookieStore());
        context.setAttribute(ClientContext.CREDS_PROVIDER, getCredentialsProvider());
        return context;
    }

    /* access modifiers changed from: protected */
    public ConnectionReuseStrategy createConnectionReuseStrategy() {
        return new DefaultConnectionReuseStrategy();
    }

    /* access modifiers changed from: protected */
    public ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy() {
        return new DefaultConnectionKeepAliveStrategy();
    }

    /* access modifiers changed from: protected */
    public AuthSchemeRegistry createAuthSchemeRegistry() {
        AuthSchemeRegistry registry = new AuthSchemeRegistry();
        registry.register(AuthPolicy.BASIC, new BasicSchemeFactory());
        registry.register(AuthPolicy.DIGEST, new DigestSchemeFactory());
        return registry;
    }

    /* access modifiers changed from: protected */
    public CookieSpecRegistry createCookieSpecRegistry() {
        CookieSpecRegistry registry = new CookieSpecRegistry();
        registry.register(CookiePolicy.BEST_MATCH, new BestMatchSpecFactory());
        registry.register(CookiePolicy.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory());
        registry.register(CookiePolicy.NETSCAPE, new NetscapeDraftSpecFactory());
        registry.register(CookiePolicy.RFC_2109, new RFC2109SpecFactory());
        registry.register(CookiePolicy.RFC_2965, new RFC2965SpecFactory());
        return registry;
    }

    /* access modifiers changed from: protected */
    public BasicHttpProcessor createHttpProcessor() {
        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestDefaultHeaders());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestContent());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestTargetHost());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestClientConnControl());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestUserAgent());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestExpectContinue());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestAddCookies());
        httpproc.addInterceptor((HttpResponseInterceptor) new ResponseProcessCookies());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestTargetAuthentication());
        httpproc.addInterceptor((HttpRequestInterceptor) new RequestProxyAuthentication());
        return httpproc;
    }

    /* access modifiers changed from: protected */
    public HttpRequestRetryHandler createHttpRequestRetryHandler() {
        return new DefaultHttpRequestRetryHandler();
    }

    /* access modifiers changed from: protected */
    public RedirectHandler createRedirectHandler() {
        return new DefaultRedirectHandler();
    }

    /* access modifiers changed from: protected */
    public AuthenticationHandler createTargetAuthenticationHandler() {
        return new DefaultTargetAuthenticationHandler();
    }

    /* access modifiers changed from: protected */
    public AuthenticationHandler createProxyAuthenticationHandler() {
        return new DefaultProxyAuthenticationHandler();
    }

    /* access modifiers changed from: protected */
    public CookieStore createCookieStore() {
        return new BasicCookieStore();
    }

    /* access modifiers changed from: protected */
    public CredentialsProvider createCredentialsProvider() {
        return new BasicCredentialsProvider();
    }

    /* access modifiers changed from: protected */
    public HttpRoutePlanner createHttpRoutePlanner() {
        return new DefaultHttpRoutePlanner(getConnectionManager().getSchemeRegistry());
    }

    /* access modifiers changed from: protected */
    public UserTokenHandler createUserTokenHandler() {
        return new DefaultUserTokenHandler();
    }
}
