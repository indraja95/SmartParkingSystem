package com.shaded.fasterxml.jackson.databind.module;

import com.shaded.fasterxml.jackson.databind.BeanDescription;
import com.shaded.fasterxml.jackson.databind.JavaType;
import com.shaded.fasterxml.jackson.databind.JsonSerializer;
import com.shaded.fasterxml.jackson.databind.SerializationConfig;
import com.shaded.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.shaded.fasterxml.jackson.databind.ser.Serializers.Base;
import com.shaded.fasterxml.jackson.databind.type.ArrayType;
import com.shaded.fasterxml.jackson.databind.type.ClassKey;
import com.shaded.fasterxml.jackson.databind.type.CollectionLikeType;
import com.shaded.fasterxml.jackson.databind.type.CollectionType;
import com.shaded.fasterxml.jackson.databind.type.MapLikeType;
import com.shaded.fasterxml.jackson.databind.type.MapType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SimpleSerializers extends Base implements Serializable {
    private static final long serialVersionUID = 8531646511998456779L;
    protected HashMap<ClassKey, JsonSerializer<?>> _classMappings = null;
    protected HashMap<ClassKey, JsonSerializer<?>> _interfaceMappings = null;

    public SimpleSerializers() {
    }

    public SimpleSerializers(List<JsonSerializer<?>> list) {
        addSerializers(list);
    }

    public void addSerializer(JsonSerializer<?> jsonSerializer) {
        Class<Object> handledType = jsonSerializer.handledType();
        if (handledType == null || handledType == Object.class) {
            throw new IllegalArgumentException("JsonSerializer of type " + jsonSerializer.getClass().getName() + " does not define valid handledType() -- must either register with method that takes type argument " + " or make serializer extend 'com.fasterxml.jackson.databind.ser.std.StdSerializer'");
        }
        _addSerializer(handledType, jsonSerializer);
    }

    public <T> void addSerializer(Class<? extends T> cls, JsonSerializer<T> jsonSerializer) {
        _addSerializer(cls, jsonSerializer);
    }

    public void addSerializers(List<JsonSerializer<?>> list) {
        for (JsonSerializer addSerializer : list) {
            addSerializer(addSerializer);
        }
    }

    private void _addSerializer(Class<?> cls, JsonSerializer<?> jsonSerializer) {
        ClassKey classKey = new ClassKey(cls);
        if (cls.isInterface()) {
            if (this._interfaceMappings == null) {
                this._interfaceMappings = new HashMap<>();
            }
            this._interfaceMappings.put(classKey, jsonSerializer);
            return;
        }
        if (this._classMappings == null) {
            this._classMappings = new HashMap<>();
        }
        this._classMappings.put(classKey, jsonSerializer);
    }

    public JsonSerializer<?> findSerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription) {
        JsonSerializer _findInterfaceMapping;
        Class rawClass = javaType.getRawClass();
        ClassKey classKey = new ClassKey(rawClass);
        if (rawClass.isInterface()) {
            if (this._interfaceMappings != null) {
                JsonSerializer<?> jsonSerializer = (JsonSerializer) this._interfaceMappings.get(classKey);
                if (jsonSerializer != null) {
                    return jsonSerializer;
                }
            }
        } else if (this._classMappings != null) {
            JsonSerializer<?> jsonSerializer2 = (JsonSerializer) this._classMappings.get(classKey);
            if (jsonSerializer2 != null) {
                return jsonSerializer2;
            }
            for (Class cls = rawClass; cls != null; cls = cls.getSuperclass()) {
                classKey.reset(cls);
                JsonSerializer<?> jsonSerializer3 = (JsonSerializer) this._classMappings.get(classKey);
                if (jsonSerializer3 != null) {
                    return jsonSerializer3;
                }
            }
        }
        if (this._interfaceMappings != null) {
            JsonSerializer<?> _findInterfaceMapping2 = _findInterfaceMapping(rawClass, classKey);
            if (_findInterfaceMapping2 != null) {
                return _findInterfaceMapping2;
            }
            if (!rawClass.isInterface()) {
                Class cls2 = rawClass;
                do {
                    cls2 = cls2.getSuperclass();
                    if (cls2 != null) {
                        _findInterfaceMapping = _findInterfaceMapping(cls2, classKey);
                    }
                } while (_findInterfaceMapping == null);
                return _findInterfaceMapping;
            }
        }
        return null;
    }

    public JsonSerializer<?> findArraySerializer(SerializationConfig serializationConfig, ArrayType arrayType, BeanDescription beanDescription, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer) {
        return findSerializer(serializationConfig, arrayType, beanDescription);
    }

    public JsonSerializer<?> findCollectionSerializer(SerializationConfig serializationConfig, CollectionType collectionType, BeanDescription beanDescription, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer) {
        return findSerializer(serializationConfig, collectionType, beanDescription);
    }

    public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig serializationConfig, CollectionLikeType collectionLikeType, BeanDescription beanDescription, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer) {
        return findSerializer(serializationConfig, collectionLikeType, beanDescription);
    }

    public JsonSerializer<?> findMapSerializer(SerializationConfig serializationConfig, MapType mapType, BeanDescription beanDescription, JsonSerializer<Object> jsonSerializer, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer2) {
        return findSerializer(serializationConfig, mapType, beanDescription);
    }

    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig serializationConfig, MapLikeType mapLikeType, BeanDescription beanDescription, JsonSerializer<Object> jsonSerializer, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer2) {
        return findSerializer(serializationConfig, mapLikeType, beanDescription);
    }

    /* access modifiers changed from: protected */
    public JsonSerializer<?> _findInterfaceMapping(Class<?> cls, ClassKey classKey) {
        Class[] interfaces;
        for (Class cls2 : cls.getInterfaces()) {
            classKey.reset(cls2);
            JsonSerializer<?> jsonSerializer = (JsonSerializer) this._interfaceMappings.get(classKey);
            if (jsonSerializer != null) {
                return jsonSerializer;
            }
            JsonSerializer<?> _findInterfaceMapping = _findInterfaceMapping(cls2, classKey);
            if (_findInterfaceMapping != null) {
                return _findInterfaceMapping;
            }
        }
        return null;
    }
}
