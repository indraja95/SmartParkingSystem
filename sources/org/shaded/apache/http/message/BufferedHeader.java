package org.shaded.apache.http.message;

import org.shaded.apache.http.FormattedHeader;
import org.shaded.apache.http.HeaderElement;
import org.shaded.apache.http.ParseException;
import org.shaded.apache.http.util.CharArrayBuffer;

public class BufferedHeader implements FormattedHeader, Cloneable {
    private final CharArrayBuffer buffer;
    private final String name;
    private final int valuePos;

    public BufferedHeader(CharArrayBuffer buffer2) throws ParseException {
        if (buffer2 == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        int colon = buffer2.indexOf(58);
        if (colon == -1) {
            throw new ParseException(new StringBuffer().append("Invalid header: ").append(buffer2.toString()).toString());
        }
        String s = buffer2.substringTrimmed(0, colon);
        if (s.length() == 0) {
            throw new ParseException(new StringBuffer().append("Invalid header: ").append(buffer2.toString()).toString());
        }
        this.buffer = buffer2;
        this.name = s;
        this.valuePos = colon + 1;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.buffer.substringTrimmed(this.valuePos, this.buffer.length());
    }

    public HeaderElement[] getElements() throws ParseException {
        ParserCursor cursor = new ParserCursor(0, this.buffer.length());
        cursor.updatePos(this.valuePos);
        return BasicHeaderValueParser.DEFAULT.parseElements(this.buffer, cursor);
    }

    public int getValuePos() {
        return this.valuePos;
    }

    public CharArrayBuffer getBuffer() {
        return this.buffer;
    }

    public String toString() {
        return this.buffer.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
