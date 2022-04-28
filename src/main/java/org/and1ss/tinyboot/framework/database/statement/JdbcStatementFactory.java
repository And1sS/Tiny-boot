package org.and1ss.tinyboot.framework.database.statement;

import java.sql.SQLException;
import java.sql.Statement;

public interface JdbcStatementFactory {

    Statement createStatement() throws SQLException;
}
