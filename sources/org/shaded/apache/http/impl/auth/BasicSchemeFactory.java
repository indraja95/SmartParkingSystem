package org.shaded.apache.http.impl.auth;

import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.auth.AuthScheme;
import org.shaded.apache.http.auth.AuthSchemeFactory;
import org.shaded.apache.http.params.HttpParams;

@Immutable
public class BasicSchemeFactory implements AuthSchemeFactory {
    public AuthScheme newInstance(HttpParams params) {
        return new BasicScheme();
    }
}
