package org.shaded.apache.http.auth;

import java.security.Principal;
import java.util.Locale;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.util.LangUtils;

@Immutable
public class NTCredentials implements Credentials {
    private final String password;
    private final NTUserPrincipal principal;
    private final String workstation;

    public NTCredentials(String usernamePassword) {
        String username;
        if (usernamePassword == null) {
            throw new IllegalArgumentException("Username:password string may not be null");
        }
        int atColon = usernamePassword.indexOf(58);
        if (atColon >= 0) {
            username = usernamePassword.substring(0, atColon);
            this.password = usernamePassword.substring(atColon + 1);
        } else {
            username = usernamePassword;
            this.password = null;
        }
        int atSlash = username.indexOf(47);
        if (atSlash >= 0) {
            this.principal = new NTUserPrincipal(username.substring(0, atSlash).toUpperCase(Locale.ENGLISH), username.substring(atSlash + 1));
        } else {
            this.principal = new NTUserPrincipal(null, username.substring(atSlash + 1));
        }
        this.workstation = null;
    }

    public NTCredentials(String userName, String password2, String workstation2, String domain) {
        if (userName == null) {
            throw new IllegalArgumentException("User name may not be null");
        }
        this.principal = new NTUserPrincipal(domain, userName);
        this.password = password2;
        if (workstation2 != null) {
            this.workstation = workstation2.toUpperCase(Locale.ENGLISH);
        } else {
            this.workstation = null;
        }
    }

    public Principal getUserPrincipal() {
        return this.principal;
    }

    public String getUserName() {
        return this.principal.getUsername();
    }

    public String getPassword() {
        return this.password;
    }

    public String getDomain() {
        return this.principal.getDomain();
    }

    public String getWorkstation() {
        return this.workstation;
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(17, (Object) this.principal), (Object) this.workstation);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof NTCredentials)) {
            return false;
        }
        NTCredentials that = (NTCredentials) o;
        if (!LangUtils.equals((Object) this.principal, (Object) that.principal) || !LangUtils.equals((Object) this.workstation, (Object) that.workstation)) {
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[principal: ");
        buffer.append(this.principal);
        buffer.append("][workstation: ");
        buffer.append(this.workstation);
        buffer.append("]");
        return buffer.toString();
    }
}
