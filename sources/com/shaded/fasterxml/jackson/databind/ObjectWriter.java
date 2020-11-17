package com.shaded.fasterxml.jackson.databind;

import com.shaded.fasterxml.jackson.core.Base64Variant;
import com.shaded.fasterxml.jackson.core.FormatSchema;
import com.shaded.fasterxml.jackson.core.JsonEncoding;
import com.shaded.fasterxml.jackson.core.JsonFactory;
import com.shaded.fasterxml.jackson.core.JsonGenerationException;
import com.shaded.fasterxml.jackson.core.JsonGenerator;
import com.shaded.fasterxml.jackson.core.JsonParser.Feature;
import com.shaded.fasterxml.jackson.core.JsonProcessingException;
import com.shaded.fasterxml.jackson.core.PrettyPrinter;
import com.shaded.fasterxml.jackson.core.Version;
import com.shaded.fasterxml.jackson.core.Versioned;
import com.shaded.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.shaded.fasterxml.jackson.core.type.TypeReference;
import com.shaded.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.shaded.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.shaded.fasterxml.jackson.core.util.Instantiatable;
import com.shaded.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.shaded.fasterxml.jackson.databind.cfg.PackageVersion;
import com.shaded.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.shaded.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.shaded.fasterxml.jackson.databind.ser.FilterProvider;
import com.shaded.fasterxml.jackson.databind.ser.SerializerFactory;
import com.shaded.fasterxml.jackson.databind.type.TypeFactory;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class ObjectWriter implements Versioned, Serializable {
    protected static final PrettyPrinter NULL_PRETTY_PRINTER = new MinimalPrettyPrinter();
    private static final long serialVersionUID = -7024829992408267532L;
    protected final SerializationConfig _config;
    protected final JsonFactory _jsonFactory;
    protected final PrettyPrinter _prettyPrinter;
    protected final JsonSerializer<Object> _rootSerializer;
    protected final JavaType _rootType;
    protected final FormatSchema _schema;
    protected final SerializerFactory _serializerFactory;
    protected final DefaultSerializerProvider _serializerProvider;

    protected ObjectWriter(ObjectMapper objectMapper, SerializationConfig serializationConfig, JavaType javaType, PrettyPrinter prettyPrinter) {
        this._config = serializationConfig;
        this._serializerProvider = objectMapper._serializerProvider;
        this._serializerFactory = objectMapper._serializerFactory;
        this._jsonFactory = objectMapper._jsonFactory;
        if (javaType != null) {
            javaType = javaType.withStaticTyping();
        }
        this._rootType = javaType;
        this._prettyPrinter = prettyPrinter;
        this._schema = null;
        this._rootSerializer = _prefetchRootSerializer(serializationConfig, javaType);
    }

    protected ObjectWriter(ObjectMapper objectMapper, SerializationConfig serializationConfig) {
        this._config = serializationConfig;
        this._serializerProvider = objectMapper._serializerProvider;
        this._serializerFactory = objectMapper._serializerFactory;
        this._jsonFactory = objectMapper._jsonFactory;
        this._rootType = null;
        this._rootSerializer = null;
        this._prettyPrinter = null;
        this._schema = null;
    }

    protected ObjectWriter(ObjectMapper objectMapper, SerializationConfig serializationConfig, FormatSchema formatSchema) {
        this._config = serializationConfig;
        this._serializerProvider = objectMapper._serializerProvider;
        this._serializerFactory = objectMapper._serializerFactory;
        this._jsonFactory = objectMapper._jsonFactory;
        this._rootType = null;
        this._rootSerializer = null;
        this._prettyPrinter = null;
        this._schema = formatSchema;
    }

    protected ObjectWriter(ObjectWriter objectWriter, SerializationConfig serializationConfig, JavaType javaType, JsonSerializer<Object> jsonSerializer, PrettyPrinter prettyPrinter, FormatSchema formatSchema) {
        this._config = serializationConfig;
        this._serializerProvider = objectWriter._serializerProvider;
        this._serializerFactory = objectWriter._serializerFactory;
        this._jsonFactory = objectWriter._jsonFactory;
        this._rootType = javaType;
        this._rootSerializer = jsonSerializer;
        this._prettyPrinter = prettyPrinter;
        this._schema = formatSchema;
    }

    protected ObjectWriter(ObjectWriter objectWriter, SerializationConfig serializationConfig) {
        this._config = serializationConfig;
        this._serializerProvider = objectWriter._serializerProvider;
        this._serializerFactory = objectWriter._serializerFactory;
        this._jsonFactory = objectWriter._jsonFactory;
        this._schema = objectWriter._schema;
        this._rootType = objectWriter._rootType;
        this._rootSerializer = objectWriter._rootSerializer;
        this._prettyPrinter = objectWriter._prettyPrinter;
    }

    public Version version() {
        return PackageVersion.VERSION;
    }

    public ObjectWriter with(SerializationFeature serializationFeature) {
        SerializationConfig with = this._config.with(serializationFeature);
        return with == this._config ? this : new ObjectWriter(this, with);
    }

    public ObjectWriter with(SerializationFeature serializationFeature, SerializationFeature... serializationFeatureArr) {
        SerializationConfig with = this._config.with(serializationFeature, serializationFeatureArr);
        return with == this._config ? this : new ObjectWriter(this, with);
    }

    public ObjectWriter withFeatures(SerializationFeature... serializationFeatureArr) {
        SerializationConfig withFeatures = this._config.withFeatures(serializationFeatureArr);
        return withFeatures == this._config ? this : new ObjectWriter(this, withFeatures);
    }

    public ObjectWriter without(SerializationFeature serializationFeature) {
        SerializationConfig without = this._config.without(serializationFeature);
        return without == this._config ? this : new ObjectWriter(this, without);
    }

    public ObjectWriter without(SerializationFeature serializationFeature, SerializationFeature... serializationFeatureArr) {
        SerializationConfig without = this._config.without(serializationFeature, serializationFeatureArr);
        return without == this._config ? this : new ObjectWriter(this, without);
    }

    public ObjectWriter withoutFeatures(SerializationFeature... serializationFeatureArr) {
        SerializationConfig withoutFeatures = this._config.withoutFeatures(serializationFeatureArr);
        return withoutFeatures == this._config ? this : new ObjectWriter(this, withoutFeatures);
    }

    public ObjectWriter with(DateFormat dateFormat) {
        SerializationConfig with = this._config.with(dateFormat);
        return with == this._config ? this : new ObjectWriter(this, with);
    }

    public ObjectWriter withDefaultPrettyPrinter() {
        return with((PrettyPrinter) new DefaultPrettyPrinter());
    }

    public ObjectWriter with(FilterProvider filterProvider) {
        return filterProvider == this._config.getFilterProvider() ? this : new ObjectWriter(this, this._config.withFilters(filterProvider));
    }

    public ObjectWriter with(PrettyPrinter prettyPrinter) {
        PrettyPrinter prettyPrinter2;
        if (prettyPrinter == this._prettyPrinter) {
            return this;
        }
        if (prettyPrinter == null) {
            prettyPrinter2 = NULL_PRETTY_PRINTER;
        } else {
            prettyPrinter2 = prettyPrinter;
        }
        return new ObjectWriter(this, this._config, this._rootType, this._rootSerializer, prettyPrinter2, this._schema);
    }

    public ObjectWriter withRootName(String str) {
        SerializationConfig withRootName = this._config.withRootName(str);
        return withRootName == this._config ? this : new ObjectWriter(this, withRootName);
    }

    public ObjectWriter withSchema(FormatSchema formatSchema) {
        if (this._schema == formatSchema) {
            return this;
        }
        _verifySchemaType(formatSchema);
        return new ObjectWriter(this, this._config, this._rootType, this._rootSerializer, this._prettyPrinter, formatSchema);
    }

    public ObjectWriter withType(JavaType javaType) {
        JavaType withStaticTyping = javaType.withStaticTyping();
        return new ObjectWriter(this, this._config, withStaticTyping, _prefetchRootSerializer(this._config, withStaticTyping), this._prettyPrinter, this._schema);
    }

    public ObjectWriter withType(Class<?> cls) {
        return withType(this._config.constructType(cls));
    }

    public ObjectWriter withType(TypeReference<?> typeReference) {
        return withType(this._config.getTypeFactory().constructType(typeReference.getType()));
    }

    public ObjectWriter withView(Class<?> cls) {
        SerializationConfig withView = this._config.withView(cls);
        return withView == this._config ? this : new ObjectWriter(this, withView);
    }

    public ObjectWriter with(Locale locale) {
        SerializationConfig with = this._config.with(locale);
        return with == this._config ? this : new ObjectWriter(this, with);
    }

    public ObjectWriter with(TimeZone timeZone) {
        SerializationConfig with = this._config.with(timeZone);
        return with == this._config ? this : new ObjectWriter(this, with);
    }

    public ObjectWriter with(Base64Variant base64Variant) {
        SerializationConfig with = this._config.with(base64Variant);
        return with == this._config ? this : new ObjectWriter(this, with);
    }

    public boolean isEnabled(SerializationFeature serializationFeature) {
        return this._config.isEnabled(serializationFeature);
    }

    public boolean isEnabled(MapperFeature mapperFeature) {
        return this._config.isEnabled(mapperFeature);
    }

    public boolean isEnabled(Feature feature) {
        return this._jsonFactory.isEnabled(feature);
    }

    public SerializationConfig getConfig() {
        return this._config;
    }

    @Deprecated
    public JsonFactory getJsonFactory() {
        return this._jsonFactory;
    }

    public JsonFactory getFactory() {
        return this._jsonFactory;
    }

    public TypeFactory getTypeFactory() {
        return this._config.getTypeFactory();
    }

    public boolean hasPrefetchedSerializer() {
        return this._rootSerializer != null;
    }

    public void writeValue(JsonGenerator jsonGenerator, Object obj) throws IOException, JsonGenerationException, JsonMappingException {
        _configureJsonGenerator(jsonGenerator);
        if (!this._config.isEnabled(SerializationFeature.CLOSE_CLOSEABLE) || !(obj instanceof Closeable)) {
            if (this._rootType == null) {
                _serializerProvider(this._config).serializeValue(jsonGenerator, obj);
            } else {
                _serializerProvider(this._config).serializeValue(jsonGenerator, obj, this._rootType, this._rootSerializer);
            }
            if (this._config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
                jsonGenerator.flush();
                return;
            }
            return;
        }
        _writeCloseableValue(jsonGenerator, obj, this._config);
    }

    public void writeValue(File file, Object obj) throws IOException, JsonGenerationException, JsonMappingException {
        _configAndWriteValue(this._jsonFactory.createGenerator(file, JsonEncoding.UTF8), obj);
    }

    public void writeValue(OutputStream outputStream, Object obj) throws IOException, JsonGenerationException, JsonMappingException {
        _configAndWriteValue(this._jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8), obj);
    }

    public void writeValue(Writer writer, Object obj) throws IOException, JsonGenerationException, JsonMappingException {
        _configAndWriteValue(this._jsonFactory.createGenerator(writer), obj);
    }

    public String writeValueAsString(Object obj) throws JsonProcessingException {
        SegmentedStringWriter segmentedStringWriter = new SegmentedStringWriter(this._jsonFactory._getBufferRecycler());
        try {
            _configAndWriteValue(this._jsonFactory.createGenerator((Writer) segmentedStringWriter), obj);
            return segmentedStringWriter.getAndClear();
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e2) {
            throw JsonMappingException.fromUnexpectedIOE(e2);
        }
    }

    public byte[] writeValueAsBytes(Object obj) throws JsonProcessingException {
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder(this._jsonFactory._getBufferRecycler());
        try {
            _configAndWriteValue(this._jsonFactory.createGenerator((OutputStream) byteArrayBuilder, JsonEncoding.UTF8), obj);
            byte[] byteArray = byteArrayBuilder.toByteArray();
            byteArrayBuilder.release();
            return byteArray;
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e2) {
            throw JsonMappingException.fromUnexpectedIOE(e2);
        }
    }

    public void acceptJsonFormatVisitor(JavaType javaType, JsonFormatVisitorWrapper jsonFormatVisitorWrapper) throws JsonMappingException {
        if (javaType == null) {
            throw new IllegalArgumentException("type must be provided");
        }
        _serializerProvider(this._config).acceptJsonFormatVisitor(javaType, jsonFormatVisitorWrapper);
    }

    public boolean canSerialize(Class<?> cls) {
        return _serializerProvider(this._config).hasSerializerFor(cls);
    }

    /* access modifiers changed from: protected */
    public DefaultSerializerProvider _serializerProvider(SerializationConfig serializationConfig) {
        return this._serializerProvider.createInstance(serializationConfig, this._serializerFactory);
    }

    /* access modifiers changed from: protected */
    public void _verifySchemaType(FormatSchema formatSchema) {
        if (formatSchema != null && !this._jsonFactory.canUseSchema(formatSchema)) {
            throw new IllegalArgumentException("Can not use FormatSchema of type " + formatSchema.getClass().getName() + " for format " + this._jsonFactory.getFormatName());
        }
    }

    /* access modifiers changed from: protected */
    public final void _configAndWriteValue(JsonGenerator jsonGenerator, Object obj) throws IOException, JsonGenerationException, JsonMappingException {
        _configureJsonGenerator(jsonGenerator);
        if (!this._config.isEnabled(SerializationFeature.CLOSE_CLOSEABLE) || !(obj instanceof Closeable)) {
            boolean z = false;
            try {
                z = this._rootType;
                if (z == null) {
                    _serializerProvider(this._config).serializeValue(jsonGenerator, obj);
                } else {
                    _serializerProvider(this._config).serializeValue(jsonGenerator, obj, this._rootType, this._rootSerializer);
                }
                jsonGenerator.close();
            } finally {
                if (!z) {
                    try {
                        jsonGenerator.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            _writeCloseable(jsonGenerator, obj, this._config);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0034 A[SYNTHETIC, Splitter:B:22:0x0034] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0039 A[SYNTHETIC, Splitter:B:25:0x0039] */
    private final void _writeCloseable(JsonGenerator jsonGenerator, Object obj, SerializationConfig serializationConfig) throws IOException, JsonGenerationException, JsonMappingException {
        Closeable closeable;
        Throwable th;
        Closeable closeable2 = (Closeable) obj;
        try {
            if (this._rootType == null) {
                _serializerProvider(serializationConfig).serializeValue(jsonGenerator, obj);
            } else {
                _serializerProvider(serializationConfig).serializeValue(jsonGenerator, obj, this._rootType, this._rootSerializer);
            }
            JsonGenerator jsonGenerator2 = null;
            try {
                jsonGenerator.close();
                Closeable closeable3 = null;
                try {
                    closeable2.close();
                    if (0 != 0) {
                        try {
                            jsonGenerator2.close();
                        } catch (IOException e) {
                        }
                    }
                    if (0 != 0) {
                        try {
                            closeable3.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    closeable = null;
                    jsonGenerator = null;
                    if (jsonGenerator != null) {
                    }
                    if (closeable != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                jsonGenerator = null;
                Closeable closeable4 = closeable2;
                th = th3;
                closeable = closeable4;
                if (jsonGenerator != null) {
                    try {
                        jsonGenerator.close();
                    } catch (IOException e3) {
                    }
                }
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (Throwable th4) {
            Throwable th5 = th4;
            closeable = closeable2;
            th = th5;
        }
    }

    private final void _writeCloseableValue(JsonGenerator jsonGenerator, Object obj, SerializationConfig serializationConfig) throws IOException, JsonGenerationException, JsonMappingException {
        Closeable closeable;
        Throwable th;
        Closeable closeable2 = (Closeable) obj;
        try {
            if (this._rootType == null) {
                _serializerProvider(serializationConfig).serializeValue(jsonGenerator, obj);
            } else {
                _serializerProvider(serializationConfig).serializeValue(jsonGenerator, obj, this._rootType, this._rootSerializer);
            }
            if (this._config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
                jsonGenerator.flush();
            }
            closeable = null;
            try {
                closeable2.close();
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            Throwable th4 = th3;
            closeable = closeable2;
            th = th4;
        }
    }

    /* access modifiers changed from: protected */
    public JsonSerializer<Object> _prefetchRootSerializer(SerializationConfig serializationConfig, JavaType javaType) {
        JsonSerializer<Object> jsonSerializer = null;
        if (javaType == null || !this._config.isEnabled(SerializationFeature.EAGER_SERIALIZER_FETCH)) {
            return jsonSerializer;
        }
        try {
            return _serializerProvider(serializationConfig).findTypedValueSerializer(javaType, true, (BeanProperty) null);
        } catch (JsonProcessingException e) {
            return jsonSerializer;
        }
    }

    private void _configureJsonGenerator(JsonGenerator jsonGenerator) {
        if (this._prettyPrinter != null) {
            PrettyPrinter prettyPrinter = this._prettyPrinter;
            if (prettyPrinter == NULL_PRETTY_PRINTER) {
                jsonGenerator.setPrettyPrinter(null);
            } else {
                if (prettyPrinter instanceof Instantiatable) {
                    prettyPrinter = (PrettyPrinter) ((Instantiatable) prettyPrinter).createInstance();
                }
                jsonGenerator.setPrettyPrinter(prettyPrinter);
            }
        } else if (this._config.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
            jsonGenerator.useDefaultPrettyPrinter();
        }
        if (this._schema != null) {
            jsonGenerator.setSchema(this._schema);
        }
    }
}
