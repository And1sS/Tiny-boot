package org.and1ss.tinyboot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.and1ss.tinyboot.framework.database.annotations.Column;
import org.and1ss.tinyboot.framework.database.annotations.Entity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Entity
public class User {

    private Long id;

    private String login;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String password;
}
