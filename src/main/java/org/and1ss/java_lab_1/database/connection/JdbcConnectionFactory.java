package org.and1ss.java_lab_1.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionFactory {

    Connection getConnection() throws SQLException;

    void releaseConnection() throws SQLException;

    JdbcConnectionOptions getConnectionOptions();
}
