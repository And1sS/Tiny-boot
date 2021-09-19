package org.and1ss.java_lab_1.database.mapper;

import org.and1ss.java_lab_1.database.annotations.Column;
import org.and1ss.java_lab_1.database.annotations.Entity;
import org.and1ss.java_lab_1.database.annotations.Transient;
import org.and1ss.java_lab_1.database.converters.JdbcTypeConverter;
import org.and1ss.java_lab_1.database.converters.JdbcTypeConverterRegistry;
import org.and1ss.java_lab_1.database.util.StringUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

public class ResultSetMapper {

    private final JdbcTypeConverterRegistry converterRegistry;

    public ResultSetMapper(JdbcTypeConverterRegistry jdbcTypeConverterRegistry) {
        converterRegistry = Objects.requireNonNull(jdbcTypeConverterRegistry);
    }

    public <T> T map(T mapTo, ResultSet resultSet) {
        final Class<?> clazz = mapTo.getClass();
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Object to map from should be entity, but provided one is not");
        }

        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> mapField(mapTo, field, resultSet));
        return mapTo;
    }

    private void mapField(Object mapTo, Field field, ResultSet resultSet) {
        try {
            if (field.isAnnotationPresent(Transient.class)) {
                return;
            }

            final Column columnAnnotation = field.getAnnotation(Column.class);
            final String columnAnnotationName = columnAnnotation != null ? columnAnnotation.name() : null;
            final String columnName = columnAnnotationName != null
                    ? columnAnnotationName
                    : StringUtil.camelToSnakeCase(field.getName());

            setField(mapTo, field, resultSet.getString(columnName));
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void setField(Object object, Field field, String value) throws IllegalAccessException {
        JdbcTypeConverter<?> typeConverter = converterRegistry.getConverterForType(field.getType());
        field.setAccessible(true);
        field.set(object, typeConverter.parseString(value));
    }
}
