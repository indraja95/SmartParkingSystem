package org.shaded.apache.http.impl.client;

import java.io.IOException;
import org.shaded.apache.http.HttpEntity;
import org.shaded.apache.http.HttpResponse;
import org.shaded.apache.http.StatusLine;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.client.HttpResponseException;
import org.shaded.apache.http.client.ResponseHandler;
import org.shaded.apache.http.util.EntityUtils;

@Immutable
public class BasicResponseHandler implements ResponseHandler<String> {
    public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        return EntityUtils.toString(entity);
    }
}
