package org.and1ss.java_lab_1;

import org.and1ss.java_lab_1.framework.database.connection.JdbcConnectionFactory;
import org.and1ss.java_lab_1.framework.database.connection.JdbcConnectionOptions;
import org.and1ss.java_lab_1.framework.database.connection.JdbcFixedConnectionPoolFactoryImpl;
import org.and1ss.java_lab_1.framework.database.converters.JdbcLongTypeConverter;
import org.and1ss.java_lab_1.framework.database.converters.JdbcStringTypeConverter;
import org.and1ss.java_lab_1.framework.database.converters.JdbcTypeConverterRegistry;
import org.and1ss.java_lab_1.framework.database.converters.JdbcTypeConverterRegistryImpl;
import org.and1ss.java_lab_1.framework.database.mapper.ResultSetMapper;
import org.and1ss.java_lab_1.framework.database.repository.RepositoryInvocationHandler;
import org.and1ss.java_lab_1.framework.database.statement.JdbcStatementFactory;
import org.and1ss.java_lab_1.framework.database.statement.JdbcStatementFactoryImpl;
import org.and1ss.java_lab_1.framework.database.transaction.TransactionManager;
import org.and1ss.java_lab_1.framework.database.transaction.TransactionManagerImpl;
import org.and1ss.java_lab_1.framework.database.transaction.TransactionalUtil;
import org.and1ss.java_lab_1.framework.util.PropertiesUtil;
import org.and1ss.java_lab_1.framework.web.DispatcherServlet;
import org.and1ss.java_lab_1.framework.web.mapper.HandlerMapper;
import org.and1ss.java_lab_1.framework.web.mapper.HandlerMapperImpl;
import org.and1ss.java_lab_1.framework.web.registry.HandlerRegistryImpl;
import org.and1ss.java_lab_1.repository.UserRepository;
import org.and1ss.java_lab_1.resource.UserResource;
import org.and1ss.java_lab_1.service.UserService;
import org.and1ss.java_lab_1.service.impl.UserServiceImpl;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws IOException, LifecycleException {
        final Properties applicationProperties = PropertiesUtil.loadProperties("application.properties");
        final JdbcConnectionOptions jdbcConnectionOptions = getJdbcConnectionOptions(applicationProperties);

        final JdbcConnectionFactory jdbcConnectionFactory =
                new JdbcFixedConnectionPoolFactoryImpl(10, jdbcConnectionOptions);
        final JdbcStatementFactory jdbcStatementFactory = new JdbcStatementFactoryImpl(jdbcConnectionFactory);
        final JdbcTypeConverterRegistry jdbcTypeConverterRegistry = getJdbcConverterRegistry();

        final TransactionManager transactionManager = new TransactionManagerImpl(jdbcConnectionFactory);
        final ResultSetMapper resultSetMapper = new ResultSetMapper(jdbcTypeConverterRegistry);

        final UserService userService = getUserService(
                jdbcConnectionFactory,
                jdbcStatementFactory,
                resultSetMapper,
                jdbcTypeConverterRegistry,
                transactionManager);


        final HandlerMapper handlerMapper = new HandlerMapperImpl(new HandlerRegistryImpl());
        handlerMapper.registerHandler(new UserResource(userService));

        final Tomcat tomcat = getTomcat(8080, handlerMapper);
        tomcat.start();
        tomcat.getServer().await();
        jdbcConnectionFactory.closeOpenedConnections();
    }

    private static JdbcTypeConverterRegistry getJdbcConverterRegistry() {
        return new JdbcTypeConverterRegistryImpl()
                .registerTypeConverter(new JdbcStringTypeConverter())
                .registerTypeConverter(new JdbcLongTypeConverter());
    }

    private static JdbcConnectionOptions getJdbcConnectionOptions(Properties applicationProperties) {
        return JdbcConnectionOptions.builder()
                .url(String.format("jdbc:postgresql://%s", applicationProperties.getProperty("database.url")))
                .user(applicationProperties.getProperty("database.user"))
                .password(applicationProperties.getProperty("database.password"))
                .build();
    }

    private static UserService getUserService(JdbcConnectionFactory jdbcConnectionFactory,
                                              JdbcStatementFactory jdbcStatementFactory,
                                              ResultSetMapper resultSetMapper,
                                              JdbcTypeConverterRegistry jdbcTypeConverterRegistry,
                                              TransactionManager transactionManager) {

        final UserRepository userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                new RepositoryInvocationHandler(
                        jdbcStatementFactory, resultSetMapper, (a, b, c) -> null, jdbcTypeConverterRegistry));

        return TransactionalUtil.wrapInTransaction(
                new UserServiceImpl(userRepository),
                UserService.class,
                jdbcConnectionFactory,
                transactionManager);
    }

    private static Tomcat getTomcat(int port, HandlerMapper handlerMapper) {
        final DispatcherServlet dispatcherServlet = new DispatcherServlet(handlerMapper);

        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);

        String servletName = "DispatcherServlet";
        String urlPattern = "/*";

        tomcat.addServlet(contextPath, servletName, dispatcherServlet);
        context.addServletMappingDecoded(urlPattern, servletName);

        return tomcat;
    }
}
