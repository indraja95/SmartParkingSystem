package org.shaded.apache.http.client.entity;

import java.io.UnsupportedEncodingException;
import java.util.List;
import org.shaded.apache.http.NameValuePair;
import org.shaded.apache.http.annotation.NotThreadSafe;
import org.shaded.apache.http.client.utils.URLEncodedUtils;
import org.shaded.apache.http.entity.StringEntity;

@NotThreadSafe
public class UrlEncodedFormEntity extends StringEntity {
    public UrlEncodedFormEntity(List<? extends NameValuePair> parameters, String encoding) throws UnsupportedEncodingException {
        super(URLEncodedUtils.format(parameters, encoding), encoding);
        StringBuilder append = new StringBuilder().append("application/x-www-form-urlencoded; charset=");
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        setContentType(append.append(encoding).toString());
    }

    public UrlEncodedFormEntity(List<? extends NameValuePair> parameters) throws UnsupportedEncodingException {
        this(parameters, "ISO-8859-1");
    }
}
