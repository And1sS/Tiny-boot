package org.and1ss.java_lab_1.framework.database.converters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JdbcTypeConverterRegistryImpl implements JdbcTypeConverterRegistry {

    private Map<Class<?>, JdbcTypeConverter<?>> converterRegistry;

    public JdbcTypeConverterRegistryImpl() {
        this.converterRegistry = new HashMap<>();
    }

    public JdbcTypeConverterRegistryImpl(Map<Class<?>, JdbcTypeConverter<?>> converterRegistry) {
        this.converterRegistry = Objects.requireNonNull(converterRegistry);
    }

    @Override
    public JdbcTypeConverterRegistry registerTypeConverter(JdbcTypeConverter<?> typeConverter) {
        final Type[] genericInterfaces = typeConverter.getClass().getGenericInterfaces();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                final ParameterizedType parametrizedInterfaceType = (ParameterizedType) genericInterface;
                final Class<?> interfaceClass = (Class<?>) parametrizedInterfaceType.getRawType();
                if (interfaceClass != JdbcTypeConverter.class) continue;

                final Type[] genericTypes = parametrizedInterfaceType.getActualTypeArguments();
                if (genericTypes == null || genericTypes.length != 1) {
                    throw new RuntimeException("Invalid jdbc type converter generic arguments");
                }

                final Class<?> genericClass = (Class<?>) genericTypes[0];
                if (genericClass == null) {
                    throw new RuntimeException("Invalid jdbc type converter generic arguments");
                }

                if (converterRegistry.containsKey(genericClass)) {
                    throw new RuntimeException(
                            String.format("Jdbc type converter for type %s is already registered", genericClass));
                }

                converterRegistry.put(genericClass, typeConverter);
                return this;
            }
        }

        // should never happen
        throw new RuntimeException(String.format(
                "Specified type converter %s does not implement JdbcTypeConverter interface", typeConverter));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> JdbcTypeConverter<T> getConverterForType(Class<T> typeClass) {
        final JdbcTypeConverter<T> typeConverter = (JdbcTypeConverter<T>) converterRegistry.get(typeClass);
        if (typeConverter == null) {
            throw new RuntimeException(String.format("JdbcTypeConverter for type %s is absent", typeClass));
        }

        return typeConverter;
    }
}
