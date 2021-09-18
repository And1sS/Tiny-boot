package org.and1ss.java_lab_1.dao.impl;

import org.and1ss.java_lab_1.dao.UserDao;
import org.and1ss.java_lab_1.database.Column;
import org.and1ss.java_lab_1.database.Entity;
import org.and1ss.java_lab_1.database.mapper.ResultSetMapper;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactory;
import org.and1ss.java_lab_1.domain.User;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    final String INSERT_WITHOUT_ID_STATEMENT_TEMPLATE =
            "INSERT INTO usr (login, first_name, last_name, password) VALUES (\'%s\', \'%s\', \'%s\', \'%s\')";
    final String INSERT_WITH_ID_STATEMENT_TEMPLATE =
            "INSERT INTO usr (id, login, first_name, last_name, password) VALUES (%d, \'%s\', \'%s\', \'%s\', \'%s\')";
    final String FIND_BY_ID_TEMPLATE =
            "SELECT id, login, first_name, last_name, password FROM usr WHERE id = %d";

    final JdbcStatementFactory statementFactory;
    final ResultSetMapper resultSetMapper = new ResultSetMapper(); // TODO: refactor this

    public UserDaoImpl(JdbcStatementFactory statementFactory) {
        this.statementFactory = Objects.requireNonNull(statementFactory);
    }

    @Override
    public Optional<User> findUserById(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }

        final String query = String.format(FIND_BY_ID_TEMPLATE, id);
        final ResultSet resultSet = statementFactory.createStatement().executeQuery(query);
        if (resultSet.next()) {
            return Optional.of(resultSetMapper.map(new User(), resultSet));
        }

        return Optional.empty();
    }

    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        if (user.getId() == null) {
            throw new UnsupportedOperationException("User id is null");
        }

        return null;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public User deleteUserById(Long id) {
        return null;
    }
}
