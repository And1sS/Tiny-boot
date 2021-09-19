package org.and1ss.java_lab_1;

import org.and1ss.java_lab_1.database.connection.JdbcConnectionFactory;
import org.and1ss.java_lab_1.database.connection.JdbcConnectionOptions;
import org.and1ss.java_lab_1.database.connection.JdbcFixedConnectionPoolFactoryImpl;
import org.and1ss.java_lab_1.database.mapper.ResultSetMapper;
import org.and1ss.java_lab_1.database.repository.RepositoryInvocationHandler;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactory;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactoryImpl;
import org.and1ss.java_lab_1.database.transaction.TransactionalUtil;
import org.and1ss.java_lab_1.domain.User;
import org.and1ss.java_lab_1.repository.UserRepository;
import org.and1ss.java_lab_1.service.UserService;
import org.and1ss.java_lab_1.service.impl.UserServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws IOException {
        final Properties applicationProperties = new Properties();
        final InputStream applicationPropertiesStream = Application.class.getClassLoader()
                .getResourceAsStream("application.properties");
        applicationProperties.load(applicationPropertiesStream);

        final JdbcConnectionOptions jdbcConnectionOptions = JdbcConnectionOptions.builder()
                .url(String.format("jdbc:postgresql://%s", applicationProperties.getProperty("database.url")))
                .user(applicationProperties.getProperty("database.user"))
                .password(applicationProperties.getProperty("database.password"))
                .build();

        final JdbcConnectionFactory jdbcConnectionFactory =
                new JdbcFixedConnectionPoolFactoryImpl(10, jdbcConnectionOptions);
        final JdbcStatementFactory jdbcStatementFactory = new JdbcStatementFactoryImpl(jdbcConnectionFactory);

        final UserRepository userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                new RepositoryInvocationHandler(jdbcStatementFactory, new ResultSetMapper()));

        final UserService transactionalUserService = TransactionalUtil.wrapInTransaction(
                new UserServiceImpl(userRepository), UserService.class, jdbcConnectionFactory);

        final User userWithoutId = transactionalUserService.findUserById(1L).get();
        userWithoutId.setId(null);
        System.out.println(transactionalUserService.save(userWithoutId));
    }
}
