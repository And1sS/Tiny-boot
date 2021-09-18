package org.and1ss.java_lab_1.service.impl;

import org.and1ss.java_lab_1.dao.UserDao;
import org.and1ss.java_lab_1.database.transaction.Transactional;
import org.and1ss.java_lab_1.domain.User;
import org.and1ss.java_lab_1.service.UserService;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Transactional
public class UserServiceImpl implements UserService {

    final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao);
    }

    @Override
    public Optional<User> findUserById(Long id) throws SQLException {
        return userDao.findUserById(id);
    }
}
