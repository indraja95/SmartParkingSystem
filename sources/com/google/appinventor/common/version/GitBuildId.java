package com.google.appinventor.common.version;

import com.shaded.fasterxml.jackson.core.util.MinimalPrettyPrinter;

public final class GitBuildId {
    public static final String ACRA_URI = "${acra.uri}";
    public static final String ANT_BUILD_DATE = "August 31 2020";
    public static final String GIT_BUILD_FINGERPRINT = "f39892b65f668fc160fa9acf91ec058b27d80f30";
    public static final String GIT_BUILD_VERSION = "nb185";

    private GitBuildId() {
    }

    public static String getVersion() {
        String version = GIT_BUILD_VERSION;
        if (version == "" || version.contains(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR)) {
            return "none";
        }
        return version;
    }

    public static String getFingerprint() {
        return GIT_BUILD_FINGERPRINT;
    }

    public static String getDate() {
        return ANT_BUILD_DATE;
    }

    public static String getAcraUri() {
        if (ACRA_URI.equals(ACRA_URI)) {
            return "";
        }
        return ACRA_URI.trim();
    }
}
