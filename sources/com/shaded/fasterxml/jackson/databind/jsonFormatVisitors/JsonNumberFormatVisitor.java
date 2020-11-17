package com.shaded.fasterxml.jackson.databind.jsonFormatVisitors;

import com.shaded.fasterxml.jackson.core.JsonParser.NumberType;

public interface JsonNumberFormatVisitor extends JsonValueFormatVisitor {

    public static class Base extends com.shaded.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormatVisitor.Base implements JsonNumberFormatVisitor {
        public void numberType(NumberType numberType) {
        }
    }

    void numberType(NumberType numberType);
}
