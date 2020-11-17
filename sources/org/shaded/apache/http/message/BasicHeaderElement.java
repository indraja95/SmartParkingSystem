package org.shaded.apache.http.message;

import org.shaded.apache.http.HeaderElement;
import org.shaded.apache.http.NameValuePair;
import org.shaded.apache.http.util.CharArrayBuffer;
import org.shaded.apache.http.util.LangUtils;

public class BasicHeaderElement implements HeaderElement, Cloneable {
    private final String name;
    private final NameValuePair[] parameters;
    private final String value;

    public BasicHeaderElement(String name2, String value2, NameValuePair[] parameters2) {
        if (name2 == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name2;
        this.value = value2;
        if (parameters2 != null) {
            this.parameters = parameters2;
        } else {
            this.parameters = new NameValuePair[0];
        }
    }

    public BasicHeaderElement(String name2, String value2) {
        this(name2, value2, null);
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public NameValuePair[] getParameters() {
        return (NameValuePair[]) this.parameters.clone();
    }

    public int getParameterCount() {
        return this.parameters.length;
    }

    public NameValuePair getParameter(int index) {
        return this.parameters[index];
    }

    public NameValuePair getParameterByName(String name2) {
        if (name2 == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        for (NameValuePair current : this.parameters) {
            if (current.getName().equalsIgnoreCase(name2)) {
                return current;
            }
        }
        return null;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!(object instanceof HeaderElement)) {
            return false;
        }
        BasicHeaderElement that = (BasicHeaderElement) object;
        if (!this.name.equals(that.name) || !LangUtils.equals((Object) this.value, (Object) that.value) || !LangUtils.equals((Object[]) this.parameters, (Object[]) that.parameters)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int hash = LangUtils.hashCode(LangUtils.hashCode(17, (Object) this.name), (Object) this.value);
        for (NameValuePair hashCode : this.parameters) {
            hash = LangUtils.hashCode(hash, (Object) hashCode);
        }
        return hash;
    }

    public String toString() {
        CharArrayBuffer buffer = new CharArrayBuffer(64);
        buffer.append(this.name);
        if (this.value != null) {
            buffer.append("=");
            buffer.append(this.value);
        }
        for (NameValuePair append : this.parameters) {
            buffer.append("; ");
            buffer.append((Object) append);
        }
        return buffer.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
