package org.shaded.apache.http.impl.auth;

import org.shaded.apache.commons.codec.binary.Base64;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.annotation.NotThreadSafe;
import org.shaded.apache.http.auth.AUTH;
import org.shaded.apache.http.auth.AuthenticationException;
import org.shaded.apache.http.auth.Credentials;
import org.shaded.apache.http.auth.MalformedChallengeException;
import org.shaded.apache.http.auth.params.AuthParams;
import org.shaded.apache.http.message.BufferedHeader;
import org.shaded.apache.http.util.CharArrayBuffer;
import org.shaded.apache.http.util.EncodingUtils;

@NotThreadSafe
public class BasicScheme extends RFC2617Scheme {
    private boolean complete = false;

    public String getSchemeName() {
        return "basic";
    }

    public void processChallenge(Header header) throws MalformedChallengeException {
        super.processChallenge(header);
        this.complete = true;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public boolean isConnectionBased() {
        return false;
    }

    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        } else if (request != null) {
            return authenticate(credentials, AuthParams.getCredentialCharset(request.getParams()), isProxy());
        } else {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
    }

    public static Header authenticate(Credentials credentials, String charset, boolean proxy) {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        } else if (charset == null) {
            throw new IllegalArgumentException("charset may not be null");
        } else {
            StringBuilder tmp = new StringBuilder();
            tmp.append(credentials.getUserPrincipal().getName());
            tmp.append(":");
            tmp.append(credentials.getPassword() == null ? "null" : credentials.getPassword());
            byte[] base64password = Base64.encodeBase64(EncodingUtils.getBytes(tmp.toString(), charset));
            CharArrayBuffer buffer = new CharArrayBuffer(32);
            if (proxy) {
                buffer.append(AUTH.PROXY_AUTH_RESP);
            } else {
                buffer.append(AUTH.WWW_AUTH_RESP);
            }
            buffer.append(": Basic ");
            buffer.append(base64password, 0, base64password.length);
            return new BufferedHeader(buffer);
        }
    }
}
