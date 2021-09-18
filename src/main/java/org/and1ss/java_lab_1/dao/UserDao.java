package org.and1ss.java_lab_1.dao;

import org.and1ss.java_lab_1.domain.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDao {

    Optional<User> findUserById(Long id) throws SQLException;

    User createUser(User user) throws SQLException;

    User updateUser(User user);

    User deleteUserById(Long id);
}
