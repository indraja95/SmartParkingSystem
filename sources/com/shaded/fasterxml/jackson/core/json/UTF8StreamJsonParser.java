package com.shaded.fasterxml.jackson.core.json;

import com.google.appinventor.components.runtime.util.Ev3Constants.Opcode;
import com.google.appinventor.components.runtime.util.FullScreenVideoUtil;
import com.shaded.fasterxml.jackson.core.Base64Variant;
import com.shaded.fasterxml.jackson.core.JsonParseException;
import com.shaded.fasterxml.jackson.core.JsonParser.Feature;
import com.shaded.fasterxml.jackson.core.JsonToken;
import com.shaded.fasterxml.jackson.core.ObjectCodec;
import com.shaded.fasterxml.jackson.core.SerializableString;
import com.shaded.fasterxml.jackson.core.base.ParserBase;
import com.shaded.fasterxml.jackson.core.io.CharTypes;
import com.shaded.fasterxml.jackson.core.io.IOContext;
import com.shaded.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.shaded.fasterxml.jackson.core.sym.Name;
import com.shaded.fasterxml.jackson.core.util.ByteArrayBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class UTF8StreamJsonParser extends ParserBase {
    static final byte BYTE_LF = 10;
    private static final int[] sInputCodesLatin1 = CharTypes.getInputCodeLatin1();
    private static final int[] sInputCodesUtf8 = CharTypes.getInputCodeUtf8();
    protected boolean _bufferRecyclable;
    protected byte[] _inputBuffer;
    protected InputStream _inputStream;
    protected ObjectCodec _objectCodec;
    private int _quad1;
    protected int[] _quadBuffer = new int[16];
    protected final BytesToNameCanonicalizer _symbols;
    protected boolean _tokenIncomplete = false;

    public UTF8StreamJsonParser(IOContext iOContext, int i, InputStream inputStream, ObjectCodec objectCodec, BytesToNameCanonicalizer bytesToNameCanonicalizer, byte[] bArr, int i2, int i3, boolean z) {
        super(iOContext, i);
        this._inputStream = inputStream;
        this._objectCodec = objectCodec;
        this._symbols = bytesToNameCanonicalizer;
        this._inputBuffer = bArr;
        this._inputPtr = i2;
        this._inputEnd = i3;
        this._bufferRecyclable = z;
    }

    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    public void setCodec(ObjectCodec objectCodec) {
        this._objectCodec = objectCodec;
    }

    public int releaseBuffered(OutputStream outputStream) throws IOException {
        int i = this._inputEnd - this._inputPtr;
        if (i < 1) {
            return 0;
        }
        outputStream.write(this._inputBuffer, this._inputPtr, i);
        return i;
    }

    public Object getInputSource() {
        return this._inputStream;
    }

    /* access modifiers changed from: protected */
    public boolean loadMore() throws IOException {
        this._currInputProcessed += (long) this._inputEnd;
        this._currInputRowStart -= this._inputEnd;
        if (this._inputStream == null) {
            return false;
        }
        int read = this._inputStream.read(this._inputBuffer, 0, this._inputBuffer.length);
        if (read > 0) {
            this._inputPtr = 0;
            this._inputEnd = read;
            return true;
        }
        _closeInput();
        if (read != 0) {
            return false;
        }
        throw new IOException("InputStream.read() returned 0 characters when trying to read " + this._inputBuffer.length + " bytes");
    }

    /* access modifiers changed from: protected */
    public boolean _loadToHaveAtLeast(int i) throws IOException {
        if (this._inputStream == null) {
            return false;
        }
        int i2 = this._inputEnd - this._inputPtr;
        if (i2 <= 0 || this._inputPtr <= 0) {
            this._inputEnd = 0;
        } else {
            this._currInputProcessed += (long) this._inputPtr;
            this._currInputRowStart -= this._inputPtr;
            System.arraycopy(this._inputBuffer, this._inputPtr, this._inputBuffer, 0, i2);
            this._inputEnd = i2;
        }
        this._inputPtr = 0;
        while (this._inputEnd < i) {
            int read = this._inputStream.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (read < 1) {
                _closeInput();
                if (read != 0) {
                    return false;
                }
                throw new IOException("InputStream.read() returned 0 characters when trying to read " + i2 + " bytes");
            }
            this._inputEnd = read + this._inputEnd;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void _closeInput() throws IOException {
        if (this._inputStream != null) {
            if (this._ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                this._inputStream.close();
            }
            this._inputStream = null;
        }
    }

    /* access modifiers changed from: protected */
    public void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        if (this._bufferRecyclable) {
            byte[] bArr = this._inputBuffer;
            if (bArr != null) {
                this._inputBuffer = null;
                this._ioContext.releaseReadIOBuffer(bArr);
            }
        }
    }

    public String getText() throws IOException, JsonParseException {
        if (this._currToken != JsonToken.VALUE_STRING) {
            return _getText2(this._currToken);
        }
        if (this._tokenIncomplete) {
            this._tokenIncomplete = false;
            _finishString();
        }
        return this._textBuffer.contentsAsString();
    }

    public String getValueAsString() throws IOException, JsonParseException {
        if (this._currToken != JsonToken.VALUE_STRING) {
            return super.getValueAsString(null);
        }
        if (this._tokenIncomplete) {
            this._tokenIncomplete = false;
            _finishString();
        }
        return this._textBuffer.contentsAsString();
    }

    public String getValueAsString(String str) throws IOException, JsonParseException {
        if (this._currToken != JsonToken.VALUE_STRING) {
            return super.getValueAsString(str);
        }
        if (this._tokenIncomplete) {
            this._tokenIncomplete = false;
            _finishString();
        }
        return this._textBuffer.contentsAsString();
    }

    /* access modifiers changed from: protected */
    public String _getText2(JsonToken jsonToken) {
        if (jsonToken == null) {
            return null;
        }
        switch (jsonToken) {
            case FIELD_NAME:
                return this._parsingContext.getCurrentName();
            case VALUE_STRING:
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return this._textBuffer.contentsAsString();
            default:
                return jsonToken.asString();
        }
    }

    public char[] getTextCharacters() throws IOException, JsonParseException {
        if (this._currToken == null) {
            return null;
        }
        switch (this._currToken) {
            case FIELD_NAME:
                if (!this._nameCopied) {
                    String currentName = this._parsingContext.getCurrentName();
                    int length = currentName.length();
                    if (this._nameCopyBuffer == null) {
                        this._nameCopyBuffer = this._ioContext.allocNameCopyBuffer(length);
                    } else if (this._nameCopyBuffer.length < length) {
                        this._nameCopyBuffer = new char[length];
                    }
                    currentName.getChars(0, length, this._nameCopyBuffer, 0);
                    this._nameCopied = true;
                }
                return this._nameCopyBuffer;
            case VALUE_STRING:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                break;
            default:
                return this._currToken.asCharArray();
        }
        return this._textBuffer.getTextBuffer();
    }

    public int getTextLength() throws IOException, JsonParseException {
        if (this._currToken == null) {
            return 0;
        }
        switch (this._currToken) {
            case FIELD_NAME:
                return this._parsingContext.getCurrentName().length();
            case VALUE_STRING:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                break;
            default:
                return this._currToken.asCharArray().length;
        }
        return this._textBuffer.size();
    }

    public int getTextOffset() throws IOException, JsonParseException {
        if (this._currToken == null) {
            return 0;
        }
        switch (this._currToken) {
            case VALUE_STRING:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                break;
            default:
                return 0;
        }
        return this._textBuffer.getTextOffset();
    }

    public byte[] getBinaryValue(Base64Variant base64Variant) throws IOException, JsonParseException {
        if (this._currToken != JsonToken.VALUE_STRING && (this._currToken != JsonToken.VALUE_EMBEDDED_OBJECT || this._binaryValue == null)) {
            _reportError("Current token (" + this._currToken + ") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        if (this._tokenIncomplete) {
            try {
                this._binaryValue = _decodeBase64(base64Variant);
                this._tokenIncomplete = false;
            } catch (IllegalArgumentException e) {
                throw _constructError("Failed to decode VALUE_STRING as base64 (" + base64Variant + "): " + e.getMessage());
            }
        } else if (this._binaryValue == null) {
            ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
            _decodeBase64(getText(), _getByteArrayBuilder, base64Variant);
            this._binaryValue = _getByteArrayBuilder.toByteArray();
        }
        return this._binaryValue;
    }

    public int readBinaryValue(Base64Variant base64Variant, OutputStream outputStream) throws IOException, JsonParseException {
        if (!this._tokenIncomplete || this._currToken != JsonToken.VALUE_STRING) {
            byte[] binaryValue = getBinaryValue(base64Variant);
            outputStream.write(binaryValue);
            return binaryValue.length;
        }
        byte[] allocBase64Buffer = this._ioContext.allocBase64Buffer();
        try {
            return _readBinary(base64Variant, outputStream, allocBase64Buffer);
        } finally {
            this._ioContext.releaseBase64Buffer(allocBase64Buffer);
        }
    }

    /* access modifiers changed from: protected */
    public int _readBinary(Base64Variant base64Variant, OutputStream outputStream, byte[] bArr) throws IOException, JsonParseException {
        int i;
        int length = bArr.length - 3;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                loadMoreGuaranteed();
            }
            byte[] bArr2 = this._inputBuffer;
            int i4 = this._inputPtr;
            this._inputPtr = i4 + 1;
            byte b = bArr2[i4] & Opcode.TST;
            if (b > 32) {
                int decodeBase64Char = base64Variant.decodeBase64Char((int) b);
                if (decodeBase64Char < 0) {
                    if (b == 34) {
                        break;
                    }
                    decodeBase64Char = _decodeBase64Escape(base64Variant, (int) b, 0);
                    if (decodeBase64Char < 0) {
                        continue;
                    }
                }
                int i5 = decodeBase64Char;
                if (i3 > length) {
                    i2 += i3;
                    outputStream.write(bArr, 0, i3);
                    i = 0;
                } else {
                    i = i3;
                }
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr3 = this._inputBuffer;
                int i6 = this._inputPtr;
                this._inputPtr = i6 + 1;
                byte b2 = bArr3[i6] & Opcode.TST;
                int decodeBase64Char2 = base64Variant.decodeBase64Char((int) b2);
                if (decodeBase64Char2 < 0) {
                    decodeBase64Char2 = _decodeBase64Escape(base64Variant, (int) b2, 1);
                }
                int i7 = (i5 << 6) | decodeBase64Char2;
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr4 = this._inputBuffer;
                int i8 = this._inputPtr;
                this._inputPtr = i8 + 1;
                byte b3 = bArr4[i8] & Opcode.TST;
                int decodeBase64Char3 = base64Variant.decodeBase64Char((int) b3);
                if (decodeBase64Char3 < 0) {
                    if (decodeBase64Char3 != -2) {
                        if (b3 == 34 && !base64Variant.usesPadding()) {
                            i3 = i + 1;
                            bArr[i] = (byte) (i7 >> 4);
                            break;
                        }
                        decodeBase64Char3 = _decodeBase64Escape(base64Variant, (int) b3, 2);
                    }
                    if (decodeBase64Char3 == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            loadMoreGuaranteed();
                        }
                        byte[] bArr5 = this._inputBuffer;
                        int i9 = this._inputPtr;
                        this._inputPtr = i9 + 1;
                        byte b4 = bArr5[i9] & Opcode.TST;
                        if (!base64Variant.usesPaddingChar((int) b4)) {
                            throw reportInvalidBase64Char(base64Variant, b4, 3, "expected padding character '" + base64Variant.getPaddingChar() + "'");
                        }
                        i3 = i + 1;
                        bArr[i] = (byte) (i7 >> 4);
                    }
                }
                int i10 = (i7 << 6) | decodeBase64Char3;
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr6 = this._inputBuffer;
                int i11 = this._inputPtr;
                this._inputPtr = i11 + 1;
                byte b5 = bArr6[i11] & Opcode.TST;
                int decodeBase64Char4 = base64Variant.decodeBase64Char((int) b5);
                if (decodeBase64Char4 < 0) {
                    if (decodeBase64Char4 != -2) {
                        if (b5 == 34 && !base64Variant.usesPadding()) {
                            int i12 = i10 >> 2;
                            int i13 = i + 1;
                            bArr[i] = (byte) (i12 >> 8);
                            i3 = i13 + 1;
                            bArr[i13] = (byte) i12;
                            break;
                        }
                        decodeBase64Char4 = _decodeBase64Escape(base64Variant, (int) b5, 3);
                    }
                    if (decodeBase64Char4 == -2) {
                        int i14 = i10 >> 2;
                        int i15 = i + 1;
                        bArr[i] = (byte) (i14 >> 8);
                        i3 = i15 + 1;
                        bArr[i15] = (byte) i14;
                    }
                }
                int i16 = (i10 << 6) | decodeBase64Char4;
                int i17 = i + 1;
                bArr[i] = (byte) (i16 >> 16);
                int i18 = i17 + 1;
                bArr[i17] = (byte) (i16 >> 8);
                i3 = i18 + 1;
                bArr[i18] = (byte) i16;
            }
        }
        this._tokenIncomplete = false;
        if (i3 <= 0) {
            return i2;
        }
        int i19 = i2 + i3;
        outputStream.write(bArr, 0, i3);
        return i19;
    }

    public JsonToken nextToken() throws IOException, JsonParseException {
        JsonToken parseNumberText;
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            return _nextAfterName();
        }
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return null;
        }
        this._tokenInputTotal = (this._currInputProcessed + ((long) this._inputPtr)) - 1;
        this._tokenInputRow = this._currInputRow;
        this._tokenInputCol = (this._inputPtr - this._currInputRowStart) - 1;
        this._binaryValue = null;
        if (_skipWSOrEnd == 93) {
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.getParent();
            JsonToken jsonToken = JsonToken.END_ARRAY;
            this._currToken = jsonToken;
            return jsonToken;
        } else if (_skipWSOrEnd == 125) {
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.getParent();
            JsonToken jsonToken2 = JsonToken.END_OBJECT;
            this._currToken = jsonToken2;
            return jsonToken2;
        } else {
            if (this._parsingContext.expectComma()) {
                if (_skipWSOrEnd != 44) {
                    _reportUnexpectedChar(_skipWSOrEnd, "was expecting comma to separate " + this._parsingContext.getTypeDesc() + " entries");
                }
                _skipWSOrEnd = _skipWS();
            }
            if (!this._parsingContext.inObject()) {
                return _nextTokenNotInObject(_skipWSOrEnd);
            }
            this._parsingContext.setCurrentName(_parseFieldName(_skipWSOrEnd).getName());
            this._currToken = JsonToken.FIELD_NAME;
            int _skipWS = _skipWS();
            if (_skipWS != 58) {
                _reportUnexpectedChar(_skipWS, "was expecting a colon to separate field name and value");
            }
            int _skipWS2 = _skipWS();
            if (_skipWS2 == 34) {
                this._tokenIncomplete = true;
                this._nextToken = JsonToken.VALUE_STRING;
                return this._currToken;
            }
            switch (_skipWS2) {
                case 45:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                    parseNumberText = parseNumberText(_skipWS2);
                    break;
                case 91:
                    parseNumberText = JsonToken.START_ARRAY;
                    break;
                case 93:
                case 125:
                    _reportUnexpectedChar(_skipWS2, "expected a value");
                    break;
                case 102:
                    _matchToken("false", 1);
                    parseNumberText = JsonToken.VALUE_FALSE;
                    break;
                case 110:
                    _matchToken("null", 1);
                    parseNumberText = JsonToken.VALUE_NULL;
                    break;
                case 116:
                    break;
                case 123:
                    parseNumberText = JsonToken.START_OBJECT;
                    break;
                default:
                    parseNumberText = _handleUnexpectedValue(_skipWS2);
                    break;
            }
            _matchToken("true", 1);
            parseNumberText = JsonToken.VALUE_TRUE;
            this._nextToken = parseNumberText;
            return this._currToken;
        }
    }

    private JsonToken _nextTokenNotInObject(int i) throws IOException, JsonParseException {
        if (i == 34) {
            this._tokenIncomplete = true;
            JsonToken jsonToken = JsonToken.VALUE_STRING;
            this._currToken = jsonToken;
            return jsonToken;
        }
        switch (i) {
            case 45:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                JsonToken parseNumberText = parseNumberText(i);
                this._currToken = parseNumberText;
                return parseNumberText;
            case 91:
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                JsonToken jsonToken2 = JsonToken.START_ARRAY;
                this._currToken = jsonToken2;
                return jsonToken2;
            case 93:
            case 125:
                _reportUnexpectedChar(i, "expected a value");
                break;
            case 102:
                _matchToken("false", 1);
                JsonToken jsonToken3 = JsonToken.VALUE_FALSE;
                this._currToken = jsonToken3;
                return jsonToken3;
            case 110:
                _matchToken("null", 1);
                JsonToken jsonToken4 = JsonToken.VALUE_NULL;
                this._currToken = jsonToken4;
                return jsonToken4;
            case 116:
                break;
            case 123:
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                JsonToken jsonToken5 = JsonToken.START_OBJECT;
                this._currToken = jsonToken5;
                return jsonToken5;
            default:
                JsonToken _handleUnexpectedValue = _handleUnexpectedValue(i);
                this._currToken = _handleUnexpectedValue;
                return _handleUnexpectedValue;
        }
        _matchToken("true", 1);
        JsonToken jsonToken6 = JsonToken.VALUE_TRUE;
        this._currToken = jsonToken6;
        return jsonToken6;
    }

    private JsonToken _nextAfterName() {
        this._nameCopied = false;
        JsonToken jsonToken = this._nextToken;
        this._nextToken = null;
        if (jsonToken == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
        } else if (jsonToken == JsonToken.START_OBJECT) {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
        }
        this._currToken = jsonToken;
        return jsonToken;
    }

    public void close() throws IOException {
        super.close();
        this._symbols.release();
    }

    public boolean nextFieldName(SerializableString serializableString) throws IOException, JsonParseException {
        int i = 0;
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return false;
        }
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return false;
        }
        this._tokenInputTotal = (this._currInputProcessed + ((long) this._inputPtr)) - 1;
        this._tokenInputRow = this._currInputRow;
        this._tokenInputCol = (this._inputPtr - this._currInputRowStart) - 1;
        this._binaryValue = null;
        if (_skipWSOrEnd == 93) {
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.getParent();
            this._currToken = JsonToken.END_ARRAY;
            return false;
        } else if (_skipWSOrEnd == 125) {
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.getParent();
            this._currToken = JsonToken.END_OBJECT;
            return false;
        } else {
            if (this._parsingContext.expectComma()) {
                if (_skipWSOrEnd != 44) {
                    _reportUnexpectedChar(_skipWSOrEnd, "was expecting comma to separate " + this._parsingContext.getTypeDesc() + " entries");
                }
                _skipWSOrEnd = _skipWS();
            }
            if (!this._parsingContext.inObject()) {
                _nextTokenNotInObject(_skipWSOrEnd);
                return false;
            }
            if (_skipWSOrEnd == 34) {
                byte[] asQuotedUTF8 = serializableString.asQuotedUTF8();
                int length = asQuotedUTF8.length;
                if (this._inputPtr + length < this._inputEnd) {
                    int i2 = this._inputPtr + length;
                    if (this._inputBuffer[i2] == 34) {
                        int i3 = this._inputPtr;
                        while (i != length) {
                            if (asQuotedUTF8[i] == this._inputBuffer[i3 + i]) {
                                i++;
                            }
                        }
                        this._inputPtr = i2 + 1;
                        this._parsingContext.setCurrentName(serializableString.getValue());
                        this._currToken = JsonToken.FIELD_NAME;
                        _isNextTokenNameYes();
                        return true;
                    }
                }
            }
            return _isNextTokenNameMaybe(_skipWSOrEnd, serializableString);
        }
    }

    private void _isNextTokenNameYes() throws IOException, JsonParseException {
        int _skipColon;
        if (this._inputPtr >= this._inputEnd - 1 || this._inputBuffer[this._inputPtr] != 58) {
            _skipColon = _skipColon();
        } else {
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr + 1;
            this._inputPtr = i;
            byte b = bArr[i];
            this._inputPtr++;
            if (b == 34) {
                this._tokenIncomplete = true;
                this._nextToken = JsonToken.VALUE_STRING;
                return;
            } else if (b == 123) {
                this._nextToken = JsonToken.START_OBJECT;
                return;
            } else if (b == 91) {
                this._nextToken = JsonToken.START_ARRAY;
                return;
            } else {
                _skipColon = b & Opcode.TST;
                if (_skipColon <= 32 || _skipColon == 47) {
                    this._inputPtr--;
                    _skipColon = _skipWS();
                }
            }
        }
        switch (_skipColon) {
            case 34:
                this._tokenIncomplete = true;
                this._nextToken = JsonToken.VALUE_STRING;
                return;
            case 45:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                this._nextToken = parseNumberText(_skipColon);
                return;
            case 91:
                this._nextToken = JsonToken.START_ARRAY;
                return;
            case 93:
            case 125:
                _reportUnexpectedChar(_skipColon, "expected a value");
                break;
            case 102:
                _matchToken("false", 1);
                this._nextToken = JsonToken.VALUE_FALSE;
                return;
            case 110:
                _matchToken("null", 1);
                this._nextToken = JsonToken.VALUE_NULL;
                return;
            case 116:
                break;
            case 123:
                this._nextToken = JsonToken.START_OBJECT;
                return;
            default:
                this._nextToken = _handleUnexpectedValue(_skipColon);
                return;
        }
        _matchToken("true", 1);
        this._nextToken = JsonToken.VALUE_TRUE;
    }

    private boolean _isNextTokenNameMaybe(int i, SerializableString serializableString) throws IOException, JsonParseException {
        JsonToken parseNumberText;
        String name = _parseFieldName(i).getName();
        this._parsingContext.setCurrentName(name);
        boolean equals = name.equals(serializableString.getValue());
        this._currToken = JsonToken.FIELD_NAME;
        int _skipWS = _skipWS();
        if (_skipWS != 58) {
            _reportUnexpectedChar(_skipWS, "was expecting a colon to separate field name and value");
        }
        int _skipWS2 = _skipWS();
        if (_skipWS2 == 34) {
            this._tokenIncomplete = true;
            this._nextToken = JsonToken.VALUE_STRING;
            return equals;
        }
        switch (_skipWS2) {
            case 45:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                parseNumberText = parseNumberText(_skipWS2);
                break;
            case 91:
                parseNumberText = JsonToken.START_ARRAY;
                break;
            case 93:
            case 125:
                _reportUnexpectedChar(_skipWS2, "expected a value");
                break;
            case 102:
                _matchToken("false", 1);
                parseNumberText = JsonToken.VALUE_FALSE;
                break;
            case 110:
                _matchToken("null", 1);
                parseNumberText = JsonToken.VALUE_NULL;
                break;
            case 116:
                break;
            case 123:
                parseNumberText = JsonToken.START_OBJECT;
                break;
            default:
                parseNumberText = _handleUnexpectedValue(_skipWS2);
                break;
        }
        _matchToken("true", 1);
        parseNumberText = JsonToken.VALUE_TRUE;
        this._nextToken = parseNumberText;
        return equals;
    }

    public String nextTextValue() throws IOException, JsonParseException {
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_STRING) {
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                }
                return this._textBuffer.contentsAsString();
            } else if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            } else if (jsonToken != JsonToken.START_OBJECT) {
                return null;
            } else {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            }
        } else if (nextToken() == JsonToken.VALUE_STRING) {
            return getText();
        } else {
            return null;
        }
    }

    public int nextIntValue(int i) throws IOException, JsonParseException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return nextToken() == JsonToken.VALUE_NUMBER_INT ? getIntValue() : i;
        }
        this._nameCopied = false;
        JsonToken jsonToken = this._nextToken;
        this._nextToken = null;
        this._currToken = jsonToken;
        if (jsonToken == JsonToken.VALUE_NUMBER_INT) {
            return getIntValue();
        }
        if (jsonToken == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            return i;
        } else if (jsonToken != JsonToken.START_OBJECT) {
            return i;
        } else {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            return i;
        }
    }

    public long nextLongValue(long j) throws IOException, JsonParseException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return nextToken() == JsonToken.VALUE_NUMBER_INT ? getLongValue() : j;
        }
        this._nameCopied = false;
        JsonToken jsonToken = this._nextToken;
        this._nextToken = null;
        this._currToken = jsonToken;
        if (jsonToken == JsonToken.VALUE_NUMBER_INT) {
            return getLongValue();
        }
        if (jsonToken == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            return j;
        } else if (jsonToken != JsonToken.START_OBJECT) {
            return j;
        } else {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            return j;
        }
    }

    public Boolean nextBooleanValue() throws IOException, JsonParseException {
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (jsonToken == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            } else if (jsonToken != JsonToken.START_OBJECT) {
                return null;
            } else {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            }
        } else {
            switch (nextToken()) {
                case VALUE_TRUE:
                    return Boolean.TRUE;
                case VALUE_FALSE:
                    return Boolean.FALSE;
                default:
                    return null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public JsonToken parseNumberText(int i) throws IOException, JsonParseException {
        int i2;
        int i3;
        int i4 = 1;
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        boolean z = i == 45;
        if (z) {
            emptyAndGetCurrentSegment[0] = '-';
            if (this._inputPtr >= this._inputEnd) {
                loadMoreGuaranteed();
            }
            byte[] bArr = this._inputBuffer;
            int i5 = this._inputPtr;
            this._inputPtr = i5 + 1;
            i3 = bArr[i5] & Opcode.TST;
            if (i3 < 48 || i3 > 57) {
                return _handleInvalidNumberStart(i3, true);
            }
            i2 = 1;
        } else {
            i2 = 0;
            i3 = i;
        }
        if (i3 == 48) {
            i3 = _verifyNoLeadingZeroes();
        }
        int i6 = i2 + 1;
        emptyAndGetCurrentSegment[i2] = (char) i3;
        int length = this._inputPtr + emptyAndGetCurrentSegment.length;
        if (length > this._inputEnd) {
            length = this._inputEnd;
        }
        while (this._inputPtr < length) {
            byte[] bArr2 = this._inputBuffer;
            int i7 = this._inputPtr;
            this._inputPtr = i7 + 1;
            byte b = bArr2[i7] & Opcode.TST;
            if (b >= 48 && b <= 57) {
                i4++;
                int i8 = i6 + 1;
                emptyAndGetCurrentSegment[i6] = (char) b;
                i6 = i8;
            } else if (b == 46 || b == 101 || b == 69) {
                return _parseFloatText(emptyAndGetCurrentSegment, i6, b, z, i4);
            } else {
                this._inputPtr--;
                this._textBuffer.setCurrentLength(i6);
                return resetInt(z, i4);
            }
        }
        return _parserNumber2(emptyAndGetCurrentSegment, i6, z, i4);
    }

    private JsonToken _parserNumber2(char[] cArr, int i, boolean z, int i2) throws IOException, JsonParseException {
        byte b;
        int i3 = i2;
        int i4 = i;
        char[] cArr2 = cArr;
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i5 = this._inputPtr;
                this._inputPtr = i5 + 1;
                b = bArr[i5] & Opcode.TST;
                if (b <= 57 && b >= 48) {
                    if (i4 >= cArr2.length) {
                        cArr2 = this._textBuffer.finishCurrentSegment();
                        i4 = 0;
                    }
                    int i6 = i4;
                    i4 = i6 + 1;
                    cArr2[i6] = (char) b;
                    i3++;
                }
            } else {
                this._textBuffer.setCurrentLength(i4);
                return resetInt(z, i3);
            }
        }
        if (b == 46 || b == 101 || b == 69) {
            return _parseFloatText(cArr2, i4, b, z, i3);
        }
        this._inputPtr--;
        this._textBuffer.setCurrentLength(i4);
        return resetInt(z, i3);
    }

    private int _verifyNoLeadingZeroes() throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            return 48;
        }
        byte b = this._inputBuffer[this._inputPtr] & Opcode.TST;
        if (b < 48 || b > 57) {
            return 48;
        }
        if (!isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        this._inputPtr++;
        if (b != 48) {
            return b;
        }
        do {
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                return b;
            }
            b = this._inputBuffer[this._inputPtr] & Opcode.TST;
            if (b < 48 || b > 57) {
                return 48;
            }
            this._inputPtr++;
        } while (b == 48);
        return b;
    }

    private JsonToken _parseFloatText(char[] cArr, int i, int i2, boolean z, int i3) throws IOException, JsonParseException {
        int i4;
        char[] cArr2;
        int i5;
        int i6;
        boolean z2;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11 = 0;
        boolean z3 = false;
        if (i2 == 46) {
            int i12 = i + 1;
            cArr[i] = (char) i2;
            while (true) {
                if (this._inputPtr >= this._inputEnd && !loadMore()) {
                    z3 = true;
                    break;
                }
                byte[] bArr = this._inputBuffer;
                int i13 = this._inputPtr;
                this._inputPtr = i13 + 1;
                i2 = bArr[i13] & Opcode.TST;
                if (i2 < 48 || i2 > 57) {
                    break;
                }
                i11++;
                if (i12 >= cArr.length) {
                    cArr = this._textBuffer.finishCurrentSegment();
                    i12 = 0;
                }
                int i14 = i12;
                i12 = i14 + 1;
                cArr[i14] = (char) i2;
            }
            if (i11 == 0) {
                reportUnexpectedNumberChar(i2, "Decimal point not followed by a digit");
            }
            i4 = i11;
            i5 = i12;
            cArr2 = cArr;
        } else {
            i4 = 0;
            cArr2 = cArr;
            i5 = i;
        }
        if (i2 == 101 || i2 == 69) {
            if (i5 >= cArr2.length) {
                cArr2 = this._textBuffer.finishCurrentSegment();
                i5 = 0;
            }
            int i15 = i5 + 1;
            cArr2[i5] = (char) i2;
            if (this._inputPtr >= this._inputEnd) {
                loadMoreGuaranteed();
            }
            byte[] bArr2 = this._inputBuffer;
            int i16 = this._inputPtr;
            this._inputPtr = i16 + 1;
            byte b = bArr2[i16] & Opcode.TST;
            if (b == 45 || b == 43) {
                if (i15 >= cArr2.length) {
                    cArr2 = this._textBuffer.finishCurrentSegment();
                    i10 = 0;
                } else {
                    i10 = i15;
                }
                int i17 = i10 + 1;
                cArr2[i10] = (char) b;
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr3 = this._inputBuffer;
                int i18 = this._inputPtr;
                this._inputPtr = i18 + 1;
                b = bArr3[i18] & Opcode.TST;
                i9 = i17;
                i8 = 0;
            } else {
                i9 = i15;
                i8 = 0;
            }
            while (true) {
                if (b <= 57 && b >= 48) {
                    i8++;
                    if (i9 >= cArr2.length) {
                        cArr2 = this._textBuffer.finishCurrentSegment();
                        i9 = 0;
                    }
                    int i19 = i9 + 1;
                    cArr2[i9] = (char) b;
                    if (this._inputPtr >= this._inputEnd && !loadMore()) {
                        i7 = i8;
                        z2 = true;
                        i6 = i19;
                        break;
                    }
                    byte[] bArr4 = this._inputBuffer;
                    int i20 = this._inputPtr;
                    this._inputPtr = i20 + 1;
                    b = bArr4[i20] & Opcode.TST;
                    i9 = i19;
                } else {
                    z2 = z3;
                    int i21 = i8;
                    i6 = i9;
                    i7 = i21;
                }
            }
            z2 = z3;
            int i212 = i8;
            i6 = i9;
            i7 = i212;
            if (i7 == 0) {
                reportUnexpectedNumberChar(b, "Exponent indicator not followed by a digit");
            }
        } else {
            z2 = z3;
            i6 = i5;
            i7 = 0;
        }
        if (!z2) {
            this._inputPtr--;
        }
        this._textBuffer.setCurrentLength(i6);
        return resetFloat(z, i3, i4, i7);
    }

    /* access modifiers changed from: protected */
    public Name _parseFieldName(int i) throws IOException, JsonParseException {
        if (i != 34) {
            return _handleUnusualFieldName(i);
        }
        if (this._inputPtr + 9 > this._inputEnd) {
            return slowParseFieldName();
        }
        byte[] bArr = this._inputBuffer;
        int[] iArr = sInputCodesLatin1;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2] & Opcode.TST;
        if (iArr[b] == 0) {
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            byte b2 = bArr[i3] & Opcode.TST;
            if (iArr[b2] == 0) {
                byte b3 = (b << 8) | b2;
                int i4 = this._inputPtr;
                this._inputPtr = i4 + 1;
                byte b4 = bArr[i4] & Opcode.TST;
                if (iArr[b4] == 0) {
                    byte b5 = (b3 << 8) | b4;
                    int i5 = this._inputPtr;
                    this._inputPtr = i5 + 1;
                    byte b6 = bArr[i5] & Opcode.TST;
                    if (iArr[b6] == 0) {
                        byte b7 = (b5 << 8) | b6;
                        int i6 = this._inputPtr;
                        this._inputPtr = i6 + 1;
                        byte b8 = bArr[i6] & Opcode.TST;
                        if (iArr[b8] == 0) {
                            this._quad1 = b7;
                            return parseMediumFieldName(b8, iArr);
                        } else if (b8 == 34) {
                            return findName(b7, 4);
                        } else {
                            return parseFieldName(b7, b8, 4);
                        }
                    } else if (b6 == 34) {
                        return findName(b5, 3);
                    } else {
                        return parseFieldName(b5, b6, 3);
                    }
                } else if (b4 == 34) {
                    return findName(b3, 2);
                } else {
                    return parseFieldName(b3, b4, 2);
                }
            } else if (b2 == 34) {
                return findName(b, 1);
            } else {
                return parseFieldName(b, b2, 1);
            }
        } else if (b == 34) {
            return BytesToNameCanonicalizer.getEmptyName();
        } else {
            return parseFieldName(0, b, 0);
        }
    }

    /* access modifiers changed from: protected */
    public Name parseMediumFieldName(int i, int[] iArr) throws IOException, JsonParseException {
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2] & Opcode.TST;
        if (iArr[b] == 0) {
            byte b2 = b | (i << 8);
            byte[] bArr2 = this._inputBuffer;
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            byte b3 = bArr2[i3] & Opcode.TST;
            if (iArr[b3] == 0) {
                byte b4 = (b2 << 8) | b3;
                byte[] bArr3 = this._inputBuffer;
                int i4 = this._inputPtr;
                this._inputPtr = i4 + 1;
                byte b5 = bArr3[i4] & Opcode.TST;
                if (iArr[b5] == 0) {
                    int i5 = (b4 << 8) | b5;
                    byte[] bArr4 = this._inputBuffer;
                    int i6 = this._inputPtr;
                    this._inputPtr = i6 + 1;
                    byte b6 = bArr4[i6] & Opcode.TST;
                    if (iArr[b6] == 0) {
                        this._quadBuffer[0] = this._quad1;
                        this._quadBuffer[1] = i5;
                        return parseLongFieldName(b6);
                    } else if (b6 == 34) {
                        return findName(this._quad1, i5, 4);
                    } else {
                        return parseFieldName(this._quad1, i5, b6, 4);
                    }
                } else if (b5 == 34) {
                    return findName(this._quad1, b4, 3);
                } else {
                    return parseFieldName(this._quad1, b4, b5, 3);
                }
            } else if (b3 == 34) {
                return findName(this._quad1, b2, 2);
            } else {
                return parseFieldName(this._quad1, b2, b3, 2);
            }
        } else if (b == 34) {
            return findName(this._quad1, i, 1);
        } else {
            return parseFieldName(this._quad1, i, b, 1);
        }
    }

    /* access modifiers changed from: protected */
    public Name parseLongFieldName(int i) throws IOException, JsonParseException {
        int[] iArr = sInputCodesLatin1;
        int i2 = 2;
        byte b = i;
        while (this._inputEnd - this._inputPtr >= 4) {
            byte[] bArr = this._inputBuffer;
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            byte b2 = bArr[i3] & Opcode.TST;
            if (iArr[b2] == 0) {
                byte b3 = (b << 8) | b2;
                byte[] bArr2 = this._inputBuffer;
                int i4 = this._inputPtr;
                this._inputPtr = i4 + 1;
                byte b4 = bArr2[i4] & Opcode.TST;
                if (iArr[b4] == 0) {
                    byte b5 = (b3 << 8) | b4;
                    byte[] bArr3 = this._inputBuffer;
                    int i5 = this._inputPtr;
                    this._inputPtr = i5 + 1;
                    byte b6 = bArr3[i5] & Opcode.TST;
                    if (iArr[b6] == 0) {
                        int i6 = (b5 << 8) | b6;
                        byte[] bArr4 = this._inputBuffer;
                        int i7 = this._inputPtr;
                        this._inputPtr = i7 + 1;
                        b = bArr4[i7] & Opcode.TST;
                        if (iArr[b] == 0) {
                            if (i2 >= this._quadBuffer.length) {
                                this._quadBuffer = growArrayBy(this._quadBuffer, i2);
                            }
                            int i8 = i2 + 1;
                            this._quadBuffer[i2] = i6;
                            i2 = i8;
                        } else if (b == 34) {
                            return findName(this._quadBuffer, i2, i6, 4);
                        } else {
                            return parseEscapedFieldName(this._quadBuffer, i2, i6, b, 4);
                        }
                    } else if (b6 == 34) {
                        return findName(this._quadBuffer, i2, b5, 3);
                    } else {
                        return parseEscapedFieldName(this._quadBuffer, i2, b5, b6, 3);
                    }
                } else if (b4 == 34) {
                    return findName(this._quadBuffer, i2, b3, 2);
                } else {
                    return parseEscapedFieldName(this._quadBuffer, i2, b3, b4, 2);
                }
            } else if (b2 == 34) {
                return findName(this._quadBuffer, i2, b, 1);
            } else {
                return parseEscapedFieldName(this._quadBuffer, i2, b, b2, 1);
            }
        }
        return parseEscapedFieldName(this._quadBuffer, i2, 0, b, 0);
    }

    /* access modifiers changed from: protected */
    public Name slowParseFieldName() throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(": was expecting closing '\"' for name");
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        byte b = bArr[i] & Opcode.TST;
        if (b == 34) {
            return BytesToNameCanonicalizer.getEmptyName();
        }
        return parseEscapedFieldName(this._quadBuffer, 0, 0, b, 0);
    }

    private Name parseFieldName(int i, int i2, int i3) throws IOException, JsonParseException {
        return parseEscapedFieldName(this._quadBuffer, 0, i, i2, i3);
    }

    private Name parseFieldName(int i, int i2, int i3, int i4) throws IOException, JsonParseException {
        this._quadBuffer[0] = i;
        return parseEscapedFieldName(this._quadBuffer, 1, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00bb  */
    public Name parseEscapedFieldName(int[] iArr, int i, int i2, int i3, int i4) throws IOException, JsonParseException {
        int i5;
        int[] iArr2;
        int i6;
        int[] iArr3;
        int i7;
        int i8;
        int[] iArr4;
        int i9;
        int i10;
        int i11;
        int[] iArr5;
        int[] iArr6 = sInputCodesLatin1;
        while (true) {
            if (iArr6[i3] != 0) {
                if (i3 == 34) {
                    break;
                }
                if (i3 != 92) {
                    _throwUnquotedSpace(i3, "name");
                } else {
                    i3 = _decodeEscaped();
                }
                if (i3 > 127) {
                    if (i4 >= 4) {
                        if (i >= iArr.length) {
                            iArr = growArrayBy(iArr, iArr.length);
                            this._quadBuffer = iArr;
                        }
                        i6 = i + 1;
                        iArr[i] = i2;
                        i4 = 0;
                        i2 = 0;
                        iArr3 = iArr;
                    } else {
                        i6 = i;
                        iArr3 = iArr;
                    }
                    if (i3 < 2048) {
                        i10 = (i3 >> 6) | FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE | (i2 << 8);
                        iArr5 = iArr3;
                        i11 = i4 + 1;
                    } else {
                        int i12 = (i3 >> 12) | 224 | (i2 << 8);
                        int i13 = i4 + 1;
                        if (i13 >= 4) {
                            if (i6 >= iArr3.length) {
                                iArr3 = growArrayBy(iArr3, iArr3.length);
                                this._quadBuffer = iArr3;
                            }
                            int i14 = i6 + 1;
                            iArr3[i6] = i12;
                            i8 = i14;
                            iArr4 = iArr3;
                            i9 = 0;
                            i7 = 0;
                        } else {
                            int i15 = i13;
                            i7 = i12;
                            i8 = i6;
                            iArr4 = iArr3;
                            i9 = i15;
                        }
                        i10 = (i7 << 8) | ((i3 >> 6) & 63) | 128;
                        i11 = i9 + 1;
                        int i16 = i8;
                        iArr5 = iArr4;
                        i6 = i16;
                    }
                    i2 = (i3 & 63) | 128;
                    i4 = i11;
                    i = i6;
                    iArr2 = iArr5;
                    i5 = i10;
                    if (i4 >= 4) {
                        i4++;
                        i2 |= i5 << 8;
                        iArr = iArr2;
                    } else {
                        if (i >= iArr2.length) {
                            iArr2 = growArrayBy(iArr2, iArr2.length);
                            this._quadBuffer = iArr2;
                        }
                        int i17 = i + 1;
                        iArr2[i] = i5;
                        i4 = 1;
                        i = i17;
                        iArr = iArr2;
                    }
                    if (this._inputPtr >= this._inputEnd && !loadMore()) {
                        _reportInvalidEOF(" in field name");
                    }
                    byte[] bArr = this._inputBuffer;
                    int i18 = this._inputPtr;
                    this._inputPtr = i18 + 1;
                    i3 = bArr[i18] & Opcode.TST;
                }
            }
            i5 = i2;
            iArr2 = iArr;
            i2 = i3;
            if (i4 >= 4) {
            }
            _reportInvalidEOF(" in field name");
            byte[] bArr2 = this._inputBuffer;
            int i182 = this._inputPtr;
            this._inputPtr = i182 + 1;
            i3 = bArr2[i182] & Opcode.TST;
        }
        if (i4 > 0) {
            if (i >= iArr.length) {
                iArr = growArrayBy(iArr, iArr.length);
                this._quadBuffer = iArr;
            }
            int i19 = i + 1;
            iArr[i] = i2;
            i = i19;
        }
        Name findName = this._symbols.findName(iArr, i);
        if (findName == null) {
            return addName(iArr, i, i4);
        }
        return findName;
    }

    /* access modifiers changed from: protected */
    public Name _handleUnusualFieldName(int i) throws IOException, JsonParseException {
        int[] iArr;
        int i2;
        int i3;
        int i4;
        if (i == 39 && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return _parseApostropheFieldName();
        }
        if (!isEnabled(Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            _reportUnexpectedChar(i, "was expecting double-quote to start field name");
        }
        int[] inputCodeUtf8JsNames = CharTypes.getInputCodeUtf8JsNames();
        if (inputCodeUtf8JsNames[i] != 0) {
            _reportUnexpectedChar(i, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }
        int i5 = 0;
        int i6 = 0;
        int i7 = i;
        int i8 = 0;
        int[] iArr2 = this._quadBuffer;
        while (true) {
            if (i5 < 4) {
                int i9 = i5 + 1;
                i3 = i7 | (i6 << 8);
                i4 = i8;
                iArr = iArr2;
                i2 = i9;
            } else {
                if (i8 >= iArr2.length) {
                    iArr2 = growArrayBy(iArr2, iArr2.length);
                    this._quadBuffer = iArr2;
                }
                int i10 = i8 + 1;
                iArr2[i8] = i6;
                iArr = iArr2;
                i2 = 1;
                i3 = i7;
                i4 = i10;
            }
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                _reportInvalidEOF(" in field name");
            }
            byte b = this._inputBuffer[this._inputPtr] & Opcode.TST;
            if (inputCodeUtf8JsNames[b] != 0) {
                break;
            }
            this._inputPtr++;
            i6 = i3;
            i5 = i2;
            iArr2 = iArr;
            i8 = i4;
            i7 = b;
        }
        if (i2 > 0) {
            if (i4 >= iArr.length) {
                iArr = growArrayBy(iArr, iArr.length);
                this._quadBuffer = iArr;
            }
            int i11 = i4 + 1;
            iArr[i4] = i3;
            i4 = i11;
        }
        Name findName = this._symbols.findName(iArr, i4);
        if (findName == null) {
            return addName(iArr, i4, i2);
        }
        return findName;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00f3  */
    public Name _parseApostropheFieldName() throws IOException, JsonParseException {
        int[] iArr;
        int i;
        int i2;
        int i3;
        int i4;
        byte b;
        int[] iArr2;
        int i5;
        byte b2;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int[] iArr3;
        int i11;
        int i12;
        int i13;
        int[] iArr4;
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(": was expecting closing ''' for name");
        }
        byte[] bArr = this._inputBuffer;
        int i14 = this._inputPtr;
        this._inputPtr = i14 + 1;
        char c = bArr[i14] & 255;
        if (c == '\'') {
            return BytesToNameCanonicalizer.getEmptyName();
        }
        int[] iArr5 = this._quadBuffer;
        int[] iArr6 = sInputCodesLatin1;
        int i15 = 0;
        int i16 = 0;
        int i17 = 0;
        while (c != '\'') {
            if (!(c == '\"' || iArr6[c] == 0)) {
                if (c != '\\') {
                    _throwUnquotedSpace(c, "name");
                } else {
                    c = _decodeEscaped();
                }
                if (c > 127) {
                    if (i15 >= 4) {
                        if (i17 >= iArr5.length) {
                            iArr5 = growArrayBy(iArr5, iArr5.length);
                            this._quadBuffer = iArr5;
                        }
                        int i18 = i17 + 1;
                        iArr5[i17] = i16;
                        i8 = 0;
                        i3 = i18;
                        i7 = 0;
                    } else {
                        int i19 = i15;
                        i7 = i16;
                        i3 = i17;
                        i8 = i19;
                    }
                    if (c < 2048) {
                        int i20 = i8 + 1;
                        i12 = (i7 << 8) | (c >> 6) | FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE;
                        iArr4 = iArr5;
                        i13 = i20;
                    } else {
                        int i21 = (i7 << 8) | (c >> 12) | 224;
                        int i22 = i8 + 1;
                        if (i22 >= 4) {
                            if (i3 >= iArr5.length) {
                                iArr5 = growArrayBy(iArr5, iArr5.length);
                                this._quadBuffer = iArr5;
                            }
                            int i23 = i3 + 1;
                            iArr5[i3] = i21;
                            i10 = i23;
                            iArr3 = iArr5;
                            i11 = 0;
                            i9 = 0;
                        } else {
                            int i24 = i22;
                            i9 = i21;
                            i10 = i3;
                            iArr3 = iArr5;
                            i11 = i24;
                        }
                        i12 = (i9 << 8) | ((c >> 6) & 63) | 128;
                        i13 = i11 + 1;
                        int i25 = i10;
                        iArr4 = iArr3;
                        i3 = i25;
                    }
                    i2 = i12;
                    i4 = i13;
                    iArr5 = iArr4;
                    b = (c & '?') | 128;
                    if (i4 >= 4) {
                        int i26 = i4 + 1;
                        b2 = b | (i2 << 8);
                        i6 = i3;
                        iArr2 = iArr5;
                        i5 = i26;
                    } else {
                        if (i3 >= iArr5.length) {
                            iArr5 = growArrayBy(iArr5, iArr5.length);
                            this._quadBuffer = iArr5;
                        }
                        int i27 = i3 + 1;
                        iArr5[i3] = i2;
                        iArr2 = iArr5;
                        i5 = 1;
                        b2 = b;
                        i6 = i27;
                    }
                    if (this._inputPtr >= this._inputEnd && !loadMore()) {
                        _reportInvalidEOF(" in field name");
                    }
                    byte[] bArr2 = this._inputBuffer;
                    int i28 = this._inputPtr;
                    this._inputPtr = i28 + 1;
                    c = bArr2[i28] & 255;
                    int i29 = i5;
                    iArr5 = iArr2;
                    i16 = b2;
                    i17 = i6;
                    i15 = i29;
                }
            }
            i2 = i16;
            i3 = i17;
            i4 = i15;
            b = c;
            if (i4 >= 4) {
            }
            _reportInvalidEOF(" in field name");
            byte[] bArr22 = this._inputBuffer;
            int i282 = this._inputPtr;
            this._inputPtr = i282 + 1;
            c = bArr22[i282] & 255;
            int i292 = i5;
            iArr5 = iArr2;
            i16 = b2;
            i17 = i6;
            i15 = i292;
        }
        if (i15 > 0) {
            if (i17 >= iArr5.length) {
                iArr5 = growArrayBy(iArr5, iArr5.length);
                this._quadBuffer = iArr5;
            }
            int i30 = i17 + 1;
            iArr5[i17] = i16;
            int i31 = i30;
            iArr = iArr5;
            i = i31;
        } else {
            iArr = iArr5;
            i = i17;
        }
        Name findName = this._symbols.findName(iArr, i);
        if (findName == null) {
            return addName(iArr, i, i15);
        }
        return findName;
    }

    private Name findName(int i, int i2) throws JsonParseException {
        Name findName = this._symbols.findName(i);
        if (findName != null) {
            return findName;
        }
        this._quadBuffer[0] = i;
        return addName(this._quadBuffer, 1, i2);
    }

    private Name findName(int i, int i2, int i3) throws JsonParseException {
        Name findName = this._symbols.findName(i, i2);
        if (findName != null) {
            return findName;
        }
        this._quadBuffer[0] = i;
        this._quadBuffer[1] = i2;
        return addName(this._quadBuffer, 2, i3);
    }

    private Name findName(int[] iArr, int i, int i2, int i3) throws JsonParseException {
        if (i >= iArr.length) {
            iArr = growArrayBy(iArr, iArr.length);
            this._quadBuffer = iArr;
        }
        int i4 = i + 1;
        iArr[i] = i2;
        Name findName = this._symbols.findName(iArr, i4);
        if (findName == null) {
            return addName(iArr, i4, i3);
        }
        return findName;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00d1 A[SYNTHETIC] */
    private Name addName(int[] iArr, int i, int i2) throws JsonParseException {
        int i3;
        char[] cArr;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9 = ((i << 2) - 4) + i2;
        if (i2 < 4) {
            i3 = iArr[i - 1];
            iArr[i - 1] = i3 << ((4 - i2) << 3);
        } else {
            i3 = 0;
        }
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int i10 = 0;
        int i11 = 0;
        while (i11 < i9) {
            int i12 = (iArr[i11 >> 2] >> ((3 - (i11 & 3)) << 3)) & 255;
            int i13 = i11 + 1;
            if (i12 > 127) {
                if ((i12 & 224) == 192) {
                    i7 = i12 & 31;
                    i8 = 1;
                } else if ((i12 & 240) == 224) {
                    i7 = i12 & 15;
                    i8 = 2;
                } else if ((i12 & 248) == 240) {
                    i7 = i12 & 7;
                    i8 = 3;
                } else {
                    _reportInvalidInitial(i12);
                    i7 = 1;
                    i8 = 1;
                }
                if (i13 + i8 > i9) {
                    _reportInvalidEOF(" in field name");
                }
                int i14 = iArr[i13 >> 2] >> ((3 - (i13 & 3)) << 3);
                i13++;
                if ((i14 & FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE) != 128) {
                    _reportInvalidOther(i14);
                }
                i12 = (i7 << 6) | (i14 & 63);
                if (i8 > 1) {
                    int i15 = iArr[i13 >> 2] >> ((3 - (i13 & 3)) << 3);
                    i13++;
                    if ((i15 & FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE) != 128) {
                        _reportInvalidOther(i15);
                    }
                    i12 = (i12 << 6) | (i15 & 63);
                    if (i8 > 2) {
                        int i16 = iArr[i13 >> 2] >> ((3 - (i13 & 3)) << 3);
                        i13++;
                        if ((i16 & FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE) != 128) {
                            _reportInvalidOther(i16 & 255);
                        }
                        i12 = (i12 << 6) | (i16 & 63);
                    }
                }
                if (i8 > 2) {
                    int i17 = i12 - 65536;
                    if (i10 >= emptyAndGetCurrentSegment.length) {
                        emptyAndGetCurrentSegment = this._textBuffer.expandCurrentSegment();
                    }
                    int i18 = i10 + 1;
                    emptyAndGetCurrentSegment[i10] = (char) (55296 + (i17 >> 10));
                    int i19 = (i17 & 1023) | 56320;
                    i5 = i13;
                    i6 = i18;
                    cArr = emptyAndGetCurrentSegment;
                    i4 = i19;
                    if (i6 < cArr.length) {
                        cArr = this._textBuffer.expandCurrentSegment();
                    }
                    i10 = i6 + 1;
                    cArr[i6] = (char) i4;
                    i11 = i5;
                    emptyAndGetCurrentSegment = cArr;
                }
            }
            cArr = emptyAndGetCurrentSegment;
            i4 = i12;
            i5 = i13;
            i6 = i10;
            if (i6 < cArr.length) {
            }
            i10 = i6 + 1;
            cArr[i6] = (char) i4;
            i11 = i5;
            emptyAndGetCurrentSegment = cArr;
        }
        String str = new String(emptyAndGetCurrentSegment, 0, i10);
        if (i2 < 4) {
            iArr[i - 1] = i3;
        }
        return this._symbols.addName(str, iArr, i);
    }

    /* access modifiers changed from: protected */
    public void _finishString() throws IOException, JsonParseException {
        int i = this._inputPtr;
        if (i >= this._inputEnd) {
            loadMoreGuaranteed();
            i = this._inputPtr;
        }
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = sInputCodesUtf8;
        int min = Math.min(this._inputEnd, emptyAndGetCurrentSegment.length + i);
        byte[] bArr = this._inputBuffer;
        int i2 = i;
        int i3 = 0;
        while (true) {
            if (i2 >= min) {
                break;
            }
            byte b = bArr[i2] & Opcode.TST;
            if (iArr[b] == 0) {
                int i4 = i2 + 1;
                int i5 = i3 + 1;
                emptyAndGetCurrentSegment[i3] = (char) b;
                i3 = i5;
                i2 = i4;
            } else if (b == 34) {
                this._inputPtr = i2 + 1;
                this._textBuffer.setCurrentLength(i3);
                return;
            }
        }
        this._inputPtr = i2;
        _finishString2(emptyAndGetCurrentSegment, i3);
    }

    private void _finishString2(char[] cArr, int i) throws IOException, JsonParseException {
        int i2;
        int[] iArr = sInputCodesUtf8;
        byte[] bArr = this._inputBuffer;
        while (true) {
            int i3 = this._inputPtr;
            if (i3 >= this._inputEnd) {
                loadMoreGuaranteed();
                i3 = this._inputPtr;
            }
            if (i >= cArr.length) {
                cArr = this._textBuffer.finishCurrentSegment();
                i = 0;
            }
            int min = Math.min(this._inputEnd, (cArr.length - i) + i3);
            while (true) {
                if (i3 < min) {
                    int i4 = i3 + 1;
                    int i5 = bArr[i3] & Opcode.TST;
                    if (iArr[i5] != 0) {
                        this._inputPtr = i4;
                        if (i5 == 34) {
                            this._textBuffer.setCurrentLength(i);
                            return;
                        }
                        switch (iArr[i5]) {
                            case 1:
                                i5 = _decodeEscaped();
                                break;
                            case 2:
                                i5 = _decodeUtf8_2(i5);
                                break;
                            case 3:
                                if (this._inputEnd - this._inputPtr < 2) {
                                    i5 = _decodeUtf8_3(i5);
                                    break;
                                } else {
                                    i5 = _decodeUtf8_3fast(i5);
                                    break;
                                }
                            case 4:
                                int _decodeUtf8_4 = _decodeUtf8_4(i5);
                                int i6 = i + 1;
                                cArr[i] = (char) (55296 | (_decodeUtf8_4 >> 10));
                                if (i6 >= cArr.length) {
                                    cArr = this._textBuffer.finishCurrentSegment();
                                    i6 = 0;
                                }
                                i = i6;
                                i5 = (_decodeUtf8_4 & 1023) | 56320;
                                break;
                            default:
                                if (i5 >= 32) {
                                    _reportInvalidChar(i5);
                                    break;
                                } else {
                                    _throwUnquotedSpace(i5, "string value");
                                    break;
                                }
                        }
                        if (i >= cArr.length) {
                            cArr = this._textBuffer.finishCurrentSegment();
                            i2 = 0;
                        } else {
                            i2 = i;
                        }
                        i = i2 + 1;
                        cArr[i2] = (char) i5;
                    } else {
                        int i7 = i + 1;
                        cArr[i] = (char) i5;
                        i3 = i4;
                        i = i7;
                    }
                } else {
                    this._inputPtr = i3;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void _skipString() throws IOException, JsonParseException {
        this._tokenIncomplete = false;
        int[] iArr = sInputCodesUtf8;
        byte[] bArr = this._inputBuffer;
        while (true) {
            int i = this._inputPtr;
            int i2 = this._inputEnd;
            if (i >= i2) {
                loadMoreGuaranteed();
                i = this._inputPtr;
                i2 = this._inputEnd;
            }
            while (true) {
                if (i < i2) {
                    int i3 = i + 1;
                    byte b = bArr[i] & Opcode.TST;
                    if (iArr[b] != 0) {
                        this._inputPtr = i3;
                        if (b != 34) {
                            switch (iArr[b]) {
                                case 1:
                                    _decodeEscaped();
                                    break;
                                case 2:
                                    _skipUtf8_2(b);
                                    break;
                                case 3:
                                    _skipUtf8_3(b);
                                    break;
                                case 4:
                                    _skipUtf8_4(b);
                                    break;
                                default:
                                    if (b >= 32) {
                                        _reportInvalidChar(b);
                                        break;
                                    } else {
                                        _throwUnquotedSpace(b, "string value");
                                        break;
                                    }
                            }
                        } else {
                            return;
                        }
                    } else {
                        i = i3;
                    }
                } else {
                    this._inputPtr = i;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public JsonToken _handleUnexpectedValue(int i) throws IOException, JsonParseException {
        switch (i) {
            case 39:
                if (isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
                    return _handleApostropheValue();
                }
                break;
            case 43:
                if (this._inputPtr >= this._inputEnd && !loadMore()) {
                    _reportInvalidEOFInValue();
                }
                byte[] bArr = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                return _handleInvalidNumberStart(bArr[i2] & Opcode.TST, false);
            case 78:
                _matchToken("NaN", 1);
                if (!isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    _reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                    break;
                } else {
                    return resetAsNaN("NaN", Double.NaN);
                }
        }
        _reportUnexpectedChar(i, "expected a valid value (number, String, array, object, 'true', 'false' or 'null')");
        return null;
    }

    /* access modifiers changed from: protected */
    public JsonToken _handleApostropheValue() throws IOException, JsonParseException {
        int i;
        int i2;
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = sInputCodesUtf8;
        byte[] bArr = this._inputBuffer;
        int i3 = 0;
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                loadMoreGuaranteed();
            }
            if (i3 >= emptyAndGetCurrentSegment.length) {
                emptyAndGetCurrentSegment = this._textBuffer.finishCurrentSegment();
                i3 = 0;
            }
            int i4 = this._inputEnd;
            int length = this._inputPtr + (emptyAndGetCurrentSegment.length - i3);
            if (length >= i4) {
                length = i4;
            }
            while (true) {
                if (this._inputPtr < length) {
                    int i5 = this._inputPtr;
                    this._inputPtr = i5 + 1;
                    byte b = bArr[i5] & Opcode.TST;
                    if (b != 39 && iArr[b] == 0) {
                        int i6 = i3 + 1;
                        emptyAndGetCurrentSegment[i3] = (char) b;
                        i3 = i6;
                    } else if (b == 39) {
                        this._textBuffer.setCurrentLength(i3);
                        return JsonToken.VALUE_STRING;
                    } else {
                        switch (iArr[b]) {
                            case 1:
                                if (b != 34) {
                                    i = _decodeEscaped();
                                    break;
                                }
                            case 2:
                                i = _decodeUtf8_2(b);
                                break;
                            case 3:
                                if (this._inputEnd - this._inputPtr < 2) {
                                    i = _decodeUtf8_3(b);
                                    break;
                                } else {
                                    i = _decodeUtf8_3fast(b);
                                    break;
                                }
                            case 4:
                                int _decodeUtf8_4 = _decodeUtf8_4(b);
                                int i7 = i3 + 1;
                                emptyAndGetCurrentSegment[i3] = (char) (55296 | (_decodeUtf8_4 >> 10));
                                if (i7 >= emptyAndGetCurrentSegment.length) {
                                    emptyAndGetCurrentSegment = this._textBuffer.finishCurrentSegment();
                                    i3 = 0;
                                } else {
                                    i3 = i7;
                                }
                                i = 56320 | (_decodeUtf8_4 & 1023);
                                break;
                            default:
                                if (b < 32) {
                                    _throwUnquotedSpace(b, "string value");
                                }
                                _reportInvalidChar(b);
                        }
                        i = b;
                        if (i3 >= emptyAndGetCurrentSegment.length) {
                            emptyAndGetCurrentSegment = this._textBuffer.finishCurrentSegment();
                            i2 = 0;
                        } else {
                            i2 = i3;
                        }
                        i3 = i2 + 1;
                        emptyAndGetCurrentSegment[i2] = (char) i;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public JsonToken _handleInvalidNumberStart(int i, boolean z) throws IOException, JsonParseException {
        int i2;
        String str;
        int i3 = i;
        while (true) {
            if (i3 != 73) {
                i2 = i3;
                break;
            }
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                _reportInvalidEOFInValue();
            }
            byte[] bArr = this._inputBuffer;
            int i4 = this._inputPtr;
            this._inputPtr = i4 + 1;
            byte b = bArr[i4];
            if (b != 78) {
                if (b != 110) {
                    i2 = b;
                    break;
                }
                str = z ? "-Infinity" : "+Infinity";
            } else {
                str = z ? "-INF" : "+INF";
            }
            _matchToken(str, 3);
            if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                return resetAsNaN(str, z ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
            _reportError("Non-standard token '" + str + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            i3 = b;
        }
        reportUnexpectedNumberChar(i2, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    /* access modifiers changed from: protected */
    public void _matchToken(String str, int i) throws IOException, JsonParseException {
        int length = str.length();
        do {
            if ((this._inputPtr >= this._inputEnd && !loadMore()) || this._inputBuffer[this._inputPtr] != str.charAt(i)) {
                _reportInvalidToken(str.substring(0, i));
            }
            this._inputPtr++;
            i++;
        } while (i < length);
        if (this._inputPtr < this._inputEnd || loadMore()) {
            byte b = this._inputBuffer[this._inputPtr] & Opcode.TST;
            if (b >= 48 && b != 93 && b != 125 && Character.isJavaIdentifierPart((char) _decodeCharForError(b))) {
                _reportInvalidToken(str.substring(0, i));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void _reportInvalidToken(String str) throws IOException, JsonParseException {
        _reportInvalidToken(str, "'null', 'true', 'false' or NaN");
    }

    /* access modifiers changed from: protected */
    public void _reportInvalidToken(String str, String str2) throws IOException, JsonParseException {
        StringBuilder sb = new StringBuilder(str);
        while (true) {
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                break;
            }
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            char _decodeCharForError = (char) _decodeCharForError(bArr[i]);
            if (!Character.isJavaIdentifierPart(_decodeCharForError)) {
                break;
            }
            sb.append(_decodeCharForError);
        }
        _reportError("Unrecognized token '" + sb.toString() + "': was expecting " + str2);
    }

    private int _skipWS() throws IOException, JsonParseException {
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                byte b = bArr[i] & Opcode.TST;
                if (b > 32) {
                    if (b != 47) {
                        return b;
                    }
                    _skipComment();
                } else if (b != 32) {
                    if (b == 10) {
                        _skipLF();
                    } else if (b == 13) {
                        _skipCR();
                    } else if (b != 9) {
                        _throwInvalidSpace(b);
                    }
                }
            } else {
                throw _constructError("Unexpected end-of-input within/between " + this._parsingContext.getTypeDesc() + " entries");
            }
        }
    }

    private int _skipWSOrEnd() throws IOException, JsonParseException {
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                byte b = bArr[i] & Opcode.TST;
                if (b > 32) {
                    if (b != 47) {
                        return b;
                    }
                    _skipComment();
                } else if (b != 32) {
                    if (b == 10) {
                        _skipLF();
                    } else if (b == 13) {
                        _skipCR();
                    } else if (b != 9) {
                        _throwInvalidSpace(b);
                    }
                }
            } else {
                _handleEOF();
                return -1;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006c, code lost:
        if (r6._inputPtr < r6._inputEnd) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006e, code lost:
        loadMoreGuaranteed();
     */
    private int _skipColon() throws IOException, JsonParseException {
        byte b;
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        byte b2 = bArr[i];
        if (b2 == 58) {
            if (this._inputPtr < this._inputEnd) {
                b = this._inputBuffer[this._inputPtr] & Opcode.TST;
                if (b > 32 && b != 47) {
                    this._inputPtr++;
                }
            }
            while (true) {
                if (this._inputPtr < this._inputEnd || loadMore()) {
                    byte[] bArr2 = this._inputBuffer;
                    int i2 = this._inputPtr;
                    this._inputPtr = i2 + 1;
                    b = bArr2[i2] & Opcode.TST;
                    if (b > 32) {
                        if (b == 47) {
                            _skipComment();
                        }
                    } else if (b != 32) {
                        if (b == 10) {
                            _skipLF();
                        } else if (b == 13) {
                            _skipCR();
                        } else if (b != 9) {
                            _throwInvalidSpace(b);
                        }
                    }
                } else {
                    throw _constructError("Unexpected end-of-input within/between " + this._parsingContext.getTypeDesc() + " entries");
                }
            }
        } else {
            while (true) {
                switch (r0) {
                    case 9:
                    case 32:
                        break;
                    case 10:
                        _skipLF();
                        break;
                    case 13:
                        _skipCR();
                        break;
                    case 47:
                        _skipComment();
                        break;
                    default:
                        if (r0 < 32) {
                            _throwInvalidSpace(r0);
                        }
                        if (r0 != 58) {
                            _reportUnexpectedChar(r0, "was expecting a colon to separate field name and value");
                            break;
                        }
                        break;
                }
                byte[] bArr3 = this._inputBuffer;
                int i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                b2 = bArr3[i3];
            }
        }
        return b;
    }

    private void _skipComment() throws IOException, JsonParseException {
        if (!isEnabled(Feature.ALLOW_COMMENTS)) {
            _reportUnexpectedChar(47, "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(" in a comment");
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        byte b = bArr[i] & Opcode.TST;
        if (b == 47) {
            _skipCppComment();
        } else if (b == 42) {
            _skipCComment();
        } else {
            _reportUnexpectedChar(b, "was expecting either '*' or '/' for a comment");
        }
    }

    private void _skipCComment() throws IOException, JsonParseException {
        int[] inputCodeComment = CharTypes.getInputCodeComment();
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                byte b = bArr[i] & Opcode.TST;
                int i2 = inputCodeComment[b];
                if (i2 != 0) {
                    switch (i2) {
                        case 2:
                            _skipUtf8_2(b);
                            continue;
                        case 3:
                            _skipUtf8_3(b);
                            continue;
                        case 4:
                            _skipUtf8_4(b);
                            continue;
                        case 10:
                            _skipLF();
                            continue;
                        case 13:
                            _skipCR();
                            continue;
                        case 42:
                            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                                break;
                            } else if (this._inputBuffer[this._inputPtr] == 47) {
                                this._inputPtr++;
                                return;
                            } else {
                                continue;
                            }
                        default:
                            _reportInvalidChar(b);
                            continue;
                    }
                }
            }
        }
        _reportInvalidEOF(" in a comment");
    }

    private void _skipCppComment() throws IOException, JsonParseException {
        int[] inputCodeComment = CharTypes.getInputCodeComment();
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                byte b = bArr[i] & Opcode.TST;
                int i2 = inputCodeComment[b];
                if (i2 != 0) {
                    switch (i2) {
                        case 2:
                            _skipUtf8_2(b);
                            break;
                        case 3:
                            _skipUtf8_3(b);
                            break;
                        case 4:
                            _skipUtf8_4(b);
                            break;
                        case 10:
                            _skipLF();
                            return;
                        case 13:
                            _skipCR();
                            return;
                        case 42:
                            break;
                        default:
                            _reportInvalidChar(b);
                            break;
                    }
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public char _decodeEscaped() throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(" in character escape sequence");
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        byte b = bArr[i];
        switch (b) {
            case 34:
            case 47:
            case 92:
                return (char) b;
            case 98:
                return 8;
            case 102:
                return 12;
            case 110:
                return 10;
            case 114:
                return 13;
            case 116:
                return 9;
            case 117:
                int i2 = 0;
                for (int i3 = 0; i3 < 4; i3++) {
                    if (this._inputPtr >= this._inputEnd && !loadMore()) {
                        _reportInvalidEOF(" in character escape sequence");
                    }
                    byte[] bArr2 = this._inputBuffer;
                    int i4 = this._inputPtr;
                    this._inputPtr = i4 + 1;
                    byte b2 = bArr2[i4];
                    int charToHex = CharTypes.charToHex(b2);
                    if (charToHex < 0) {
                        _reportUnexpectedChar(b2, "expected a hex-digit for character escape sequence");
                    }
                    i2 = (i2 << 4) | charToHex;
                }
                return (char) i2;
            default:
                return _handleUnrecognizedCharacterEscape((char) _decodeCharForError(b));
        }
    }

    /* access modifiers changed from: protected */
    public int _decodeCharForError(int i) throws IOException, JsonParseException {
        char c;
        if (i >= 0) {
            return i;
        }
        if ((i & 224) == 192) {
            i &= 31;
            c = 1;
        } else if ((i & 240) == 224) {
            i &= 15;
            c = 2;
        } else if ((i & 248) == 240) {
            i &= 7;
            c = 3;
        } else {
            _reportInvalidInitial(i & 255);
            c = 1;
        }
        int nextByte = nextByte();
        if ((nextByte & FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE) != 128) {
            _reportInvalidOther(nextByte & 255);
        }
        int i2 = (i << 6) | (nextByte & 63);
        if (c <= 1) {
            return i2;
        }
        int nextByte2 = nextByte();
        if ((nextByte2 & FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE) != 128) {
            _reportInvalidOther(nextByte2 & 255);
        }
        int i3 = (i2 << 6) | (nextByte2 & 63);
        if (c <= 2) {
            return i3;
        }
        int nextByte3 = nextByte();
        if ((nextByte3 & FullScreenVideoUtil.FULLSCREEN_VIDEO_ACTION_PAUSE) != 128) {
            _reportInvalidOther(nextByte3 & 255);
        }
        return (i3 << 6) | (nextByte3 & 63);
    }

    private int _decodeUtf8_2(int i) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & Opcode.FILE) != 128) {
            _reportInvalidOther(b & Opcode.TST, this._inputPtr);
        }
        return (b & Opcode.MOVEF_F) | ((i & 31) << 6);
    }

    private int _decodeUtf8_3(int i) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        int i2 = i & 15;
        byte[] bArr = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b = bArr[i3];
        if ((b & Opcode.FILE) != 128) {
            _reportInvalidOther(b & Opcode.TST, this._inputPtr);
        }
        byte b2 = (i2 << 6) | (b & Opcode.MOVEF_F);
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr2 = this._inputBuffer;
        int i4 = this._inputPtr;
        this._inputPtr = i4 + 1;
        byte b3 = bArr2[i4];
        if ((b3 & Opcode.FILE) != 128) {
            _reportInvalidOther(b3 & Opcode.TST, this._inputPtr);
        }
        return (b2 << 6) | (b3 & Opcode.MOVEF_F);
    }

    private int _decodeUtf8_3fast(int i) throws IOException, JsonParseException {
        int i2 = i & 15;
        byte[] bArr = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b = bArr[i3];
        if ((b & Opcode.FILE) != 128) {
            _reportInvalidOther(b & Opcode.TST, this._inputPtr);
        }
        byte b2 = (i2 << 6) | (b & Opcode.MOVEF_F);
        byte[] bArr2 = this._inputBuffer;
        int i4 = this._inputPtr;
        this._inputPtr = i4 + 1;
        byte b3 = bArr2[i4];
        if ((b3 & Opcode.FILE) != 128) {
            _reportInvalidOther(b3 & Opcode.TST, this._inputPtr);
        }
        return (b2 << 6) | (b3 & Opcode.MOVEF_F);
    }

    private int _decodeUtf8_4(int i) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & Opcode.FILE) != 128) {
            _reportInvalidOther(b & Opcode.TST, this._inputPtr);
        }
        byte b2 = (b & Opcode.MOVEF_F) | ((i & 7) << 6);
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr2 = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b3 = bArr2[i3];
        if ((b3 & Opcode.FILE) != 128) {
            _reportInvalidOther(b3 & Opcode.TST, this._inputPtr);
        }
        byte b4 = (b2 << 6) | (b3 & Opcode.MOVEF_F);
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr3 = this._inputBuffer;
        int i4 = this._inputPtr;
        this._inputPtr = i4 + 1;
        byte b5 = bArr3[i4];
        if ((b5 & Opcode.FILE) != 128) {
            _reportInvalidOther(b5 & Opcode.TST, this._inputPtr);
        }
        return ((b4 << 6) | (b5 & Opcode.MOVEF_F)) - 65536;
    }

    private void _skipUtf8_2(int i) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & Opcode.FILE) != 128) {
            _reportInvalidOther(b & Opcode.TST, this._inputPtr);
        }
    }

    private void _skipUtf8_3(int i) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & Opcode.FILE) != 128) {
            _reportInvalidOther(b & Opcode.TST, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr2 = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b2 = bArr2[i3];
        if ((b2 & Opcode.FILE) != 128) {
            _reportInvalidOther(b2 & Opcode.TST, this._inputPtr);
        }
    }

    private void _skipUtf8_4(int i) throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & Opcode.FILE) != 128) {
            _reportInvalidOther(b & Opcode.TST, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr2 = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b2 = bArr2[i3];
        if ((b2 & Opcode.FILE) != 128) {
            _reportInvalidOther(b2 & Opcode.TST, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr3 = this._inputBuffer;
        int i4 = this._inputPtr;
        this._inputPtr = i4 + 1;
        byte b3 = bArr3[i4];
        if ((b3 & Opcode.FILE) != 128) {
            _reportInvalidOther(b3 & Opcode.TST, this._inputPtr);
        }
    }

    /* access modifiers changed from: protected */
    public void _skipCR() throws IOException {
        if ((this._inputPtr < this._inputEnd || loadMore()) && this._inputBuffer[this._inputPtr] == 10) {
            this._inputPtr++;
        }
        this._currInputRow++;
        this._currInputRowStart = this._inputPtr;
    }

    /* access modifiers changed from: protected */
    public void _skipLF() throws IOException {
        this._currInputRow++;
        this._currInputRowStart = this._inputPtr;
    }

    private int nextByte() throws IOException, JsonParseException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        return bArr[i] & Opcode.TST;
    }

    /* access modifiers changed from: protected */
    public void _reportInvalidChar(int i) throws JsonParseException {
        if (i < 32) {
            _throwInvalidSpace(i);
        }
        _reportInvalidInitial(i);
    }

    /* access modifiers changed from: protected */
    public void _reportInvalidInitial(int i) throws JsonParseException {
        _reportError("Invalid UTF-8 start byte 0x" + Integer.toHexString(i));
    }

    /* access modifiers changed from: protected */
    public void _reportInvalidOther(int i) throws JsonParseException {
        _reportError("Invalid UTF-8 middle byte 0x" + Integer.toHexString(i));
    }

    /* access modifiers changed from: protected */
    public void _reportInvalidOther(int i, int i2) throws JsonParseException {
        this._inputPtr = i2;
        _reportInvalidOther(i);
    }

    public static int[] growArrayBy(int[] iArr, int i) {
        if (iArr == null) {
            return new int[i];
        }
        int length = iArr.length;
        int[] iArr2 = new int[(length + i)];
        System.arraycopy(iArr, 0, iArr2, 0, length);
        return iArr2;
    }

    /* access modifiers changed from: protected */
    public byte[] _decodeBase64(Base64Variant base64Variant) throws IOException, JsonParseException {
        ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                loadMoreGuaranteed();
            }
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            byte b = bArr[i] & Opcode.TST;
            if (b > 32) {
                int decodeBase64Char = base64Variant.decodeBase64Char((int) b);
                if (decodeBase64Char < 0) {
                    if (b == 34) {
                        return _getByteArrayBuilder.toByteArray();
                    }
                    decodeBase64Char = _decodeBase64Escape(base64Variant, (int) b, 0);
                    if (decodeBase64Char < 0) {
                        continue;
                    }
                }
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr2 = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                byte b2 = bArr2[i2] & Opcode.TST;
                int decodeBase64Char2 = base64Variant.decodeBase64Char((int) b2);
                if (decodeBase64Char2 < 0) {
                    decodeBase64Char2 = _decodeBase64Escape(base64Variant, (int) b2, 1);
                }
                int i3 = decodeBase64Char2 | (decodeBase64Char << 6);
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr3 = this._inputBuffer;
                int i4 = this._inputPtr;
                this._inputPtr = i4 + 1;
                byte b3 = bArr3[i4] & Opcode.TST;
                int decodeBase64Char3 = base64Variant.decodeBase64Char((int) b3);
                if (decodeBase64Char3 < 0) {
                    if (decodeBase64Char3 != -2) {
                        if (b3 != 34 || base64Variant.usesPadding()) {
                            decodeBase64Char3 = _decodeBase64Escape(base64Variant, (int) b3, 2);
                        } else {
                            _getByteArrayBuilder.append(i3 >> 4);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (decodeBase64Char3 == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            loadMoreGuaranteed();
                        }
                        byte[] bArr4 = this._inputBuffer;
                        int i5 = this._inputPtr;
                        this._inputPtr = i5 + 1;
                        byte b4 = bArr4[i5] & Opcode.TST;
                        if (!base64Variant.usesPaddingChar((int) b4)) {
                            throw reportInvalidBase64Char(base64Variant, b4, 3, "expected padding character '" + base64Variant.getPaddingChar() + "'");
                        }
                        _getByteArrayBuilder.append(i3 >> 4);
                    }
                }
                int i6 = (i3 << 6) | decodeBase64Char3;
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr5 = this._inputBuffer;
                int i7 = this._inputPtr;
                this._inputPtr = i7 + 1;
                byte b5 = bArr5[i7] & Opcode.TST;
                int decodeBase64Char4 = base64Variant.decodeBase64Char((int) b5);
                if (decodeBase64Char4 < 0) {
                    if (decodeBase64Char4 != -2) {
                        if (b5 != 34 || base64Variant.usesPadding()) {
                            decodeBase64Char4 = _decodeBase64Escape(base64Variant, (int) b5, 3);
                        } else {
                            _getByteArrayBuilder.appendTwoBytes(i6 >> 2);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (decodeBase64Char4 == -2) {
                        _getByteArrayBuilder.appendTwoBytes(i6 >> 2);
                    }
                }
                _getByteArrayBuilder.appendThreeBytes(decodeBase64Char4 | (i6 << 6));
            }
        }
    }
}
