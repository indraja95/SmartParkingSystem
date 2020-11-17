package com.firebase.client;

import java.util.Collections;
import java.util.Map;

public class AuthData {
    private final Map<String, Object> auth;
    private final long expires;
    private final String provider;
    private final Map<String, Object> providerData;
    private final String token;
    private final String uid;

    public AuthData(String token2, long expires2, String uid2, String provider2, Map<String, Object> auth2, Map<String, Object> providerData2) {
        Map<String, Object> map;
        Map<String, Object> map2 = null;
        this.token = token2;
        this.expires = expires2;
        this.uid = uid2;
        this.provider = provider2;
        if (providerData2 != null) {
            map = Collections.unmodifiableMap(providerData2);
        } else {
            map = null;
        }
        this.providerData = map;
        if (auth2 != null) {
            map2 = Collections.unmodifiableMap(auth2);
        }
        this.auth = map2;
    }

    public String getToken() {
        return this.token;
    }

    public long getExpires() {
        return this.expires;
    }

    public String getUid() {
        return this.uid;
    }

    public String getProvider() {
        return this.provider;
    }

    public Map<String, Object> getProviderData() {
        return this.providerData;
    }

    public Map<String, Object> getAuth() {
        return this.auth;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthData authData = (AuthData) o;
        if (this.provider == null ? authData.provider != null : !this.provider.equals(authData.provider)) {
            return false;
        }
        if (this.providerData == null ? authData.providerData != null : !this.providerData.equals(authData.providerData)) {
            return false;
        }
        if (this.auth == null ? authData.auth != null : !this.auth.equals(authData.auth)) {
            return false;
        }
        if (this.token == null ? authData.token != null : !this.token.equals(authData.token)) {
            return false;
        }
        if (this.expires != authData.expires) {
            return false;
        }
        if (this.uid != null) {
            if (this.uid.equals(authData.uid)) {
                return true;
            }
        } else if (authData.uid == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result;
        int i;
        int i2;
        int i3;
        int i4 = 0;
        if (this.token != null) {
            result = this.token.hashCode();
        } else {
            result = 0;
        }
        int i5 = result * 31;
        if (this.uid != null) {
            i = this.uid.hashCode();
        } else {
            i = 0;
        }
        int i6 = (i5 + i) * 31;
        if (this.provider != null) {
            i2 = this.provider.hashCode();
        } else {
            i2 = 0;
        }
        int i7 = (i6 + i2) * 31;
        if (this.providerData != null) {
            i3 = this.providerData.hashCode();
        } else {
            i3 = 0;
        }
        int i8 = (i7 + i3) * 31;
        if (this.auth != null) {
            i4 = this.auth.hashCode();
        }
        return i8 + i4;
    }

    public String toString() {
        return "AuthData{uid='" + this.uid + '\'' + ", provider='" + this.provider + '\'' + ", token='" + (this.token == null ? null : "***") + '\'' + ", expires='" + this.expires + '\'' + ", auth='" + this.auth + '\'' + ", providerData='" + this.providerData + '\'' + '}';
    }
}
