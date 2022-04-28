package org.and1ss.tinyboot.framework.database.transaction;

import org.and1ss.tinyboot.framework.database.connection.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Objects;

public class TransactionManagerImpl implements TransactionManager {

    private static final Object TRANSACTION_MARKER = new Object();

    private final ThreadLocal<Object> isInTransaction;
    private final JdbcConnectionFactory connectionFactory;

    public TransactionManagerImpl(JdbcConnectionFactory jdbcConnectionFactory) {
        isInTransaction = new ThreadLocal<>();
        connectionFactory = Objects.requireNonNull(jdbcConnectionFactory);
    }

    @Override
    public void setIsolationLevel(int isolationLevel) {
        wrapInRuntimeException(() -> connectionFactory.getConnection().setTransactionIsolation(isolationLevel));
    }

    @Override
    public void beginTransaction() {
        isInTransaction.set(TRANSACTION_MARKER);
        final Connection connection = connectionFactory.getConnection();
        wrapInRuntimeException(() -> connection.setAutoCommit(false));
    }

    @Override
    public boolean isInsideTransaction() {
        return isInTransaction.get() != null;
    }

    @Override
    public Savepoint createSavepoint() {
        try {
            return connectionFactory.getConnection().setSavepoint();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollbackToSavepoint(Savepoint savepoint) {
        wrapInRuntimeException(() -> connectionFactory.getConnection().rollback(savepoint));
    }

    @Override
    public void commitTransaction() {
        wrapInRuntimeException(() -> connectionFactory.getConnection().commit());
    }

    @Override
    public void rollbackTransaction() {
        wrapInRuntimeException(() -> connectionFactory.getConnection().rollback());
    }

    @Override
    public void closeTransaction() {
        isInTransaction.remove();
    }

    private void wrapInRuntimeException(SqlRunnable runnable) {
        try {
            runnable.run();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private interface SqlRunnable {
        void run() throws SQLException;
    }
}
