package org.shaded.apache.http.impl.io;

import java.io.IOException;
import java.util.ArrayList;
import org.shaded.apache.http.Header;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpMessage;
import org.shaded.apache.http.ParseException;
import org.shaded.apache.http.ProtocolException;
import org.shaded.apache.http.io.HttpMessageParser;
import org.shaded.apache.http.io.SessionInputBuffer;
import org.shaded.apache.http.message.BasicLineParser;
import org.shaded.apache.http.message.LineParser;
import org.shaded.apache.http.params.CoreConnectionPNames;
import org.shaded.apache.http.params.HttpParams;
import org.shaded.apache.http.util.CharArrayBuffer;

public abstract class AbstractMessageParser implements HttpMessageParser {
    protected final LineParser lineParser;
    private final int maxHeaderCount;
    private final int maxLineLen;
    private final SessionInputBuffer sessionBuffer;

    /* access modifiers changed from: protected */
    public abstract HttpMessage parseHead(SessionInputBuffer sessionInputBuffer) throws IOException, HttpException, ParseException;

    public AbstractMessageParser(SessionInputBuffer buffer, LineParser parser, HttpParams params) {
        if (buffer == null) {
            throw new IllegalArgumentException("Session input buffer may not be null");
        } else if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        } else {
            this.sessionBuffer = buffer;
            this.maxHeaderCount = params.getIntParameter(CoreConnectionPNames.MAX_HEADER_COUNT, -1);
            this.maxLineLen = params.getIntParameter(CoreConnectionPNames.MAX_LINE_LENGTH, -1);
            if (parser == null) {
                parser = BasicLineParser.DEFAULT;
            }
            this.lineParser = parser;
        }
    }

    public static Header[] parseHeaders(SessionInputBuffer inbuffer, int maxHeaderCount2, int maxLineLen2, LineParser parser) throws HttpException, IOException {
        if (inbuffer == null) {
            throw new IllegalArgumentException("Session input buffer may not be null");
        }
        if (parser == null) {
            parser = BasicLineParser.DEFAULT;
        }
        ArrayList headerLines = new ArrayList();
        CharArrayBuffer current = null;
        CharArrayBuffer previous = null;
        while (true) {
            if (current == null) {
                current = new CharArrayBuffer(64);
            } else {
                current.clear();
            }
            if (inbuffer.readLine(current) == -1 || current.length() < 1) {
                Header[] headers = new Header[headerLines.size()];
                int i = 0;
            } else {
                if ((current.charAt(0) == ' ' || current.charAt(0) == 9) && previous != null) {
                    int i2 = 0;
                    while (i2 < current.length()) {
                        char ch = current.charAt(i2);
                        if (ch != ' ' && ch != 9) {
                            break;
                        }
                        i2++;
                    }
                    if (maxLineLen2 <= 0 || ((previous.length() + 1) + current.length()) - i2 <= maxLineLen2) {
                        previous.append(' ');
                        previous.append(current, i2, current.length() - i2);
                    } else {
                        throw new IOException("Maximum line length limit exceeded");
                    }
                } else {
                    headerLines.add(current);
                    previous = current;
                    current = null;
                }
                if (maxHeaderCount2 > 0 && headerLines.size() >= maxHeaderCount2) {
                    throw new IOException("Maximum header count exceeded");
                }
            }
        }
        Header[] headers2 = new Header[headerLines.size()];
        int i3 = 0;
        while (i3 < headerLines.size()) {
            try {
                headers2[i3] = parser.parseHeader((CharArrayBuffer) headerLines.get(i3));
                i3++;
            } catch (ParseException ex) {
                throw new ProtocolException(ex.getMessage());
            }
        }
        return headers2;
    }

    public HttpMessage parse() throws IOException, HttpException {
        try {
            HttpMessage message = parseHead(this.sessionBuffer);
            message.setHeaders(parseHeaders(this.sessionBuffer, this.maxHeaderCount, this.maxLineLen, this.lineParser));
            return message;
        } catch (ParseException px) {
            throw new ProtocolException(px.getMessage(), px);
        }
    }
}
