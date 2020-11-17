package org.shaded.apache.http.message;

import org.shaded.apache.http.ProtocolVersion;
import org.shaded.apache.http.StatusLine;
import org.shaded.apache.http.util.CharArrayBuffer;

public class BasicStatusLine implements StatusLine, Cloneable {
    private final ProtocolVersion protoVersion;
    private final String reasonPhrase;
    private final int statusCode;

    public BasicStatusLine(ProtocolVersion version, int statusCode2, String reasonPhrase2) {
        if (version == null) {
            throw new IllegalArgumentException("Protocol version may not be null.");
        } else if (statusCode2 < 0) {
            throw new IllegalArgumentException("Status code may not be negative.");
        } else {
            this.protoVersion = version;
            this.statusCode = statusCode2;
            this.reasonPhrase = reasonPhrase2;
        }
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protoVersion;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public String toString() {
        return BasicLineFormatter.DEFAULT.formatStatusLine((CharArrayBuffer) null, (StatusLine) this).toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
