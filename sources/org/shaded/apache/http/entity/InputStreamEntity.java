package org.shaded.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamEntity extends AbstractHttpEntity {
    private static final int BUFFER_SIZE = 2048;
    private boolean consumed = false;
    private final InputStream content;
    private final long length;

    public InputStreamEntity(InputStream instream, long length2) {
        if (instream == null) {
            throw new IllegalArgumentException("Source input stream may not be null");
        }
        this.content = instream;
        this.length = length2;
    }

    public boolean isRepeatable() {
        return false;
    }

    public long getContentLength() {
        return this.length;
    }

    public InputStream getContent() throws IOException {
        return this.content;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = this.content;
        byte[] buffer = new byte[2048];
        if (this.length < 0) {
            while (true) {
                int l = instream.read(buffer);
                if (l == -1) {
                    break;
                }
                outstream.write(buffer, 0, l);
            }
        } else {
            long remaining = this.length;
            while (remaining > 0) {
                int l2 = instream.read(buffer, 0, (int) Math.min(2048, remaining));
                if (l2 == -1) {
                    break;
                }
                outstream.write(buffer, 0, l2);
                remaining -= (long) l2;
            }
        }
        this.consumed = true;
    }

    public boolean isStreaming() {
        return !this.consumed;
    }

    public void consumeContent() throws IOException {
        this.consumed = true;
        this.content.close();
    }
}
