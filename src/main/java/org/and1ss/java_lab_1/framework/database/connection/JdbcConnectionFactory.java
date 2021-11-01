package org.and1ss.java_lab_1.framework.database.connection;

import java.sql.Connection;

public interface JdbcConnectionFactory {

    Connection getConnection();

    void releaseConnection();

    JdbcConnectionOptions getConnectionOptions();

    void closeOpenedConnections();
}
