package org.and1ss.java_lab_1;

import org.and1ss.java_lab_1.database.connection.JdbcConnectionFactory;
import org.and1ss.java_lab_1.database.connection.JdbcConnectionOptions;
import org.and1ss.java_lab_1.database.connection.JdbcFixedConnectionPoolFactoryImpl;
import org.and1ss.java_lab_1.database.converters.JdbcLongTypeConverter;
import org.and1ss.java_lab_1.database.converters.JdbcStringTypeConverter;
import org.and1ss.java_lab_1.database.converters.JdbcTypeConverterRegistry;
import org.and1ss.java_lab_1.database.converters.JdbcTypeConverterRegistryImpl;
import org.and1ss.java_lab_1.database.mapper.ResultSetMapper;
import org.and1ss.java_lab_1.database.repository.RepositoryInvocationHandler;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactory;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactoryImpl;
import org.and1ss.java_lab_1.database.transaction.TransactionManager;
import org.and1ss.java_lab_1.database.transaction.TransactionManagerImpl;
import org.and1ss.java_lab_1.database.transaction.TransactionalUtil;
import org.and1ss.java_lab_1.domain.User;
import org.and1ss.java_lab_1.repository.UserRepository;
import org.and1ss.java_lab_1.service.UserService;
import org.and1ss.java_lab_1.service.impl.UserServiceImpl;
import org.and1ss.java_lab_1.util.PropertiesUtil;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws IOException {
        final Properties applicationProperties = PropertiesUtil.loadProperties("application.properties");

        final JdbcConnectionOptions jdbcConnectionOptions = JdbcConnectionOptions.builder()
                .url(String.format("jdbc:postgresql://%s", applicationProperties.getProperty("database.url")))
                .user(applicationProperties.getProperty("database.user"))
                .password(applicationProperties.getProperty("database.password"))
                .build();

        final JdbcConnectionFactory jdbcConnectionFactory =
                new JdbcFixedConnectionPoolFactoryImpl(10, jdbcConnectionOptions);
        final JdbcStatementFactory jdbcStatementFactory = new JdbcStatementFactoryImpl(jdbcConnectionFactory);

        final TransactionManager transactionManager = new TransactionManagerImpl(jdbcConnectionFactory);

        final JdbcTypeConverterRegistry jdbcTypeConverterRegistry = new JdbcTypeConverterRegistryImpl()
                .registerTypeConverter(new JdbcStringTypeConverter())
                .registerTypeConverter(new JdbcLongTypeConverter());

        final UserRepository userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                new RepositoryInvocationHandler(
                        jdbcStatementFactory,
                        new ResultSetMapper(jdbcTypeConverterRegistry),
                        (a, b, c) -> null,
                        jdbcTypeConverterRegistry));

        final UserService transactionalUserService = TransactionalUtil.wrapInTransaction(
                new UserServiceImpl(userRepository),
                UserService.class,
                jdbcConnectionFactory,
                transactionManager);

        final User userWithoutId = transactionalUserService.findUserById(101L).get();
        userWithoutId.setId(null);

        System.out.println("--------- Saving user ---------\n");
        final User savedUser = transactionalUserService.save(userWithoutId);
        System.out.println(savedUser);

        System.out.println("\n\n--------- After saving --------\n");
        userRepository.findAllUsers().forEach(System.out::println);

        userRepository.deleteUserWithId(savedUser.getId());
        System.out.println("\n\n--------- After deletion ----------\n");
        userRepository.findAllUsers().forEach(System.out::println);

        jdbcConnectionFactory.closeOpenedConnections();
    }
}
