package org.and1ss.java_lab_1.framework.database.statement;

import java.sql.SQLException;
import java.sql.Statement;

public interface JdbcStatementFactory {

    Statement createStatement() throws SQLException;
}
