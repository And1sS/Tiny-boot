package org.and1ss.tinyboot.framework.database.converters;

public class JdbcStringTypeConverter implements JdbcTypeConverter<String> {

    @Override
    public String convertToString(String value) {
        return value;
    }

    @Override
    public String parseString(String value) {
        return value;
    }
}
