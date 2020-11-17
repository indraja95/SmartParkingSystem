package org.shaded.apache.http.impl.auth;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.NameValuePair;
import org.shaded.apache.http.annotation.NotThreadSafe;
import org.shaded.apache.http.auth.AUTH;
import org.shaded.apache.http.auth.AuthenticationException;
import org.shaded.apache.http.auth.Credentials;
import org.shaded.apache.http.auth.MalformedChallengeException;
import org.shaded.apache.http.auth.params.AuthParams;
import org.shaded.apache.http.message.BasicHeaderValueFormatter;
import org.shaded.apache.http.message.BasicNameValuePair;
import org.shaded.apache.http.message.BufferedHeader;
import org.shaded.apache.http.util.CharArrayBuffer;
import org.shaded.apache.http.util.EncodingUtils;

@NotThreadSafe
public class DigestScheme extends RFC2617Scheme {
    private static final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String NC = "00000001";
    private static final int QOP_AUTH = 2;
    private static final int QOP_AUTH_INT = 1;
    private static final int QOP_MISSING = 0;
    private String cnonce;
    private boolean complete = false;
    private int qopVariant = 0;

    public void processChallenge(Header header) throws MalformedChallengeException {
        super.processChallenge(header);
        if (getParameter("realm") == null) {
            throw new MalformedChallengeException("missing realm in challange");
        } else if (getParameter("nonce") == null) {
            throw new MalformedChallengeException("missing nonce in challange");
        } else {
            boolean unsupportedQop = false;
            String qop = getParameter("qop");
            if (qop != null) {
                StringTokenizer tok = new StringTokenizer(qop, ",");
                while (true) {
                    if (!tok.hasMoreTokens()) {
                        break;
                    }
                    String variant = tok.nextToken().trim();
                    if (variant.equals("auth")) {
                        this.qopVariant = 2;
                        break;
                    } else if (variant.equals("auth-int")) {
                        this.qopVariant = 1;
                    } else {
                        unsupportedQop = true;
                    }
                }
            }
            if (!unsupportedQop || this.qopVariant != 0) {
                this.cnonce = null;
                this.complete = true;
                return;
            }
            throw new MalformedChallengeException("None of the qop methods is supported");
        }
    }

    public boolean isComplete() {
        if ("true".equalsIgnoreCase(getParameter("stale"))) {
            return false;
        }
        return this.complete;
    }

    public String getSchemeName() {
        return "digest";
    }

    public boolean isConnectionBased() {
        return false;
    }

    public void overrideParamter(String name, String value) {
        getParameters().put(name, value);
    }

