package org.shaded.apache.http.impl.cookie;

import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.cookie.Cookie;
import org.shaded.apache.http.cookie.CookieAttributeHandler;
import org.shaded.apache.http.cookie.CookieOrigin;
import org.shaded.apache.http.cookie.MalformedCookieException;
import org.shaded.apache.http.cookie.SetCookie;

@Immutable
public class BasicPathHandler implements CookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        if (value == null || value.trim().length() == 0) {
            value = "/";
        }
        cookie.setPath(value);
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (!match(cookie, origin)) {
            throw new MalformedCookieException("Illegal path attribute \"" + cookie.getPath() + "\". Path of origin: \"" + origin.getPath() + "\"");
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            String targetpath = origin.getPath();
            String topmostPath = cookie.getPath();
            if (topmostPath == null) {
                topmostPath = "/";
            }
            if (topmostPath.length() > 1 && topmostPath.endsWith("/")) {
                topmostPath = topmostPath.substring(0, topmostPath.length() - 1);
            }
            boolean match = targetpath.startsWith(topmostPath);
            if (!match || targetpath.length() == topmostPath.length() || topmostPath.endsWith("/")) {
                return match;
            }
            if (targetpath.charAt(topmostPath.length()) == '/') {
                return true;
            }
            return false;
        }
    }
}
