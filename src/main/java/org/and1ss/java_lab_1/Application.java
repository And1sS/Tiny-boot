package org.and1ss.java_lab_1;

import org.and1ss.java_lab_1.dao.Param;
import org.and1ss.java_lab_1.dao.UserDao;
import org.and1ss.java_lab_1.dao.impl.UserDaoImpl;
import org.and1ss.java_lab_1.database.Query;
import org.and1ss.java_lab_1.database.connection.JdbcConnectionFactory;
import org.and1ss.java_lab_1.database.connection.JdbcConnectionOptions;
import org.and1ss.java_lab_1.database.connection.JdbcFixedConnectionPoolFactoryImpl;
import org.and1ss.java_lab_1.database.mapper.ResultSetMapper;
import org.and1ss.java_lab_1.database.repository.RepositoryInvocationHandler;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactory;
import org.and1ss.java_lab_1.database.statement.JdbcStatementFactoryImpl;
import org.and1ss.java_lab_1.database.transaction.TransactionalUtil;
import org.and1ss.java_lab_1.domain.User;
import org.and1ss.java_lab_1.service.UserService;
import org.and1ss.java_lab_1.service.impl.UserServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Queue;

public class Application {

    public static void main(String[] args) throws SQLException, IOException {
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

        final UserDao userDao = new UserDaoImpl(jdbcStatementFactory);
        final UserService transactionalUserService = TransactionalUtil.wrapInTransaction(
                new UserServiceImpl(userDao), UserService.class, jdbcConnectionFactory);

//        final Optional<User> optionalUser = transactionalUserService.findUserById(1L);
//        System.out.println(optionalUser.get());

        final Test test = (Test) Proxy.newProxyInstance(
                Test.class.getClassLoader(),
                new Class[]{Test.class},
                new RepositoryInvocationHandler(jdbcStatementFactory, new ResultSetMapper()));
        test.insert(101L, "firstRepositoryQueryTest", "", "", "");
        System.out.println(test.test(1L, ""));
    }

    public interface Test {
        @Query("SELECT id, login, first_name, last_name, password FROM usr WHERE id = :id")
        Queue<User> test(Long id, @Param("test") String test);

        @Query("INSERT INTO usr (id, login, first_name, last_name, password) VALUES" +
                " (:id, ':login', ':firstName', ':lastName', ':password')")
        void insert(Long id, String login, String firstName, String lastName, String password);
    }
}
