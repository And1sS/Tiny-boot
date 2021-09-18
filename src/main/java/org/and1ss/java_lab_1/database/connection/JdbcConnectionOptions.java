package org.and1ss.java_lab_1.database.connection;

import lombok.Builder;
import lombok.Value;

@Builder
@Value(staticConstructor = "of")
public class JdbcConnectionOptions {

    String url;

    String user;

    String password;
}
