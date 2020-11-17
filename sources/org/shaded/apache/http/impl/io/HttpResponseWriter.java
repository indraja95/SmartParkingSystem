package org.shaded.apache.http.impl.io;

import java.io.IOException;
import org.shaded.apache.http.HttpMessage;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.io.SessionOutputBuffer;
import org.shaded.apache.http.message.LineFormatter;
import org.shaded.apache.http.params.HttpParams;

public class HttpResponseWriter extends AbstractMessageWriter {
    public HttpResponseWriter(SessionOutputBuffer buffer, LineFormatter formatter, HttpParams params) {
        super(buffer, formatter, params);
    }

    /* access modifiers changed from: protected */
    public void writeHeadLine(HttpMessage message) throws IOException {
        this.lineFormatter.formatStatusLine(this.lineBuf, ((HttpResponse) message).getStatusLine());
        this.sessionBuffer.writeLine(this.lineBuf);
    }
}
