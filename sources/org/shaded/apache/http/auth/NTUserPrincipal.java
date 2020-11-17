package org.shaded.apache.http.auth;

import java.security.Principal;
import java.util.Locale;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.util.LangUtils;

@Immutable
public class NTUserPrincipal implements Principal {
    private final String domain;
    private final String ntname;
    private final String username;

    public NTUserPrincipal(String domain2, String username2) {
        if (username2 == null) {
            throw new IllegalArgumentException("User name may not be null");
        }
        this.username = username2;
        if (domain2 != null) {
            this.domain = domain2.toUpperCase(Locale.ENGLISH);
        } else {
            this.domain = null;
        }
        if (this.domain == null || this.domain.length() <= 0) {
            this.ntname = this.username;
            return;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.domain);
        buffer.append('/');
        buffer.append(this.username);
        this.ntname = buffer.toString();
    }

    public String getName() {
        return this.ntname;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getUsername() {
        return this.username;
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(17, (Object) this.username), (Object) this.domain);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof NTUserPrincipal)) {
            return false;
        }
        NTUserPrincipal that = (NTUserPrincipal) o;
        if (!LangUtils.equals((Object) this.username, (Object) that.username) || !LangUtils.equals((Object) this.domain, (Object) that.domain)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return this.ntname;
    }
}
