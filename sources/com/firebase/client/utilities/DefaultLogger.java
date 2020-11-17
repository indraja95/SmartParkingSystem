package com.firebase.client.utilities;

import com.firebase.client.Logger;
import com.firebase.client.Logger.Level;
import com.shaded.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultLogger implements Logger {
    private final Set<String> enabledComponents;
    private final Level minLevel;

    public DefaultLogger(Level level, List<String> enabledComponents2) {
        if (enabledComponents2 != null) {
            this.enabledComponents = new HashSet(enabledComponents2);
        } else {
            this.enabledComponents = null;
        }
        this.minLevel = level;
    }

    public Level getLogLevel() {
        return this.minLevel;
    }

    public void onLogMessage(Level level, String tag, String message, long msTimestamp) {
        if (shouldLog(level, tag)) {
            String toLog = buildLogMessage(level, tag, message, msTimestamp);
            switch (level) {
                case ERROR:
                    error(tag, toLog);
                    return;
                case WARN:
                    warn(tag, toLog);
                    return;
                case INFO:
                    info(tag, toLog);
                    return;
                case DEBUG:
                    debug(tag, toLog);
                    return;
                default:
                    throw new RuntimeException("Should not reach here!");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String buildLogMessage(Level level, String tag, String message, long msTimestamp) {
        return new Date(msTimestamp).toString() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + "[" + level + "] " + tag + ": " + message;
    }

    /* access modifiers changed from: protected */
    public void error(String tag, String toLog) {
        System.err.println(toLog);
    }

    /* access modifiers changed from: protected */
    public void warn(String tag, String toLog) {
        System.out.println(toLog);
    }

    /* access modifiers changed from: protected */
    public void info(String tag, String toLog) {
        System.out.println(toLog);
    }

    /* access modifiers changed from: protected */
    public void debug(String tag, String toLog) {
        System.out.println(toLog);
    }

    /* access modifiers changed from: protected */
    public boolean shouldLog(Level level, String tag) {
        return level.ordinal() >= this.minLevel.ordinal() && (this.enabledComponents == null || level.ordinal() > Level.DEBUG.ordinal() || this.enabledComponents.contains(tag));
    }
}
