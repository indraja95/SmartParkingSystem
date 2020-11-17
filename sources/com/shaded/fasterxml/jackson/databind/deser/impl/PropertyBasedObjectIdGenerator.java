package com.shaded.fasterxml.jackson.databind.deser.impl;

import com.shaded.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.shaded.fasterxml.jackson.annotation.ObjectIdGenerator.IdKey;
import com.shaded.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

public class PropertyBasedObjectIdGenerator extends PropertyGenerator {
    private static final long serialVersionUID = 1;

    public PropertyBasedObjectIdGenerator(Class<?> cls) {
        super(cls);
    }

    public Object generateId(Object obj) {
        throw new UnsupportedOperationException();
    }

    public ObjectIdGenerator<Object> forScope(Class<?> cls) {
        return cls == this._scope ? this : new PropertyBasedObjectIdGenerator(cls);
    }

    public ObjectIdGenerator<Object> newForSerialization(Object obj) {
        return this;
    }

    public IdKey key(Object obj) {
        return new IdKey(getClass(), this._scope, obj);
    }
}
