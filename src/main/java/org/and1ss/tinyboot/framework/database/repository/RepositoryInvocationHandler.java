package org.and1ss.tinyboot.framework.database.repository;

import org.and1ss.tinyboot.framework.database.annotations.Entity;
import org.and1ss.tinyboot.framework.database.annotations.Param;
import org.and1ss.tinyboot.framework.database.annotations.Query;
import org.and1ss.tinyboot.framework.database.converters.JdbcTypeConverter;
import org.and1ss.tinyboot.framework.database.converters.JdbcTypeConverterRegistry;
import org.and1ss.tinyboot.framework.database.mapper.ResultSetMapper;
import org.and1ss.tinyboot.framework.database.statement.JdbcStatementFactory;
import org.and1ss.tinyboot.framework.database.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

// TODO: refactor this
public class RepositoryInvocationHandler implements InvocationHandler {

    final JdbcStatementFactory jdbcStatementFactory;
    final ResultSetMapper resultSetMapper;
    final RepositoryMethodParser repositoryMethodParser;
    final JdbcTypeConverterRegistry converterRegistry;

    public RepositoryInvocationHandler(JdbcStatementFactory jdbcStatementFactory,
                                       ResultSetMapper resultSetMapper,
                                       RepositoryMethodParser repositoryMethodParser,
                                       JdbcTypeConverterRegistry jdbcTypeConverterRegistry) {

        this.jdbcStatementFactory = Objects.requireNonNull(jdbcStatementFactory);
        this.resultSetMapper = Objects.requireNonNull(resultSetMapper);
        this.repositoryMethodParser = Objects.requireNonNull(repositoryMethodParser);
        this.converterRegistry = Objects.requireNonNull(jdbcTypeConverterRegistry);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Type type = method.getGenericReturnType();

        if (type instanceof ParameterizedType) {
            return executeQueryWithParametrizedReturnType(method, (ParameterizedType) type, args);
        } else {
            Class<?> methodReturnTypeClass = method.getReturnType();
            if (methodReturnTypeClass.isInterface()) {
                // handle projections
            } else if (methodReturnTypeClass.isAnnotationPresent(Entity.class)) {
                return executeQueryWithEntityReturnType(method, methodReturnTypeClass, args);
            } else if (methodReturnTypeClass.equals(Void.TYPE)) {
                final String query = generateSqlQueryForMethod(method, Void.TYPE, args);
                return jdbcStatementFactory.createStatement().executeUpdate(query);
            } else if (ClassUtil.isPrimitiveOrWrapper(methodReturnTypeClass)) {
                return executeQueryWithPrimitiveReturnType(method, methodReturnTypeClass, args);
            }
        }

        throw new RuntimeException(String.format("Could not parse query for method %s", method));
    }

    private Object executeQueryWithParametrizedReturnType(Method method, ParameterizedType returnType, Object[] args)
            throws SQLException, InvocationTargetException, InstantiationException,
            IllegalAccessException, NoSuchMethodException {

        Class<?> returnTypeClass = (Class<?>) returnType.getRawType();

        if (Optional.class.isAssignableFrom(returnTypeClass)) {
            final Type optionalArgumentType = returnType.getActualTypeArguments()[0];
            final Class<?> optionalArgumentClass = (Class<?>) optionalArgumentType;
            return Optional.ofNullable(executeQueryWithEntityReturnType(method, optionalArgumentClass, args));
        } else if (Collection.class.isAssignableFrom(returnTypeClass)) {
            final Type collectionArgumentType = returnType.getActualTypeArguments()[0];
            final Class<?> collectionArgumentClass = (Class<?>) collectionArgumentType;
            final List<Object> resultList = executeQueryWithCollectionReturnType(method, collectionArgumentClass, args);

            if (returnTypeClass.isInterface()) {
                if (List.class.isAssignableFrom(returnTypeClass)) {
                    return resultList;
                } else if (Set.class.isAssignableFrom(returnTypeClass)) {
                    return new HashSet<>(resultList);
                } else if (Queue.class.isAssignableFrom(returnTypeClass)) {
                    return new LinkedList<>(resultList);
                } else if (Collection.class.isAssignableFrom(returnTypeClass)) {
                    return resultList;
                }
            } else {
                final Constructor<?> returnTypeConstructor = returnTypeClass.getConstructor(Collection.class);
                return returnTypeConstructor.newInstance(resultList);
            }
        }

        throw new RuntimeException(
                "Repositories can return only Entities or projections and their collections, optionals");
    }

