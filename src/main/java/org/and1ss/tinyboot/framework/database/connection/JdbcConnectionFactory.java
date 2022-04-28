package org.and1ss.tinyboot.framework.database.connection;

import java.sql.Connection;

public interface JdbcConnectionFactory {

    Connection getConnection();

    void releaseConnection();

    JdbcConnectionOptions getConnectionOptions();

    void closeOpenedConnections();
}
