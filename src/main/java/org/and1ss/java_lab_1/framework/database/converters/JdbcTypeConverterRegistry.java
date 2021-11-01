package org.and1ss.java_lab_1.framework.database.converters;

public interface JdbcTypeConverterRegistry {

    JdbcTypeConverterRegistry registerTypeConverter(JdbcTypeConverter<?> typeConverter);

    <T> JdbcTypeConverter<T> getConverterForType(Class<T> typeClass);
}
