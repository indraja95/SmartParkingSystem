package com.firebase.client.utilities.encoding;

import com.shaded.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelpers {
    private static final ObjectMapper mapperInstance = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return mapperInstance;
    }
}
