package org.and1ss.java_lab_1.database.transaction;

import org.and1ss.java_lab_1.database.annotations.Transactional;
import org.and1ss.java_lab_1.database.connection.JdbcConnectionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Objects;

public class TransactionalInvocationHandler implements InvocationHandler {

    final JdbcConnectionFactory connectionFactory;
    final Object original;

    public TransactionalInvocationHandler(Object original, JdbcConnectionFactory connectionFactory) {
        this.original = Objects.requireNonNull(original);
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!isTransactional(original, method)) {
            return method.invoke(original, args);
        }

        final Connection connection = connectionFactory.getConnection();
        connection.setAutoCommit(false);
        try {
            final Object result = method.invoke(original, args);
            connection.commit();
            return result;
        } catch (Throwable e) {
            connection.rollback();
            throw e;
        } finally {
            connectionFactory.releaseConnection();
        }
    }

    private boolean isTransactional(Object proxy, Method method) {
        return proxy.getClass().isAnnotationPresent(Transactional.class)
                || method.isAnnotationPresent(Transactional.class);
    }
}
