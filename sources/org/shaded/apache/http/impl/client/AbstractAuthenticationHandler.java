package org.shaded.apache.http.impl.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.shaded.apache.commons.logging.Log;
import org.shaded.apache.commons.logging.LogFactory;
import org.shaded.apache.http.FormattedHeader;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.auth.AuthScheme;
import org.shaded.apache.http.auth.AuthSchemeRegistry;
import org.shaded.apache.http.auth.AuthenticationException;
import org.shaded.apache.http.auth.MalformedChallengeException;
import org.shaded.apache.http.client.AuthenticationHandler;
import org.shaded.apache.http.client.protocol.ClientContext;
import org.shaded.apache.http.protocol.HTTP;
import org.shaded.apache.http.protocol.HttpContext;
import org.shaded.apache.http.util.CharArrayBuffer;

@Immutable
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {
    private static final List<String> DEFAULT_SCHEME_PRIORITY = Collections.unmodifiableList(Arrays.asList(new String[]{"ntlm", "digest", "basic"}));
    private final Log log = LogFactory.getLog(getClass());

    /* access modifiers changed from: protected */
    public Map<String, Header> parseChallenges(Header[] headers) throws MalformedChallengeException {
        Header[] arr$;
        int pos;
        CharArrayBuffer buffer;
        Map<String, Header> map = new HashMap<>(headers.length);
        for (Header header : headers) {
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                pos = ((FormattedHeader) header).getValuePos();
            } else {
                String s = header.getValue();
                if (s == null) {
                    throw new MalformedChallengeException("Header value is null");
                }
                buffer = new CharArrayBuffer(s.length());
                buffer.append(s);
                pos = 0;
            }
            while (pos < buffer.length() && HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            int beginIndex = pos;
            while (pos < buffer.length() && !HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            map.put(buffer.substring(beginIndex, pos).toLowerCase(Locale.ENGLISH), header);
        }
        return map;
    }

    /* access modifiers changed from: protected */
    public List<String> getAuthPreferences() {
        return DEFAULT_SCHEME_PRIORITY;
    }

    public AuthScheme selectScheme(Map<String, Header> challenges, HttpResponse response, HttpContext context) throws AuthenticationException {
        AuthSchemeRegistry registry = (AuthSchemeRegistry) context.getAttribute(ClientContext.AUTHSCHEME_REGISTRY);
        if (registry == null) {
            throw new IllegalStateException("AuthScheme registry not set in HTTP context");
        }
        Collection<String> authPrefs = (Collection) context.getAttribute(ClientContext.AUTH_SCHEME_PREF);
        if (authPrefs == null) {
            authPrefs = getAuthPreferences();
        }
        if (this.log.isDebugEnabled()) {
            this.log.debug("Authentication schemes in the order of preference: " + authPrefs);
        }
        AuthScheme authScheme = null;
        for (String id : authPrefs) {
            if (((Header) challenges.get(id.toLowerCase(Locale.ENGLISH))) != null) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug(id + " authentication scheme selected");
                }
                try {
                    authScheme = registry.getAuthScheme(id, response.getParams());
                    break;
                } catch (IllegalStateException e) {
                    if (this.log.isWarnEnabled()) {
                        this.log.warn("Authentication scheme " + id + " not supported");
                    }
                }
            } else if (this.log.isDebugEnabled()) {
                this.log.debug("Challenge for " + id + " authentication scheme not available");
            }
        }
        if (authScheme != null) {
            return authScheme;
        }
        throw new AuthenticationException("Unable to respond to any of these challenges: " + challenges);
    }
}
