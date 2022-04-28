package org.and1ss.tinyboot.framework.database.util;

import java.util.Set;

public class ClassUtil {

    private static final Set<Class<?>> PRIMITIVE_WRAPPER_CLASSES =
            Set.of(
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
