package org.and1ss.java_lab_1.repository;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.and1ss.java_lab_1.database.annotations.Column;
import org.and1ss.java_lab_1.database.annotations.Entity;
import org.and1ss.java_lab_1.database.annotations.Query;
import org.and1ss.java_lab_1.domain.User;

import java.util.Optional;

public interface UserRepository {

    @Query("SELECT id, login, first_name, last_name, password FROM usr WHERE id = :id")
    Optional<User> findUserById(Long id);

    @Query("INSERT INTO usr (id, login, first_name, last_name, password) VALUES" +
            " (:id, ':login', ':firstName', ':lastName', ':password')")
    void save(Long id, String login, String firstName, String lastName, String password);

    @Query("select nextval('public.usr_sequence')")
    SequenceWrapper getNextId();

    @Data
    @NoArgsConstructor
    @Entity
    class SequenceWrapper {
        @Column(name = "nextval")
        Long nextValue;
    }
}
