package com.shaded.fasterxml.jackson.databind;

import com.shaded.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.shaded.fasterxml.jackson.databind.annotation.NoClass;
import com.shaded.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.shaded.fasterxml.jackson.databind.cfg.MapperConfig;
import com.shaded.fasterxml.jackson.databind.introspect.Annotated;
import com.shaded.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.shaded.fasterxml.jackson.databind.type.TypeFactory;
import com.shaded.fasterxml.jackson.databind.util.ClassUtil;
import com.shaded.fasterxml.jackson.databind.util.Converter;
import com.shaded.fasterxml.jackson.databind.util.Converter.None;
import java.lang.reflect.Type;

public abstract class DatabindContext {
    public abstract Class<?> getActiveView();

    public abstract AnnotationIntrospector getAnnotationIntrospector();

    public abstract MapperConfig<?> getConfig();

    public abstract TypeFactory getTypeFactory();

    public final boolean isEnabled(MapperFeature mapperFeature) {
        return getConfig().isEnabled(mapperFeature);
    }

    public final boolean canOverrideAccessModifiers() {
        return getConfig().canOverrideAccessModifiers();
    }

    public JavaType constructType(Type type) {
        return getTypeFactory().constructType(type);
    }

    public JavaType constructSpecializedType(JavaType javaType, Class<?> cls) {
        return getConfig().constructSpecializedType(javaType, cls);
    }

    public ObjectIdGenerator<?> objectIdGeneratorInstance(Annotated annotated, ObjectIdInfo objectIdInfo) throws JsonMappingException {
        Class generatorType = objectIdInfo.getGeneratorType();
        MapperConfig config = getConfig();
        HandlerInstantiator handlerInstantiator = config.getHandlerInstantiator();
        ObjectIdGenerator objectIdGeneratorInstance = handlerInstantiator == null ? null : handlerInstantiator.objectIdGeneratorInstance(config, annotated, generatorType);
        if (objectIdGeneratorInstance == null) {
            objectIdGeneratorInstance = (ObjectIdGenerator) ClassUtil.createInstance(generatorType, config.canOverrideAccessModifiers());
        }
        return objectIdGeneratorInstance.forScope(objectIdInfo.getScope());
    }

    public Converter<Object, Object> converterInstance(Annotated annotated, Object obj) throws JsonMappingException {
        Converter converter = null;
        if (obj == null) {
            return null;
        }
        if (obj instanceof Converter) {
            return (Converter) obj;
        }
        if (!(obj instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned Converter definition of type " + obj.getClass().getName() + "; expected type Converter or Class<Converter> instead");
        }
        Class<NoClass> cls = (Class) obj;
        if (cls == None.class || cls == NoClass.class) {
            return null;
        }
        if (!Converter.class.isAssignableFrom(cls)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + cls.getName() + "; expected Class<Converter>");
        }
        MapperConfig config = getConfig();
        HandlerInstantiator handlerInstantiator = config.getHandlerInstantiator();
        if (handlerInstantiator != null) {
            converter = handlerInstantiator.converterInstance(config, annotated, cls);
        }
        if (converter == null) {
            converter = (Converter) ClassUtil.createInstance(cls, config.canOverrideAccessModifiers());
        }
        return converter;
    }
}
