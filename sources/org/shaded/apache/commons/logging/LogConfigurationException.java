package org.shaded.apache.commons.logging;

public class LogConfigurationException extends RuntimeException {
    protected Throwable cause;

    public LogConfigurationException() {
        this.cause = null;
    }

    public LogConfigurationException(String message) {
        super(message);
        this.cause = null;
    }

    public LogConfigurationException(Throwable cause2) {
        this(cause2 == null ? null : cause2.toString(), cause2);
    }

    public LogConfigurationException(String message, Throwable cause2) {
        super(new StringBuffer().append(message).append(" (Caused by ").append(cause2).append(")").toString());
        this.cause = null;
        this.cause = cause2;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
