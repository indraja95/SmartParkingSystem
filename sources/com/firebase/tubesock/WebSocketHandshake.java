package com.firebase.tubesock;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.shaded.apache.http.protocol.HTTP;

class WebSocketHandshake {
    private static final String WEBSOCKET_VERSION = "13";
    private Map<String, String> extraHeaders = null;
    private String nonce = null;
    private String protocol = null;
    private URI url = null;

    public WebSocketHandshake(URI url2, String protocol2, Map<String, String> extraHeaders2) {
        this.url = url2;
        this.protocol = protocol2;
        this.extraHeaders = extraHeaders2;
        this.nonce = createNonce();
    }

    public byte[] getHandshake() {
        String str;
        String path = this.url.getPath();
        String query = this.url.getQuery();
        StringBuilder append = new StringBuilder().append(path);
        if (query == null) {
            str = "";
        } else {
            str = "?" + query;
        }
        String path2 = append.append(str).toString();
        String host = this.url.getHost();
        if (this.url.getPort() != -1) {
            host = host + ":" + this.url.getPort();
        }
        LinkedHashMap<String, String> header = new LinkedHashMap<>();
        header.put(HTTP.TARGET_HOST, host);
        header.put("Upgrade", "websocket");
        header.put(HTTP.CONN_DIRECTIVE, "Upgrade");
        header.put("Sec-WebSocket-Version", WEBSOCKET_VERSION);
        header.put("Sec-WebSocket-Key", this.nonce);
        if (this.protocol != null) {
            header.put("Sec-WebSocket-Protocol", this.protocol);
        }
        if (this.extraHeaders != null) {
            for (String fieldName : this.extraHeaders.keySet()) {
                if (!header.containsKey(fieldName)) {
                    header.put(fieldName, this.extraHeaders.get(fieldName));
                }
            }
        }
        String handshake = (("GET " + path2 + " HTTP/1.1\r\n") + generateHeader(header)) + "\r\n";
        byte[] handshakeBytes = new byte[handshake.getBytes().length];
        System.arraycopy(handshake.getBytes(), 0, handshakeBytes, 0, handshake.getBytes().length);
        return handshakeBytes;
    }

    private String generateHeader(LinkedHashMap<String, String> headers) {
        String header = new String();
        for (String fieldName : headers.keySet()) {
            header = header + fieldName + ": " + ((String) headers.get(fieldName)) + "\r\n";
        }
        return header;
    }

    private String createNonce() {
        byte[] nonce2 = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce2[i] = (byte) rand(0, 255);
        }
        return Base64.encodeToString(nonce2, false);
    }

    public void verifyServerStatusLine(String statusLine) {
        int statusCode = Integer.valueOf(statusLine.substring(9, 12)).intValue();
        if (statusCode == 407) {
            throw new WebSocketException("connection failed: proxy authentication not supported");
        } else if (statusCode == 404) {
            throw new WebSocketException("connection failed: 404 not found");
        } else if (statusCode != 101) {
            throw new WebSocketException("connection failed: unknown status code " + statusCode);
        }
    }

    public void verifyServerHandshakeHeaders(HashMap<String, String> headers) {
        if (!((String) headers.get("Upgrade")).toLowerCase(Locale.US).equals("websocket")) {
            throw new WebSocketException("connection failed: missing header field in server handshake: Upgrade");
        } else if (!((String) headers.get(HTTP.CONN_DIRECTIVE)).toLowerCase(Locale.US).equals("upgrade")) {
            throw new WebSocketException("connection failed: missing header field in server handshake: Connection");
        }
    }

    private int rand(int min, int max) {
        return (int) ((Math.random() * ((double) max)) + ((double) min));
    }
}
