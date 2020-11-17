package org.shaded.apache.http.impl;

import java.io.IOException;
import org.shaded.apache.http.HttpClientConnection;
import org.shaded.apache.http.HttpConnectionMetrics;
import org.shaded.apache.http.HttpEntityEnclosingRequest;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.HttpResponseFactory;
import org.shaded.apache.http.impl.entity.EntityDeserializer;
import org.shaded.apache.http.impl.entity.EntitySerializer;
import org.shaded.apache.http.impl.entity.LaxContentLengthStrategy;
import org.shaded.apache.http.impl.entity.StrictContentLengthStrategy;
import org.shaded.apache.http.impl.io.HttpRequestWriter;
import org.shaded.apache.http.impl.io.HttpResponseParser;
import org.shaded.apache.http.io.EofSensor;
import org.shaded.apache.http.io.HttpMessageParser;
import org.shaded.apache.http.io.HttpMessageWriter;
import org.shaded.apache.http.io.SessionInputBuffer;
import org.shaded.apache.http.io.SessionOutputBuffer;
import org.shaded.apache.http.params.HttpParams;

public abstract class AbstractHttpClientConnection implements HttpClientConnection {
    private final EntityDeserializer entitydeserializer = createEntityDeserializer();
    private final EntitySerializer entityserializer = createEntitySerializer();
    private EofSensor eofSensor = null;
    private SessionInputBuffer inbuffer = null;
    private HttpConnectionMetricsImpl metrics = null;
    private SessionOutputBuffer outbuffer = null;
    private HttpMessageWriter requestWriter = null;
    private HttpMessageParser responseParser = null;

    /* access modifiers changed from: protected */
    public abstract void assertOpen() throws IllegalStateException;

    /* access modifiers changed from: protected */
    public EntityDeserializer createEntityDeserializer() {
        return new EntityDeserializer(new LaxContentLengthStrategy());
    }

    /* access modifiers changed from: protected */
    public EntitySerializer createEntitySerializer() {
        return new EntitySerializer(new StrictContentLengthStrategy());
    }

    /* access modifiers changed from: protected */
    public HttpResponseFactory createHttpResponseFactory() {
        return new DefaultHttpResponseFactory();
    }

    /* access modifiers changed from: protected */
    public HttpMessageParser createResponseParser(SessionInputBuffer buffer, HttpResponseFactory responseFactory, HttpParams params) {
        return new HttpResponseParser(buffer, null, responseFactory, params);
    }

    /* access modifiers changed from: protected */
    public HttpMessageWriter createRequestWriter(SessionOutputBuffer buffer, HttpParams params) {
        return new HttpRequestWriter(buffer, null, params);
    }

    /* access modifiers changed from: protected */
    public void init(SessionInputBuffer inbuffer2, SessionOutputBuffer outbuffer2, HttpParams params) {
        if (inbuffer2 == null) {
            throw new IllegalArgumentException("Input session buffer may not be null");
        } else if (outbuffer2 == null) {
            throw new IllegalArgumentException("Output session buffer may not be null");
        } else {
            this.inbuffer = inbuffer2;
            this.outbuffer = outbuffer2;
            if (inbuffer2 instanceof EofSensor) {
                this.eofSensor = (EofSensor) inbuffer2;
            }
            this.responseParser = createResponseParser(inbuffer2, createHttpResponseFactory(), params);
            this.requestWriter = createRequestWriter(outbuffer2, params);
            this.metrics = new HttpConnectionMetricsImpl(inbuffer2.getMetrics(), outbuffer2.getMetrics());
        }
    }

    public boolean isResponseAvailable(int timeout) throws IOException {
        assertOpen();
        return this.inbuffer.isDataAvailable(timeout);
    }

    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        assertOpen();
        this.requestWriter.write(request);
        this.metrics.incrementRequestCount();
    }

    public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        assertOpen();
        if (request.getEntity() != null) {
            this.entityserializer.serialize(this.outbuffer, request, request.getEntity());
        }
    }

    /* access modifiers changed from: protected */
    public void doFlush() throws IOException {
        this.outbuffer.flush();
    }

    public void flush() throws IOException {
        assertOpen();
        doFlush();
    }

    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        assertOpen();
        HttpResponse response = (HttpResponse) this.responseParser.parse();
        if (response.getStatusLine().getStatusCode() >= 200) {
            this.metrics.incrementResponseCount();
        }
        return response;
    }

    public void receiveResponseEntity(HttpResponse response) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        assertOpen();
        response.setEntity(this.entitydeserializer.deserialize(this.inbuffer, response));
    }

    /* access modifiers changed from: protected */
    public boolean isEof() {
        return this.eofSensor != null && this.eofSensor.isEof();
    }

    public boolean isStale() {
        boolean z = true;
        if (!isOpen() || isEof()) {
            return z;
        }
        try {
            this.inbuffer.isDataAvailable(1);
            return isEof();
        } catch (IOException e) {
            return z;
        }
    }

    public HttpConnectionMetrics getMetrics() {
        return this.metrics;
    }
}
