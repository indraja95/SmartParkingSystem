package org.shaded.apache.http.impl.conn;

import java.io.IOException;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.io.HttpTransportMetrics;
import org.shaded.apache.http.io.SessionOutputBuffer;
import org.shaded.apache.http.util.CharArrayBuffer;

@Immutable
public class LoggingSessionOutputBuffer implements SessionOutputBuffer {
    private final SessionOutputBuffer out;
    private final Wire wire;

    public LoggingSessionOutputBuffer(SessionOutputBuffer out2, Wire wire2) {
        this.out = out2;
        this.wire = wire2;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        if (this.wire.enabled()) {
            this.wire.output(b, off, len);
        }
    }

    public void write(int b) throws IOException {
        this.out.write(b);
        if (this.wire.enabled()) {
            this.wire.output(b);
        }
    }

    public void write(byte[] b) throws IOException {
        this.out.write(b);
        if (this.wire.enabled()) {
            this.wire.output(b);
        }
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void writeLine(CharArrayBuffer buffer) throws IOException {
        this.out.writeLine(buffer);
        if (this.wire.enabled()) {
            this.wire.output(new String(buffer.buffer(), 0, buffer.length()) + "[EOL]");
        }
    }

    public void writeLine(String s) throws IOException {
        this.out.writeLine(s);
        if (this.wire.enabled()) {
            this.wire.output(s + "[EOL]");
        }
    }

    public HttpTransportMetrics getMetrics() {
        return this.out.getMetrics();
    }
}
