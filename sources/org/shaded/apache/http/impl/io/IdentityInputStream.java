package org.shaded.apache.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.shaded.apache.http.io.SessionInputBuffer;

public class IdentityInputStream extends InputStream {
    private boolean closed = false;
    private final SessionInputBuffer in;

    public IdentityInputStream(SessionInputBuffer in2) {
        if (in2 == null) {
            throw new IllegalArgumentException("Session input buffer may not be null");
        }
        this.in = in2;
    }

    public int available() throws IOException {
        if (this.closed || !this.in.isDataAvailable(10)) {
            return 0;
        }
        return 1;
    }

    public void close() throws IOException {
        this.closed = true;
    }

    public int read() throws IOException {
        if (this.closed) {
            return -1;
        }
        return this.in.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            return -1;
        }
        return this.in.read(b, off, len);
    }
}
