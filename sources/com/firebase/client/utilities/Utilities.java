package com.firebase.client.utilities;

import com.firebase.client.FirebaseException;
import com.firebase.client.core.Path;
import com.firebase.client.core.RepoInfo;
import com.shaded.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import org.shaded.apache.http.protocol.HTTP;

public class Utilities {
    public static ParsedUrl parseUrl(String url) throws FirebaseException {
        String original = url;
        try {
            int schemeOffset = original.indexOf("//");
            if (schemeOffset == -1) {
                URISyntaxException uRISyntaxException = new URISyntaxException(original, "Invalid scheme specified");
                throw uRISyntaxException;
            }
            int pathOffset = original.substring(schemeOffset + 2).indexOf("/");
            if (pathOffset != -1) {
                int pathOffset2 = pathOffset + schemeOffset + 2;
                String[] pathSegments = original.substring(pathOffset2).split("/");
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < pathSegments.length; i++) {
                    if (!pathSegments[i].equals("")) {
                        builder.append("/");
                        builder.append(URLEncoder.encode(pathSegments[i], HTTP.UTF_8));
                    }
                }
                original = original.substring(0, pathOffset2) + builder.toString();
            }
            URI uri = new URI(original);
            String pathString = uri.getPath().replace("+", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
            Validation.validateRootPathString(pathString);
            Path path = new Path(pathString);
            String scheme = uri.getScheme();
            RepoInfo repoInfo = new RepoInfo();
            repoInfo.host = uri.getHost().toLowerCase();
            int port = uri.getPort();
            if (port != -1) {
                repoInfo.secure = scheme.equals("https");
                repoInfo.host += ":" + port;
            } else {
                repoInfo.secure = true;
            }
            repoInfo.namespace = repoInfo.host.split("\\.")[0].toLowerCase();
            repoInfo.internalHost = repoInfo.host;
            ParsedUrl parsedUrl = new ParsedUrl();
            parsedUrl.path = path;
            parsedUrl.repoInfo = repoInfo;
            return parsedUrl;
        } catch (URISyntaxException e) {
            FirebaseException firebaseException = new FirebaseException("Invalid Firebase url specified", e);
            throw firebaseException;
        } catch (UnsupportedEncodingException e2) {
            FirebaseException firebaseException2 = new FirebaseException("Failed to URLEncode the path", e2);
            throw firebaseException2;
        }
    }

    public static String[] splitIntoFrames(String src, int maxFrameSize) {
        if (src.length() <= maxFrameSize) {
            return new String[]{src};
        }
        ArrayList<String> segs = new ArrayList<>();
        int i = 0;
        while (i < src.length()) {
            segs.add(src.substring(i, Math.min(i + maxFrameSize, src.length())));
            i += maxFrameSize;
        }
        return (String[]) segs.toArray(new String[segs.size()]);
    }

    public static String sha1HexDigest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input.getBytes(HTTP.UTF_8));
            return Base64.encodeBytes(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Missing SHA-1 MessageDigest provider.", e);
        } catch (UnsupportedEncodingException e2) {
            throw new RuntimeException("UTF-8 encoding is required for Firebase to run!");
        }
    }

    public static String stringHashV2Representation(String value) {
        String escaped = value;
        if (value.indexOf(92) != -1) {
            escaped = escaped.replace("\\", "\\\\");
        }
        if (value.indexOf(34) != -1) {
            escaped = escaped.replace("\"", "\\\"");
        }
        return '\"' + escaped + '\"';
    }

    public static String doubleToHashString(double value) {
        StringBuilder sb = new StringBuilder(16);
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        for (int i = 0; i < 8; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return sb.toString();
    }

    public static Integer tryParseInt(String num) {
        if (num.length() > 11 || num.length() == 0) {
            return null;
        }
        int i = 0;
        boolean negative = false;
        if (num.charAt(0) == '-') {
            if (num.length() == 1) {
                return null;
            }
            negative = true;
            i = 1;
        }
        long number = 0;
        while (i < num.length()) {
            char c = num.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
            number = (10 * number) + ((long) (c - '0'));
            i++;
        }
        if (negative) {
            if ((-number) >= -2147483648L) {
                return Integer.valueOf((int) (-number));
            }
            return null;
        } else if (number <= 2147483647L) {
            return Integer.valueOf((int) number);
        } else {
            return null;
        }
    }

    public static int compareInts(int i, int j) {
        if (i < j) {
            return -1;
        }
        if (i == j) {
            return 0;
        }
        return 1;
    }

    public static int compareLongs(long i, long j) {
        if (i < j) {
            return -1;
        }
        if (i == j) {
            return 0;
        }
        return 1;
    }

    public static <C> C castOrNull(Object o, Class<C> clazz) {
        if (clazz.isAssignableFrom(o.getClass())) {
            return o;
        }
        return null;
    }

    public static <C> C getOrNull(Object o, String key, Class<C> clazz) {
        if (o == null) {
            return null;
        }
        Object result = ((Map) castOrNull(o, Map.class)).get(key);
        if (result != null) {
            return castOrNull(result, clazz);
        }
        return null;
    }

    public static Long longFromObject(Object o) {
        if (o instanceof Integer) {
            return Long.valueOf((long) ((Integer) o).intValue());
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        return null;
    }

    public static void hardAssert(boolean condition) {
        hardAssert(condition, "");
    }

    public static void hardAssert(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("hardAssert failed: " + message);
        }
    }
}
