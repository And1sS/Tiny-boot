package org.and1ss.tinyboot.framework.database.converters;

public interface JdbcTypeConverterRegistry {

    JdbcTypeConverterRegistry registerTypeConverter(JdbcTypeConverter<?> typeConverter);

    <T> JdbcTypeConverter<T> getConverterForType(Class<T> typeClass);
}
