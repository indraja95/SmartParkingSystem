package org.shaded.apache.commons.logging.impl;

import java.io.Serializable;
import org.shaded.apache.commons.logging.Log;
import org.shaded.apache.http.client.methods.HttpTrace;
import org.shaded.apache.log4j.Logger;
import org.shaded.apache.log4j.Priority;

public class Log4JLogger implements Log, Serializable {
    private static final String FQCN;
    static Class class$org$apache$commons$logging$impl$Log4JLogger;
    static Class class$org$apache$log4j$Level;
    static Class class$org$apache$log4j$Priority;
    private static Priority traceLevel;
    private transient Logger logger = null;
    private String name = null;

    static {
        Class cls;
        Class cls2;
        Class cls3;
        Class cls4;
        if (class$org$apache$commons$logging$impl$Log4JLogger == null) {
            cls = class$("org.shaded.apache.commons.logging.impl.Log4JLogger");
            class$org$apache$commons$logging$impl$Log4JLogger = cls;
        } else {
            cls = class$org$apache$commons$logging$impl$Log4JLogger;
        }
        FQCN = cls.getName();
        if (class$org$apache$log4j$Priority == null) {
            cls2 = class$("org.shaded.apache.log4j.Priority");
            class$org$apache$log4j$Priority = cls2;
        } else {
            cls2 = class$org$apache$log4j$Priority;
        }
        if (class$org$apache$log4j$Level == null) {
            cls3 = class$("org.shaded.apache.log4j.Level");
            class$org$apache$log4j$Level = cls3;
        } else {
            cls3 = class$org$apache$log4j$Level;
        }
        if (!cls2.isAssignableFrom(cls3)) {
            throw new InstantiationError("Log4J 1.2 not available");
        }
        try {
            if (class$org$apache$log4j$Level == null) {
                cls4 = class$("org.shaded.apache.log4j.Level");
                class$org$apache$log4j$Level = cls4;
            } else {
                cls4 = class$org$apache$log4j$Level;
            }
            traceLevel = (Priority) cls4.getDeclaredField(HttpTrace.METHOD_NAME).get(null);
        } catch (Exception e) {
            traceLevel = Priority.DEBUG;
        }
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public Log4JLogger() {
    }

    public Log4JLogger(String name2) {
        this.name = name2;
        this.logger = getLogger();
    }

    public Log4JLogger(Logger logger2) {
        if (logger2 == null) {
            throw new IllegalArgumentException("Warning - null logger in constructor; possible log4j misconfiguration.");
        }
        this.name = logger2.getName();
        this.logger = logger2;
    }

    public void trace(Object message) {
        getLogger().log(FQCN, traceLevel, message, null);
    }

    public void trace(Object message, Throwable t) {
        getLogger().log(FQCN, traceLevel, message, t);
    }

    public void debug(Object message) {
        getLogger().log(FQCN, Priority.DEBUG, message, null);
    }

    public void debug(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.DEBUG, message, t);
    }

    public void info(Object message) {
        getLogger().log(FQCN, Priority.INFO, message, null);
    }

    public void info(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.INFO, message, t);
    }

    public void warn(Object message) {
        getLogger().log(FQCN, Priority.WARN, message, null);
    }

    public void warn(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.WARN, message, t);
    }

    public void error(Object message) {
        getLogger().log(FQCN, Priority.ERROR, message, null);
    }

    public void error(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.ERROR, message, t);
    }

    public void fatal(Object message) {
        getLogger().log(FQCN, Priority.FATAL, message, null);
    }

    public void fatal(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.FATAL, message, t);
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.logger = Logger.getLogger(this.name);
        }
        return this.logger;
    }

    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return getLogger().isEnabledFor(Priority.ERROR);
    }

    public boolean isFatalEnabled() {
        return getLogger().isEnabledFor(Priority.FATAL);
    }

    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return getLogger().isEnabledFor(traceLevel);
    }

    public boolean isWarnEnabled() {
        return getLogger().isEnabledFor(Priority.WARN);
    }
}
