package com.shaded.fasterxml.jackson.databind.deser.impl;

import com.shaded.fasterxml.jackson.databind.BeanProperty.Std;
import com.shaded.fasterxml.jackson.databind.DeserializationContext;
import com.shaded.fasterxml.jackson.databind.JavaType;
import com.shaded.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.shaded.fasterxml.jackson.databind.util.Annotations;
import java.io.IOException;

public class ValueInjector extends Std {
    protected final Object _valueId;

    public ValueInjector(String str, JavaType javaType, Annotations annotations, AnnotatedMember annotatedMember, Object obj) {
        super(str, javaType, null, annotations, annotatedMember, false);
        this._valueId = obj;
    }

    public Object findValue(DeserializationContext deserializationContext, Object obj) {
        return deserializationContext.findInjectableValue(this._valueId, this, obj);
    }

    public void inject(DeserializationContext deserializationContext, Object obj) throws IOException {
        this._member.setValue(obj, findValue(deserializationContext, obj));
    }
}
