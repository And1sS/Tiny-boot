package org.and1ss.tinyboot.service;

import org.and1ss.tinyboot.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<User> findUserById(Long id);

    User save(User user);

    List<User> findUsersWithName(String firstName);

    List<User> findAllUsers();

    void deleteUserWithId(Long id);
}

