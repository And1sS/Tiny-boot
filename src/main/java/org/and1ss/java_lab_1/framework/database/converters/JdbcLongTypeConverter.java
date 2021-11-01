package org.and1ss.java_lab_1.framework.database.converters;

public class JdbcLongTypeConverter implements JdbcTypeConverter<Long> {

    @Override
    public String convertToString(Long value) {
        return value != null ? value.toString() : "NULL";
    }

    @Override
    public Long parseString(String value) {
        return Long.parseLong(value);
    }
}
