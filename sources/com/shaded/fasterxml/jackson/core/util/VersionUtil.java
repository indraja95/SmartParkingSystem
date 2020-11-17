package com.shaded.fasterxml.jackson.core.util;

import com.shaded.fasterxml.jackson.core.Version;
import com.shaded.fasterxml.jackson.core.Versioned;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Pattern;
import org.shaded.apache.http.cookie.ClientCookie;
import org.shaded.apache.http.protocol.HTTP;

public class VersionUtil {
    public static final String PACKAGE_VERSION_CLASS_NAME = "PackageVersion";
    @Deprecated
    public static final String VERSION_FILE = "VERSION.txt";
    private static final Pattern VERSION_SEPARATOR = Pattern.compile("[-_./;:]");
    private final Version _version;

    protected VersionUtil() {
        Version version = null;
        try {
            version = versionFor(getClass());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load Version information for bundle (via " + getClass().getName() + ").");
        }
        if (version == null) {
            version = Version.unknownVersion();
        }
        this._version = version;
    }

    public Version version() {
        return this._version;
    }

    public static Version versionFor(Class<?> cls) {
        InputStreamReader inputStreamReader;
        Version packageVersionFor = packageVersionFor(cls);
        if (packageVersionFor != null) {
            return packageVersionFor;
        }
        InputStream resourceAsStream = cls.getResourceAsStream(VERSION_FILE);
        if (resourceAsStream == null) {
            return Version.unknownVersion();
        }
        try {
            inputStreamReader = new InputStreamReader(resourceAsStream, HTTP.UTF_8);
            Version doReadVersion = doReadVersion(inputStreamReader);
            try {
                inputStreamReader.close();
            } catch (IOException e) {
            }
            try {
                resourceAsStream.close();
                return doReadVersion;
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
        } catch (UnsupportedEncodingException e3) {
            try {
                try {
                    return Version.unknownVersion();
                } catch (IOException e4) {
                    throw new RuntimeException(e4);
                }
            } finally {
                try {
                    resourceAsStream.close();
                } catch (IOException e5) {
                    throw new RuntimeException(e5);
                }
            }
        } catch (Throwable th) {
            try {
                inputStreamReader.close();
            } catch (IOException e6) {
            }
            throw th;
        }
    }

    public static Version packageVersionFor(Class<?> cls) {
        try {
            Class cls2 = Class.forName(new StringBuilder(cls.getPackage().getName()).append(".").append(PACKAGE_VERSION_CLASS_NAME).toString(), true, cls.getClassLoader());
            if (cls2 == null) {
                return null;
            }
            try {
                Object newInstance = cls2.newInstance();
                if (newInstance instanceof Versioned) {
                    return ((Versioned) newInstance).version();
                }
                throw new IllegalArgumentException("Bad version class " + cls2.getName() + ": does not implement " + Versioned.class.getName());
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e2) {
                throw new IllegalArgumentException("Failed to instantiate " + cls2.getName() + " to find version information, problem: " + e2.getMessage(), e2);
            }
        } catch (Exception e3) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0041, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0049, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x001e  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0041 A[ExcHandler: all (r0v0 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:1:0x0006] */
    private static Version doReadVersion(Reader reader) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5 = null;
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            str = bufferedReader.readLine();
            if (str != null) {
                str2 = bufferedReader.readLine();
                if (str2 != null) {
                    str5 = bufferedReader.readLine();
                }
            } else {
                str2 = str5;
            }
            try {
                bufferedReader.close();
                String str6 = str5;
                str3 = str2;
                str4 = str6;
            } catch (IOException e) {
                String str7 = str5;
                str3 = str2;
                str4 = str7;
            }
        } catch (IOException e2) {
            str2 = str5;
            str = str5;
        } catch (Throwable th) {
        }
        if (str3 != null) {
            str3 = str3.trim();
        }
        if (str4 != null) {
            str4 = str4.trim();
        }
        return parseVersion(str, str3, str4);
        try {
            bufferedReader.close();
            String str8 = str5;
            str3 = str2;
            str4 = str8;
        } catch (IOException e3) {
            String str9 = str5;
            str3 = str2;
            str4 = str9;
        }
        if (str3 != null) {
        }
        if (str4 != null) {
        }
        return parseVersion(str, str3, str4);
        throw th;
    }

    public static Version mavenVersionFor(ClassLoader classLoader, String str, String str2) {
        InputStream resourceAsStream = classLoader.getResourceAsStream("META-INF/maven/" + str.replaceAll("\\.", "/") + "/" + str2 + "/pom.properties");
        if (resourceAsStream != null) {
            try {
                Properties properties = new Properties();
                properties.load(resourceAsStream);
                Version parseVersion = parseVersion(properties.getProperty(ClientCookie.VERSION_ATTR), properties.getProperty("groupId"), properties.getProperty("artifactId"));
                try {
                    return parseVersion;
                } catch (IOException e) {
                    return parseVersion;
                }
            } catch (IOException e2) {
            } finally {
                try {
                    resourceAsStream.close();
                } catch (IOException e3) {
                }
            }
        }
        return Version.unknownVersion();
    }

    @Deprecated
    public static Version parseVersion(String str) {
        return parseVersion(str, null, null);
    }

    public static Version parseVersion(String str, String str2, String str3) {
        int i;
        int i2;
        String str4 = null;
        if (str == null) {
            return null;
        }
        String trim = str.trim();
        if (trim.length() == 0) {
            return null;
        }
        String[] split = VERSION_SEPARATOR.split(trim);
        int parseVersionPart = parseVersionPart(split[0]);
        if (split.length > 1) {
            i = parseVersionPart(split[1]);
        } else {
            i = 0;
        }
        if (split.length > 2) {
            i2 = parseVersionPart(split[2]);
        } else {
            i2 = 0;
        }
        if (split.length > 3) {
            str4 = split[3];
        }
        return new Version(parseVersionPart, i, i2, str4, str2, str3);
    }

    protected static int parseVersionPart(String str) {
        String str2 = str.toString();
        int length = str2.length();
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char charAt = str2.charAt(i2);
            if (charAt > '9' || charAt < '0') {
                break;
            }
            i = (i * 10) + (charAt - '0');
        }
        return i;
    }

    public static final void throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
}
