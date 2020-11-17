package com.firebase.client.android;

import android.util.Log;
import com.firebase.client.Logger.Level;
import com.firebase.client.utilities.DefaultLogger;
import java.util.List;

public class AndroidLogger extends DefaultLogger {
    public AndroidLogger(Level level, List<String> enabledComponents) {
        super(level, enabledComponents);
    }

    /* access modifiers changed from: protected */
    public String buildLogMessage(Level level, String tag, String message, long msTimestamp) {
        return message;
    }

    /* access modifiers changed from: protected */
    public void error(String tag, String toLog) {
        Log.e(tag, toLog);
    }

    /* access modifiers changed from: protected */
    public void warn(String tag, String toLog) {
        Log.w(tag, toLog);
    }

    /* access modifiers changed from: protected */
    public void info(String tag, String toLog) {
        Log.i(tag, toLog);
    }

    /* access modifiers changed from: protected */
    public void debug(String tag, String toLog) {
        Log.d(tag, toLog);
    }
}
