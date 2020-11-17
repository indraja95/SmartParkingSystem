package org.shaded.apache.http.impl.cookie;

import java.util.Collection;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.cookie.CookieSpec;
import org.shaded.apache.http.cookie.CookieSpecFactory;
import org.shaded.apache.http.cookie.params.CookieSpecPNames;
import org.shaded.apache.http.params.HttpParams;

@Immutable
public class RFC2965SpecFactory implements CookieSpecFactory {
    public CookieSpec newInstance(HttpParams params) {
        if (params == null) {
            return new RFC2965Spec();
        }
        String[] patterns = null;
        Collection<?> param = (Collection) params.getParameter(CookieSpecPNames.DATE_PATTERNS);
        if (param != null) {
            patterns = (String[]) param.toArray(new String[param.size()]);
        }
        return new RFC2965Spec(patterns, params.getBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, false));
    }
}
