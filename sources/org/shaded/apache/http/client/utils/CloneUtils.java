package org.shaded.apache.http.client.utils;

import java.lang.reflect.InvocationTargetException;
import org.shaded.apache.http.annotation.Immutable;

@Immutable
public class CloneUtils {
    public static Object clone(Object obj) throws CloneNotSupportedException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Cloneable) {
            try {
                try {
                    return obj.getClass().getMethod("clone", null).invoke(obj, null);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof CloneNotSupportedException) {
                        throw ((CloneNotSupportedException) cause);
                    }
                    throw new Error("Unexpected exception", cause);
                } catch (IllegalAccessException ex2) {
                    throw new IllegalAccessError(ex2.getMessage());
                }
            } catch (NoSuchMethodException ex3) {
                throw new NoSuchMethodError(ex3.getMessage());
            }
        } else {
            throw new CloneNotSupportedException();
        }
    }

    private CloneUtils() {
    }
}
