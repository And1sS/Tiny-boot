package org.and1ss.java_lab_1.database.util;

import java.util.Arrays;
import java.util.List;

public class ClassUtil {

    private static final List<Class<?>> PRIMITIVE_WRAPPER_CLASSES =
            Arrays.asList(
                    Boolean.class, Byte.class,
                    Character.class, Double.class,
                    Float.class, Integer.class,
                    Long.class, Short.class);

    private ClassUtil() {
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || PRIMITIVE_WRAPPER_CLASSES.contains(clazz);
    }
}
