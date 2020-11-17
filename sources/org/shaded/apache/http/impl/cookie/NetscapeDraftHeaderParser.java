package org.shaded.apache.http.impl.cookie;

import java.util.ArrayList;
import java.util.List;
import org.shaded.apache.http.HeaderElement;
import org.shaded.apache.http.NameValuePair;
import org.shaded.apache.http.ParseException;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.message.BasicHeaderElement;
import org.shaded.apache.http.message.BasicHeaderValueParser;
import org.shaded.apache.http.message.ParserCursor;
import org.shaded.apache.http.util.CharArrayBuffer;

@Immutable
public class NetscapeDraftHeaderParser {
    public static final NetscapeDraftHeaderParser DEFAULT = new NetscapeDraftHeaderParser();
    private static final char[] DELIMITERS = {';'};
    private final BasicHeaderValueParser nvpParser = BasicHeaderValueParser.DEFAULT;

    public HeaderElement parseHeader(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        } else {
            NameValuePair nvp = this.nvpParser.parseNameValuePair(buffer, cursor, DELIMITERS);
            List<NameValuePair> params = new ArrayList<>();
            while (!cursor.atEnd()) {
                params.add(this.nvpParser.parseNameValuePair(buffer, cursor, DELIMITERS));
            }
            return new BasicHeaderElement(nvp.getName(), nvp.getValue(), (NameValuePair[]) params.toArray(new NameValuePair[params.size()]));
        }
    }
}
