package org.and1ss.java_lab_1.database.mapper;

import org.and1ss.java_lab_1.database.annotations.Column;
import org.and1ss.java_lab_1.database.annotations.Entity;
import org.and1ss.java_lab_1.database.annotations.Transient;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ResultSetMapper {

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
            final Transient transientAnnotation = field.getAnnotation(Transient.class);
            if (transientAnnotation != null) {
                return;
            }

            final Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                final String columnName = columnAnnotation.name();
                if (columnName == null) {
                    throw new NullPointerException(
                            String.format("Name of column annotation is absent in %s", mapTo.getClass()));
                }
                setField(mapTo, field, resultSet.getString(columnName));
            } else {
                setField(mapTo, field, resultSet.getString(camelToSnakeCase(field.getName())));
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void setField(Object object, Field field, String value) throws IllegalAccessException {
        // TODO: Add type converters
        if (field.getType() == String.class) {
            field.setAccessible(true);
            field.set(object, value);
        } else if (field.getType() == Long.class) {
            field.setAccessible(true);
            field.set(object, Long.parseLong(value));
        }
    }

    private static String camelToSnakeCase(String str) {
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(str.charAt(0)));

        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_').append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
