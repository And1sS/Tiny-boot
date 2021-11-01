package org.and1ss.java_lab_1.framework.database.statement;

import org.and1ss.java_lab_1.framework.database.connection.JdbcConnectionFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class JdbcStatementFactoryImpl implements JdbcStatementFactory {

    final JdbcConnectionFactory connectionFactory;

    public JdbcStatementFactoryImpl(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return connectionFactory.getConnection().createStatement();
    }
}
