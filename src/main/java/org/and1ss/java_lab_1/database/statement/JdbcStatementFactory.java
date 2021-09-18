package org.and1ss.java_lab_1.database.statement;

import java.sql.SQLException;
import java.sql.Statement;

public interface JdbcStatementFactory {

    Statement createStatement() throws SQLException;
}
