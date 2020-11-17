package org.shaded.apache.http.impl.cookie;

import java.util.StringTokenizer;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.cookie.ClientCookie;
import org.shaded.apache.http.cookie.Cookie;
import org.shaded.apache.http.cookie.CookieAttributeHandler;
import org.shaded.apache.http.cookie.CookieOrigin;
import org.shaded.apache.http.cookie.MalformedCookieException;
import org.shaded.apache.http.cookie.SetCookie;
import org.shaded.apache.http.cookie.SetCookie2;

@Immutable
public class RFC2965PortAttributeHandler implements CookieAttributeHandler {
    private static int[] parsePortAttribute(String portValue) throws MalformedCookieException {
        StringTokenizer st = new StringTokenizer(portValue, ",");
        int[] ports = new int[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            try {
                ports[i] = Integer.parseInt(st.nextToken().trim());
                if (ports[i] < 0) {
                    throw new MalformedCookieException("Invalid Port attribute.");
                }
                i++;
            } catch (NumberFormatException e) {
                throw new MalformedCookieException("Invalid Port attribute: " + e.getMessage());
            }
        }
        return ports;
    }

    private static boolean portMatch(int port, int[] ports) {
        for (int i : ports) {
            if (port == i) {
                return true;
            }
        }
        return false;
    }

    public void parse(SetCookie cookie, String portValue) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (cookie instanceof SetCookie2) {
            SetCookie2 cookie2 = (SetCookie2) cookie;
            if (portValue != null && portValue.trim().length() > 0) {
                cookie2.setPorts(parsePortAttribute(portValue));
            }
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            int port = origin.getPort();
            if ((cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute(ClientCookie.PORT_ATTR) && !portMatch(port, cookie.getPorts())) {
                throw new MalformedCookieException("Port attribute violates RFC 2965: Request port not found in cookie's port list.");
            }
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            int port = origin.getPort();
            if ((cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute(ClientCookie.PORT_ATTR)) {
                if (cookie.getPorts() == null) {
                    return false;
                }
                if (!portMatch(port, cookie.getPorts())) {
                    return false;
                }
            }
            return true;
        }
    }
}
