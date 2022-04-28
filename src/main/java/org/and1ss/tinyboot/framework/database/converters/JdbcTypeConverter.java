package org.and1ss.tinyboot.framework.database.converters;

public interface JdbcTypeConverter<T> {

    String convertToString(T value);

    T parseString(String value);
}
