package com.shaded.fasterxml.jackson.databind.deser.impl;

import com.shaded.fasterxml.jackson.core.JsonParser;
import com.shaded.fasterxml.jackson.core.JsonProcessingException;
import com.shaded.fasterxml.jackson.databind.DeserializationContext;
import com.shaded.fasterxml.jackson.databind.JsonDeserializer;
import com.shaded.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.shaded.fasterxml.jackson.databind.util.NameTransformer;
import com.shaded.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnwrappedPropertyHandler {
    protected final List<SettableBeanProperty> _properties;

    public UnwrappedPropertyHandler() {
        this._properties = new ArrayList();
    }

    protected UnwrappedPropertyHandler(List<SettableBeanProperty> list) {
        this._properties = list;
    }

    public void addProperty(SettableBeanProperty settableBeanProperty) {
        this._properties.add(settableBeanProperty);
    }

    public UnwrappedPropertyHandler renameAll(NameTransformer nameTransformer) {
        ArrayList arrayList = new ArrayList(this._properties.size());
        for (SettableBeanProperty settableBeanProperty : this._properties) {
            SettableBeanProperty withName = settableBeanProperty.withName(nameTransformer.transform(settableBeanProperty.getName()));
            JsonDeserializer valueDeserializer = withName.getValueDeserializer();
            if (valueDeserializer != null) {
                JsonDeserializer unwrappingDeserializer = valueDeserializer.unwrappingDeserializer(nameTransformer);
                if (unwrappingDeserializer != valueDeserializer) {
                    withName = withName.withValueDeserializer(unwrappingDeserializer);
                }
            }
            arrayList.add(withName);
        }
        return new UnwrappedPropertyHandler(arrayList);
    }

    public Object processUnwrapped(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj, TokenBuffer tokenBuffer) throws IOException, JsonProcessingException {
        int size = this._properties.size();
        for (int i = 0; i < size; i++) {
            SettableBeanProperty settableBeanProperty = (SettableBeanProperty) this._properties.get(i);
            JsonParser asParser = tokenBuffer.asParser();
            asParser.nextToken();
            settableBeanProperty.deserializeAndSet(asParser, deserializationContext, obj);
        }
        return obj;
    }
}
