package org.and1ss.java_lab_1.database.converters;

public interface JdbcTypeConverter<T> {

    String convertToString(T value);

    T parseString(String value);
}
