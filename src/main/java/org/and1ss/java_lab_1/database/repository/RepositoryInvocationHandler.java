package org.and1ss.java_lab_1.database.repository;

import org.and1ss.java_lab_1.dao.Param;
import org.and1ss.java_lab_1.database.Entity;
import org.and1ss.java_lab_1.database.Query;
import org.and1ss.java_lab_1.database.mapper.ResultSetMapper;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactory;

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
import java.util.Arrays;
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

    public RepositoryInvocationHandler(JdbcStatementFactory jdbcStatementFactory, ResultSetMapper resultSetMapper) {
        this.jdbcStatementFactory = Objects.requireNonNull(jdbcStatementFactory);
        this.resultSetMapper = Objects.requireNonNull(resultSetMapper);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final List<Parameter> parameters = Arrays.asList(method.getParameters());
        Type type = method.getGenericReturnType();

        System.out.println("Return type: " + type.getTypeName());
        if (type instanceof ParameterizedType) {
            return executeQueryWithParametrizedReturnType(method, (ParameterizedType) type, args);
        } else {
            Class<?> methodReturnType = method.getReturnType();
            if (methodReturnType.isInterface()) {
                // handle projections
            } else if (methodReturnType.isAnnotationPresent(Entity.class)) {
                return executeQueryWithEntityReturnType(method, methodReturnType, args);
            } else if (methodReturnType.equals(Void.TYPE)) {
                final String query = generateSqlQueryForMethod(method, Void.TYPE, args);
                jdbcStatementFactory.createStatement().executeQuery(query);
            }
            throw new IllegalArgumentException(
                    "Repositories can return only Entities or projections and their collections, optionals");
        }
    }

    private Object executeQueryWithParametrizedReturnType(Method method, ParameterizedType returnType, Object[] args)
            throws SQLException, InvocationTargetException, InstantiationException,
            IllegalAccessException, NoSuchMethodException {

        Class<?> returnTypeClass = (Class<?>) returnType.getRawType();

        if (Optional.class.isAssignableFrom((returnTypeClass))) {
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
                throw new RuntimeException(String.format(
                        "Entities must have public no arguments constructor, but entity %s does not have it", returnType));
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
                throw new RuntimeException(String.format(
                        "Entities must have public no arguments constructor, but entity %s does not have it", returnType));
            }
        }

        return resultList;
    }

    private String generateSqlQueryForMethod(Method method, Class<?> entityClass, Object[] args) {
        final String methodName = method.getName();

        // find
        final int byIndex = methodName.indexOf("By");
        final Query queryAnnotation = method.getAnnotation(Query.class);

        if (queryAnnotation != null) {
            final String queryStringTemplate = queryAnnotation.value();
            if (queryStringTemplate == null || queryStringTemplate.isEmpty()) {
                throw new RuntimeException(
                        String.format("Query string must not be empty, but empty in method %s", method));
            }
            String queryString = queryStringTemplate;
            final Parameter[] methodParameters = method.getParameters();
            for (int i = 0; i < args.length; i++) {
                final Parameter parameter = methodParameters[i];
                final Object parameterValue = args[i];

                final Param parameterAnnotation = parameter.getAnnotation(Param.class);
                final String parameterName = parameter.getName();

                // TODO: add check
                final String parameterAlias = parameterName != null ? parameterName : parameterAnnotation.value();

                // TODO: add type converter
                queryString = queryString.replace(String.format(":%s", parameterAlias), parameterValue.toString());
            }
            System.out.println(queryString);
            return queryString;
//                    Arrays.stream(method.getParameters())
//                            .forEach(parameter -> ));
        } else if (methodName.startsWith("find")) {

        } else if (methodName.startsWith("save")) {

        } else if (methodName.startsWith("update")) {

        } else if (methodName.startsWith("delete")) {

        }
        return "SELECT id, login, first_name, last_name, password FROM usr WHERE id = 1";
    }
}
