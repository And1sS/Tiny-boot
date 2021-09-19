package org.and1ss.java_lab_1.service.impl;

import org.and1ss.java_lab_1.repository.UserRepository;
import org.and1ss.java_lab_1.database.annotations.Transactional;
import org.and1ss.java_lab_1.domain.User;
import org.and1ss.java_lab_1.service.UserService;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Transactional
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findUserById(id);
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }

        final Long userId = user.getId() != null ? user.getId() : userRepository.getNextId().getNextValue();
        userRepository.insert(userId, user.getLogin(), user.getFirstName(), user.getLastName(), user.getPassword());
        user.setId(userId);
        return user;
    }
}
