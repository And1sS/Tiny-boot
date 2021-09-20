package org.and1ss.java_lab_1.database.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class JdbcFixedConnectionPoolFactoryImpl implements JdbcConnectionFactory {

    final int maxConnections;
    final JdbcConnectionOptions connectionOptions;
    final List<Connection> freeConnections;
    final Map<Thread, Connection> usedConnections; // not ThreadLocal to keep track of used connections count

    public JdbcFixedConnectionPoolFactoryImpl(int maxConnections, JdbcConnectionOptions connectionOptions) {
        this.maxConnections = maxConnections;
        this.connectionOptions = Objects.requireNonNull(connectionOptions);

        freeConnections = Collections.synchronizedList(new ArrayList<>());
        usedConnections = new ConcurrentHashMap<>();
    }

    @Override
    public Connection getConnection() {
        final Thread currentThread = Thread.currentThread();
        if (usedConnections.containsKey(currentThread)) {
            return usedConnections.get(currentThread);
        }

        synchronized (this) {
            if (!freeConnections.isEmpty()) {
                return acquireConnection();
            } else if (getConnectionsCount() < maxConnections) {
                final Connection connection = createConnection();
                usedConnections.put(currentThread, connection);
                return connection;
            }
        }

        while (true) {
            synchronized (this) {
                if (freeConnections.isEmpty()) continue;
                return acquireConnection();
            }
        }
    }

    @Override
    public synchronized void releaseConnection() {
        final Thread currentThread = Thread.currentThread();
        if (usedConnections.containsKey(currentThread)) {
            freeConnections.add(usedConnections.remove(currentThread));
        } else {
            throw new IllegalStateException(
                    String.format("No connections are associated with thread %s", currentThread));
        }
    }

    private synchronized Connection acquireConnection() {
        final Connection connection = freeConnections.remove(freeConnections.size() - 1);
        usedConnections.put(Thread.currentThread(), connection);
        return connection;
    }

    @Override
    public JdbcConnectionOptions getConnectionOptions() {
        return connectionOptions;
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(
                    connectionOptions.getUrl(), connectionOptions.getUser(), connectionOptions.getPassword());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized int getConnectionsCount() {
        return freeConnections.size() + usedConnections.size();
    }

    @Override
    public void closeOpenedConnections() {
        freeConnections.forEach(this::closeConnection);
        usedConnections.values().forEach(this::closeConnection);
    }

    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
