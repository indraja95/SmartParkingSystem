package org.shaded.apache.http.protocol;

import com.shaded.fasterxml.jackson.core.util.BufferRecycler;
import java.io.IOException;
import java.net.ProtocolException;
import org.shaded.apache.http.HttpClientConnection;
import org.shaded.apache.http.HttpEntityEnclosingRequest;
import org.shaded.apache.http.HttpException;
import org.shaded.apache.http.HttpRequest;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.HttpVersion;
import org.shaded.apache.http.ProtocolVersion;
import org.shaded.apache.http.client.methods.HttpHead;
import org.shaded.apache.http.params.CoreProtocolPNames;

public class HttpRequestExecutor {
    /* access modifiers changed from: protected */
    public boolean canResponseHaveBody(HttpRequest request, HttpResponse response) {
        if (HttpHead.METHOD_NAME.equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }
        int status = response.getStatusLine().getStatusCode();
        if (status < 200 || status == 204 || status == 304 || status == 205) {
            return false;
        }
        return true;
    }

    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (conn == null) {
            throw new IllegalArgumentException("Client connection may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else {
            try {
                HttpResponse response = doSendRequest(request, conn, context);
                if (response == null) {
                    return doReceiveResponse(request, conn, context);
                }
                return response;
            } catch (IOException ex) {
                conn.close();
                throw ex;
            } catch (HttpException ex2) {
                conn.close();
                throw ex2;
            } catch (RuntimeException ex3) {
                conn.close();
                throw ex3;
            }
        }
    }

    public void preProcess(HttpRequest request, HttpProcessor processor, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (processor == null) {
            throw new IllegalArgumentException("HTTP processor may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else {
            context.setAttribute(ExecutionContext.HTTP_REQUEST, request);
            processor.process(request, context);
        }
    }

    /* access modifiers changed from: protected */
    public HttpResponse doSendRequest(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (conn == null) {
            throw new IllegalArgumentException("HTTP connection may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else {
            HttpResponse response = null;
            context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
            context.setAttribute(ExecutionContext.HTTP_REQ_SENT, Boolean.FALSE);
            conn.sendRequestHeader(request);
            if (request instanceof HttpEntityEnclosingRequest) {
                boolean sendentity = true;
                ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
                if (((HttpEntityEnclosingRequest) request).expectContinue() && !ver.lessEquals(HttpVersion.HTTP_1_0)) {
                    conn.flush();
                    if (conn.isResponseAvailable(request.getParams().getIntParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, BufferRecycler.DEFAULT_WRITE_CONCAT_BUFFER_LEN))) {
                        response = conn.receiveResponseHeader();
                        if (canResponseHaveBody(request, response)) {
                            conn.receiveResponseEntity(response);
                        }
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200) {
                            sendentity = false;
                        } else if (status != 100) {
                            throw new ProtocolException(new StringBuffer().append("Unexpected response: ").append(response.getStatusLine()).toString());
                        } else {
                            response = null;
                        }
                    }
                }
                if (sendentity) {
                    conn.sendRequestEntity((HttpEntityEnclosingRequest) request);
                }
            }
            conn.flush();
            context.setAttribute(ExecutionContext.HTTP_REQ_SENT, Boolean.TRUE);
            return response;
        }
    }

    /* access modifiers changed from: protected */
    public HttpResponse doReceiveResponse(HttpRequest request, HttpClientConnection conn, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (conn == null) {
            throw new IllegalArgumentException("HTTP connection may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else {
            HttpResponse response = null;
            int statuscode = 0;
            while (true) {
                if (response != null && statuscode >= 200) {
                    return response;
                }
                response = conn.receiveResponseHeader();
                if (canResponseHaveBody(request, response)) {
                    conn.receiveResponseEntity(response);
                }
                statuscode = response.getStatusLine().getStatusCode();
            }
        }
    }

    public void postProcess(HttpResponse response, HttpProcessor processor, HttpContext context) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        } else if (processor == null) {
            throw new IllegalArgumentException("HTTP processor may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else {
            context.setAttribute(ExecutionContext.HTTP_RESPONSE, response);
            processor.process(response, context);
        }
    }
}
