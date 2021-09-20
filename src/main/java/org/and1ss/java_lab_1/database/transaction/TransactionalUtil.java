package org.and1ss.java_lab_1.database.transaction;

import org.and1ss.java_lab_1.database.connection.JdbcConnectionFactory;

import java.lang.reflect.Proxy;

public class TransactionalUtil {

    public static <T> T wrapInTransaction(Object toBeProxied,
                                          Class<T> clazz,
                                          JdbcConnectionFactory jdbcConnectionFactory,
                                          TransactionManager transactionManager) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new TransactionalInvocationHandler(toBeProxied, jdbcConnectionFactory, transactionManager));
    }
}
