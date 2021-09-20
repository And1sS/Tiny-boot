package org.and1ss.java_lab_1.database.transaction;

import org.and1ss.java_lab_1.database.annotations.Transactional;
import org.and1ss.java_lab_1.database.connection.JdbcConnectionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Savepoint;
import java.util.Objects;

public class TransactionalInvocationHandler implements InvocationHandler {

    final Object original;
    final JdbcConnectionFactory connectionFactory;
    final TransactionManager transactionManager;

    public TransactionalInvocationHandler(Object original,
                                          JdbcConnectionFactory connectionFactory,
                                          TransactionManager transactionManager) {
        this.original = Objects.requireNonNull(original);
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!isTransactional(original, method)) {
            return method.invoke(original, args);
        }

        boolean topLevelTransaction = !transactionManager.isInsideTransaction();
        if (topLevelTransaction) {
            transactionManager.beginTransaction();
        }

        final Transactional transactionalAnnotation = getCurrentTransactionAnnotation(method);
        final Savepoint currentSavepoint = requiresNewTransaction(transactionalAnnotation)
                ? transactionManager.createSavepoint()
                : null;

        try {
            return invokeMethod(method, args, topLevelTransaction);
        } catch (Throwable e) {
            handleThrowable(topLevelTransaction, transactionalAnnotation, currentSavepoint);
            throw e;
        } finally {
            if (topLevelTransaction) {
                transactionManager.closeTransaction();
                connectionFactory.releaseConnection();
            }
        }
    }

    private Transactional getCurrentTransactionAnnotation(Method method) {
        final Transactional transactionalClassAnnotation = original.getClass().getAnnotation(Transactional.class);
        final Transactional transactionalMethodAnnotation = method.getClass().getAnnotation(Transactional.class);
        return transactionalMethodAnnotation != null
                ? transactionalMethodAnnotation
                : transactionalClassAnnotation;
    }

    private boolean isTransactional(Object proxy, Method method) {
        return proxy.getClass().isAnnotationPresent(Transactional.class)
                || method.isAnnotationPresent(Transactional.class);
    }

    private boolean requiresNewTransaction(Transactional transactionalAnnotation) {
        return transactionalAnnotation.propagationLevel() == Transactional.PropagationLevel.REQUIRES_NEW;
    }

    private Object invokeMethod(Method method, Object[] args, boolean commitTransaction)
            throws InvocationTargetException, IllegalAccessException {
        final Object result = method.invoke(original, args);
        if (commitTransaction) {
            transactionManager.commitTransaction();
        }
        return result;
    }

    private void handleThrowable(boolean isTopLevelTransaction,
                                 Transactional transactionalAnnotation,
                                 Savepoint currentSavepoint) {
        if (isTopLevelTransaction) {
            transactionManager.rollbackTransaction();
        } else if (requiresNewTransaction(transactionalAnnotation)) {
            transactionManager.rollbackToSavepoint(currentSavepoint);
        }
    }
}
