package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import java.io.File;

public class QUtil {
    public static String getExternalStoragePath(Context context, boolean forcePrivate, boolean useLegacy) {
        return getExternalStorageDir(context, forcePrivate, useLegacy).getAbsolutePath();
    }

    public static String getExternalStoragePath(Context context, boolean forcePrivate) {
        return getExternalStoragePath(context, forcePrivate, false);
    }

    public static String getExternalStoragePath(Context context) {
        return getExternalStoragePath(context, false, false);
    }

    public static File getExternalStorageDir(Context context) {
        return getExternalStorageDir(context, false);
    }

    public static File getExternalStorageDir(Context context, boolean forcePrivate) {
        return getExternalStorageDir(context, forcePrivate, false);
    }

    public static File getExternalStorageDir(Context context, boolean forcePrivate, boolean legacy) {
        if (VERSION.SDK_INT < 8) {
            return Environment.getExternalStorageDirectory();
        }
        if ((legacy || VERSION.SDK_INT < 29) && !forcePrivate) {
            return Environment.getExternalStorageDirectory();
        }
        return context.getExternalFilesDir(null);
    }

    public static String getReplAssetPath(Context context, boolean forcePrivate) {
        if (VERSION.SDK_INT >= 29) {
            return getExternalStoragePath(context, forcePrivate) + "/assets/";
        }
        return getExternalStoragePath(context, forcePrivate) + "/AppInventor/assets/";
    }

    public static String getReplAssetPath(Context context) {
        return getReplAssetPath(context, false);
    }

    public static String getReplDataPath(Context context, boolean forcePrivate) {
        if (VERSION.SDK_INT >= 29) {
            return getExternalStoragePath(context, forcePrivate) + "/data/";
        }
        return getExternalStoragePath(context, forcePrivate) + "/AppInventor/data/";
    }

    public static String getReplDataPath(Context context) {
        return getReplDataPath(context, false);
    }
}
