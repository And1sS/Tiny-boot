package org.and1ss.java_lab_1;

import org.and1ss.java_lab_1.web.DispatcherServlet;
import org.and1ss.java_lab_1.web.RequestHandler;
import org.and1ss.java_lab_1.web.RequestMappingInfo;
import org.and1ss.java_lab_1.web.mapper.HandlerMapper;
import org.and1ss.java_lab_1.web.mapper.HandlerMapperImpl;
import org.and1ss.java_lab_1.web.registry.HandlerRegistry;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Application {

    public static void main(String[] args) throws IOException, ServletException, LifecycleException, InvocationTargetException, IllegalAccessException {
//        final Properties applicationProperties = PropertiesUtil.loadProperties("application.properties");
//
//        final JdbcConnectionOptions jdbcConnectionOptions = JdbcConnectionOptions.builder()
//                .url(String.format("jdbc:postgresql://%s", applicationProperties.getProperty("database.url")))
//                .user(applicationProperties.getProperty("database.user"))
//                .password(applicationProperties.getProperty("database.password"))
//                .build();
//
//        final JdbcConnectionFactory jdbcConnectionFactory =
//                new JdbcFixedConnectionPoolFactoryImpl(10, jdbcConnectionOptions);
//        final JdbcStatementFactory jdbcStatementFactory = new JdbcStatementFactoryImpl(jdbcConnectionFactory);
//
//        final TransactionManager transactionManager = new TransactionManagerImpl(jdbcConnectionFactory);
//
//        final JdbcTypeConverterRegistry jdbcTypeConverterRegistry = new JdbcTypeConverterRegistryImpl()
//                .registerTypeConverter(new JdbcStringTypeConverter())
//                .registerTypeConverter(new JdbcLongTypeConverter());
//
//        final UserRepository userRepository = (UserRepository) Proxy.newProxyInstance(
//                UserRepository.class.getClassLoader(),
//                new Class[]{UserRepository.class},
//                new RepositoryInvocationHandler(
//                        jdbcStatementFactory,
//                        new ResultSetMapper(jdbcTypeConverterRegistry),
//                        (a, b, c) -> null,
//                        jdbcTypeConverterRegistry));
//
//        final UserService transactionalUserService = TransactionalUtil.wrapInTransaction(
//                new UserServiceImpl(userRepository),
//                UserService.class,
//                jdbcConnectionFactory,
//                transactionManager);

//        final User userWithoutId = transactionalUserService.findUserById(101L).get();
//        userWithoutId.setId(null);
//
//        System.out.println("--------- Saving user ---------\n");
//        final User savedUser = transactionalUserService.save(userWithoutId);
//        System.out.println(savedUser);
//
//        System.out.println("\n\n--------- After saving --------\n");
//        userRepository.findAllUsers().forEach(System.out::println);
//
//        userRepository.deleteUserWithId(savedUser.getId());
//        System.out.println("\n\n--------- After deletion ----------\n");
//        userRepository.findAllUsers().forEach(System.out::println);
//
//        jdbcConnectionFactory.closeOpenedConnections();

        final HandlerRegistry handlerRegistry = new HandlerRegistry() {
            @Override
            public void registerHandler(RequestMappingInfo requestMappingInfo, RequestHandler requestHandler) {

            }

            @Override
            public RequestHandler getHandler(RequestMappingInfo requestMappingInfo) {
                return null;
            }
        };

        final HandlerMapper handlerMapper = new HandlerMapperImpl(handlerRegistry);
        final DispatcherServlet dispatcherServlet = new DispatcherServlet(handlerMapper);

        final Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(8080);

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);

        String servletName = "DispatcherServlet";
        String urlPattern = "/*";

        tomcat.addServlet(contextPath, servletName, dispatcherServlet);
        context.addServletMappingDecoded(urlPattern, servletName);

        tomcat.start();
        tomcat.getServer().await();
    }
}
