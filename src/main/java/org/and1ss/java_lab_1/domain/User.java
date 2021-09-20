package org.and1ss.java_lab_1.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.and1ss.java_lab_1.database.annotations.Column;
import org.and1ss.java_lab_1.database.annotations.Entity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Entity
public class User {

    Long id;

    String login;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    String password;
}
