package org.shaded.apache.http.impl.cookie;

import java.util.Locale;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.cookie.Cookie;
import org.shaded.apache.http.cookie.CookieAttributeHandler;
import org.shaded.apache.http.cookie.CookieOrigin;
import org.shaded.apache.http.cookie.MalformedCookieException;
import org.shaded.apache.http.cookie.SetCookie;

@Immutable
public class RFC2109DomainHandler implements CookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (value == null) {
            throw new MalformedCookieException("Missing value for domain attribute");
        } else if (value.trim().length() == 0) {
            throw new MalformedCookieException("Blank value for domain attribute");
        } else {
            cookie.setDomain(value);
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            String host = origin.getHost();
            String domain = cookie.getDomain();
            if (domain == null) {
                throw new MalformedCookieException("Cookie domain may not be null");
            } else if (domain.equals(host)) {
            } else {
                if (domain.indexOf(46) == -1) {
                    throw new MalformedCookieException("Domain attribute \"" + domain + "\" does not match the host \"" + host + "\"");
                } else if (!domain.startsWith(".")) {
                    throw new MalformedCookieException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must start with a dot");
                } else {
                    int dotIndex = domain.indexOf(46, 1);
                    if (dotIndex < 0 || dotIndex == domain.length() - 1) {
                        throw new MalformedCookieException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must contain an embedded dot");
                    }
                    String host2 = host.toLowerCase(Locale.ENGLISH);
                    if (!host2.endsWith(domain)) {
                        throw new MalformedCookieException("Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host2 + "\"");
                    } else if (host2.substring(0, host2.length() - domain.length()).indexOf(46) != -1) {
                        throw new MalformedCookieException("Domain attribute \"" + domain + "\" violates RFC 2109: host minus domain may not contain any dots");
                    }
                }
            }
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            String host = origin.getHost();
            String domain = cookie.getDomain();
            if (domain == null) {
                return false;
            }
            if (host.equals(domain) || (domain.startsWith(".") && host.endsWith(domain))) {
                return true;
            }
            return false;
        }
    }
}
