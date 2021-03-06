package org.and1ss.tinyboot.service.impl;

import org.and1ss.tinyboot.framework.database.annotations.Transactional;
import org.and1ss.tinyboot.domain.User;
import org.and1ss.tinyboot.repository.UserRepository;
import org.and1ss.tinyboot.service.UserService;

import java.util.List;
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

        final Long userId = user.getId();
        final Long resolvedUserId = userId == null
                ? userRepository.getNextId()
                : userId;

        userRepository.save(
                resolvedUserId, user.getLogin(), user.getFirstName(), user.getLastName(), user.getPassword());
        user.setId(resolvedUserId);

        return user;
    }

    @Override
    public List<User> findUsersWithName(String firstName) {
        return userRepository.findUsersWithName(firstName);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAllUsers();
    }

    @Override
    public void deleteUserWithId(Long id) {
        userRepository.deleteUserWithId(id);
    }
}
