package com.shaded.fasterxml.jackson.databind.ser;

import com.shaded.fasterxml.jackson.databind.BeanDescription;
import com.shaded.fasterxml.jackson.databind.JavaType;
import com.shaded.fasterxml.jackson.databind.JsonSerializer;
import com.shaded.fasterxml.jackson.databind.SerializationConfig;
import com.shaded.fasterxml.jackson.databind.type.ArrayType;
import com.shaded.fasterxml.jackson.databind.type.CollectionLikeType;
import com.shaded.fasterxml.jackson.databind.type.CollectionType;
import com.shaded.fasterxml.jackson.databind.type.MapLikeType;
import com.shaded.fasterxml.jackson.databind.type.MapType;
import java.util.List;

public abstract class BeanSerializerModifier {
    public List<BeanPropertyWriter> changeProperties(SerializationConfig serializationConfig, BeanDescription beanDescription, List<BeanPropertyWriter> list) {
        return list;
    }

    public List<BeanPropertyWriter> orderProperties(SerializationConfig serializationConfig, BeanDescription beanDescription, List<BeanPropertyWriter> list) {
        return list;
    }

    public BeanSerializerBuilder updateBuilder(SerializationConfig serializationConfig, BeanDescription beanDescription, BeanSerializerBuilder beanSerializerBuilder) {
        return beanSerializerBuilder;
    }

    public JsonSerializer<?> modifySerializer(SerializationConfig serializationConfig, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }

    public JsonSerializer<?> modifyArraySerializer(SerializationConfig serializationConfig, ArrayType arrayType, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }

    public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig serializationConfig, CollectionType collectionType, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }

    public JsonSerializer<?> modifyCollectionLikeSerializer(SerializationConfig serializationConfig, CollectionLikeType collectionLikeType, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }

    public JsonSerializer<?> modifyMapSerializer(SerializationConfig serializationConfig, MapType mapType, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }

    public JsonSerializer<?> modifyMapLikeSerializer(SerializationConfig serializationConfig, MapLikeType mapLikeType, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }

    public JsonSerializer<?> modifyEnumSerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }

    public JsonSerializer<?> modifyKeySerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription, JsonSerializer<?> jsonSerializer) {
        return jsonSerializer;
    }
}