    private Object executeQueryWithEntityReturnType(Method method, Class<?> returnType, Object[] args)
            throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {

        final String query = generateSqlQueryForMethod(method, returnType, args);
        final ResultSet resultSet = jdbcStatementFactory.createStatement().executeQuery(query);

        if (resultSet.next()) {
            try {
                final Object entityObject = returnType.getConstructor().newInstance();
                return resultSetMapper.map(entityObject, resultSet);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        String.format(
                                "Entities must have public no arguments constructor, but entity %s does not have it",
                                returnType));
            }
        }
        return null;
    }

    private List<Object> executeQueryWithCollectionReturnType(Method method, Class<?> returnType, Object[] args)
            throws SQLException, InvocationTargetException,
            InstantiationException, IllegalAccessException {

        final String query = generateSqlQueryForMethod(method, returnType, args);
        final ResultSet resultSet = jdbcStatementFactory.createStatement().executeQuery(query);
        final List<Object> resultList = new ArrayList<>();

        while (resultSet.next()) {
            try {
                final Object entityObject = returnType.getConstructor().newInstance();
                resultList.add(resultSetMapper.map(entityObject, resultSet));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        String.format(
                                "Entities must have public no arguments constructor, but entity %s does not have it",
                                returnType));
            }
        }

        return resultList;
    }

    private Object executeQueryWithPrimitiveReturnType(Method method, Class<?> returnType, Object[] args)
            throws SQLException {

        final String query = generateSqlQueryForMethod(method, returnType, args);
        final ResultSet resultSet = jdbcStatementFactory.createStatement().executeQuery(query);

        if (resultSet.next()) {
            final String resultString = resultSet.getString(1);
            return converterRegistry.getConverterForType(returnType).parseString(resultString);
        }
        return null;
    }

    private String generateSqlQueryForMethod(Method method, Class<?> entityClass, Object[] args) {
        if (method.isAnnotationPresent(Query.class)) {
            return prepareQueryStringByQueryAnnotationParameter(method.getAnnotation(Query.class), method, args);
        }
        return repositoryMethodParser.parseMethod(method, entityClass, args);
    }

    private String prepareQueryStringByQueryAnnotationParameter(Query queryAnnotation, Method method, Object[] args) {
        final String queryStringTemplate = queryAnnotation.value();
        if (queryStringTemplate == null || queryStringTemplate.isEmpty()) {
            throw new RuntimeException(
                    String.format("Query string must not be empty, but empty in method %s", method));
        }

        if (args == null) {
            return queryStringTemplate;
        }

        final Parameter[] methodParameters = method.getParameters();
        String queryString = queryStringTemplate;
        for (int i = 0; i < args.length; i++) {
            queryString = setQueryStringTemplateParameter(queryString, methodParameters[i], args[i]);
        }
        return queryString;
    }

    @SuppressWarnings("unchecked")
    private String setQueryStringTemplateParameter(String queryString, Parameter parameter, Object parameterValue) {
        final Param parameterAnnotation = parameter.getAnnotation(Param.class);
        final String parameterAnnotationValue = parameterAnnotation != null ? parameterAnnotation.value() : null;
        final String parameterAlias = parameterAnnotationValue != null ? parameterAnnotationValue : parameter.getName();

        final Class<?> parameterClass = parameter.getType();
        final JdbcTypeConverter<Object> parameterTypeConverter =
                (JdbcTypeConverter<Object>) converterRegistry.getConverterForType(parameterClass);
        final String parameterStringValue = parameterTypeConverter.convertToString(parameterValue);

        return queryString.replace(String.format(":%s", parameterAlias), parameterStringValue);
    }
}