    private String getCnonce() {
        if (this.cnonce == null) {
            this.cnonce = createCnonce();
        }
        return this.cnonce;
    }

    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        } else if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else {
            getParameters().put("methodname", request.getRequestLine().getMethod());
            getParameters().put("uri", request.getRequestLine().getUri());
            if (getParameter("charset") == null) {
                getParameters().put("charset", AuthParams.getCredentialCharset(request.getParams()));
            }
            return createDigestHeader(credentials, createDigest(credentials));
        }
    }

    private static MessageDigest createMessageDigest(String digAlg) throws UnsupportedDigestAlgorithmException {
        try {
            return MessageDigest.getInstance(digAlg);
        } catch (Exception e) {
            throw new UnsupportedDigestAlgorithmException("Unsupported algorithm in HTTP Digest authentication: " + digAlg);
        }
    }

    private String createDigest(Credentials credentials) throws AuthenticationException {
        String serverDigestValue;
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String method = getParameter("methodname");
        String algorithm = getParameter("algorithm");
        if (uri == null) {
            throw new IllegalStateException("URI may not be null");
        } else if (realm == null) {
            throw new IllegalStateException("Realm may not be null");
        } else if (nonce == null) {
            throw new IllegalStateException("Nonce may not be null");
        } else {
            if (algorithm == null) {
                algorithm = "MD5";
            }
            String charset = getParameter("charset");
            if (charset == null) {
                charset = "ISO-8859-1";
            }
            if (this.qopVariant == 1) {
                throw new AuthenticationException("Unsupported qop in HTTP Digest authentication");
            }
            String digAlg = algorithm;
            if (digAlg.equalsIgnoreCase("MD5-sess")) {
                digAlg = "MD5";
            }
            MessageDigest digester = createMessageDigest(digAlg);
            String uname = credentials.getUserPrincipal().getName();
            String pwd = credentials.getPassword();
            StringBuilder sb = new StringBuilder(uname.length() + realm.length() + pwd.length() + 2);
            sb.append(uname);
            sb.append(':');
            sb.append(realm);
            sb.append(':');
            sb.append(pwd);
            String a1 = sb.toString();
            if (algorithm.equalsIgnoreCase("MD5-sess")) {
                String algorithm2 = "MD5";
                String cnonce2 = getCnonce();
                String tmp2 = encode(digester.digest(EncodingUtils.getBytes(a1, charset)));
                StringBuilder sb2 = new StringBuilder(tmp2.length() + nonce.length() + cnonce2.length() + 2);
                sb2.append(tmp2);
                sb2.append(':');
                sb2.append(nonce);
                sb2.append(':');
                sb2.append(cnonce2);
                a1 = sb2.toString();
            }
            String hasha1 = encode(digester.digest(EncodingUtils.getBytes(a1, charset)));
            String a2 = null;
            if (this.qopVariant != 1) {
                a2 = method + ':' + uri;
            }
            String hasha2 = encode(digester.digest(EncodingUtils.getAsciiBytes(a2)));
            if (this.qopVariant == 0) {
                StringBuilder sb3 = new StringBuilder(hasha1.length() + nonce.length() + hasha1.length());
                sb3.append(hasha1);
                sb3.append(':');
                sb3.append(nonce);
                sb3.append(':');
                sb3.append(hasha2);
                serverDigestValue = sb3.toString();
            } else {
                String qopOption = getQopVariantString();
                String cnonce3 = getCnonce();
                StringBuilder sb4 = new StringBuilder(hasha1.length() + nonce.length() + NC.length() + cnonce3.length() + qopOption.length() + hasha2.length() + 5);
                sb4.append(hasha1);
                sb4.append(':');
                sb4.append(nonce);
                sb4.append(':');
                sb4.append(NC);
                sb4.append(':');
                sb4.append(cnonce3);
                sb4.append(':');
                sb4.append(qopOption);
                sb4.append(':');
                sb4.append(hasha2);
                serverDigestValue = sb4.toString();
            }
            return encode(digester.digest(EncodingUtils.getAsciiBytes(serverDigestValue)));
        }
    }

    private Header createDigestHeader(Credentials credentials, String digest) {
        boolean z;
        CharArrayBuffer buffer = new CharArrayBuffer(128);
        if (isProxy()) {
            buffer.append(AUTH.PROXY_AUTH_RESP);
        } else {
            buffer.append(AUTH.WWW_AUTH_RESP);
        }
        buffer.append(": Digest ");
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String opaque = getParameter("opaque");
        String response = digest;
        String algorithm = getParameter("algorithm");
        String uname = credentials.getUserPrincipal().getName();
        List<BasicNameValuePair> params = new ArrayList<>(20);
        params.add(new BasicNameValuePair("username", uname));
        params.add(new BasicNameValuePair("realm", realm));
        params.add(new BasicNameValuePair("nonce", nonce));
        params.add(new BasicNameValuePair("uri", uri));
        params.add(new BasicNameValuePair("response", response));
        if (this.qopVariant != 0) {
            params.add(new BasicNameValuePair("qop", getQopVariantString()));
            params.add(new BasicNameValuePair("nc", NC));
            params.add(new BasicNameValuePair("cnonce", getCnonce()));
        }
        if (algorithm != null) {
            params.add(new BasicNameValuePair("algorithm", algorithm));
        }
        if (opaque != null) {
            params.add(new BasicNameValuePair("opaque", opaque));
        }
        for (int i = 0; i < params.size(); i++) {
            BasicNameValuePair param = (BasicNameValuePair) params.get(i);
            if (i > 0) {
                buffer.append(", ");
            }
            boolean noQuotes = "nc".equals(param.getName()) || "qop".equals(param.getName());
            BasicHeaderValueFormatter basicHeaderValueFormatter = BasicHeaderValueFormatter.DEFAULT;
            if (!noQuotes) {
                z = true;
            } else {
                z = false;
            }
            basicHeaderValueFormatter.formatNameValuePair(buffer, (NameValuePair) param, z);
        }
        return new BufferedHeader(buffer);
    }

    private String getQopVariantString() {
        if (this.qopVariant == 1) {
            return "auth-int";
        }
        return "auth";
    }

    private static String encode(byte[] binaryData) {
        int n = binaryData.length;
        char[] buffer = new char[(n * 2)];
        for (int i = 0; i < n; i++) {
            int low = binaryData[i] & 15;
            buffer[i * 2] = HEXADECIMAL[(binaryData[i] & 240) >> 4];
            buffer[(i * 2) + 1] = HEXADECIMAL[low];
        }
        return new String(buffer);
    }

    public static String createCnonce() {
        return encode(createMessageDigest("MD5").digest(EncodingUtils.getAsciiBytes(Long.toString(System.currentTimeMillis()))));
    }
}
