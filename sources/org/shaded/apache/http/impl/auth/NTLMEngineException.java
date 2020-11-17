package org.shaded.apache.http.impl.auth;

import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.auth.AuthenticationException;

@Immutable
public class NTLMEngineException extends AuthenticationException {
    private static final long serialVersionUID = 6027981323731768824L;

    public NTLMEngineException() {
    }

    public NTLMEngineException(String message) {
        super(message);
    }

    public NTLMEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
