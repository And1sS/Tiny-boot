package org.and1ss.tinyboot.repository;

import org.and1ss.tinyboot.framework.database.annotations.Query;
import org.and1ss.tinyboot.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    @Query("SELECT id, login, first_name, last_name, password FROM usr WHERE id = :id")
    Optional<User> findUserById(Long id);

    @Query("INSERT INTO usr (id, login, first_name, last_name, password) VALUES" +
            " (:id, ':login', ':firstName', ':lastName', ':password')")
    void save(Long id, String login, String firstName, String lastName, String password);

    @Query("SELECT * FROM usr WHERE first_name = ':firstName'")
    List<User> findUsersWithName(String firstName);

    @Query("SELECT * FROM usr")
    List<User> findAllUsers();

    @Query("select nextval('public.usr_sequence')")
    Long getNextId();

    @Query("DELETE FROM usr WHERE id = :id")
    void deleteUserWithId(Long id);
}
