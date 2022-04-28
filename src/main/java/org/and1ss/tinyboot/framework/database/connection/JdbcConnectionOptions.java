package org.and1ss.tinyboot.framework.database.connection;

import lombok.Builder;
import lombok.Value;

@Builder
@Value(staticConstructor = "of")
public class JdbcConnectionOptions {

    String url;

    String user;

    String password;
}
