# Tiny Boot
## What is it?
Tiny boot is small PoC of web mvc and database frameworks for building web applications.</br>
Based on Java JDBC API and Servlet API. For Servlet containers embeded version of Tomcat is used. 
## What about features?
Tiny boot contains Spring like MVC framework and simple ORM(a bit like Spring Data JDBC).

## MVC
This MVC framework implementation contains:
- Request routing based on http url and methods. (https://github.com/And1sS/Tiny-boot/tree/master/src/main/java/org/and1ss/tinyboot/framework/web/mapper)
- Handlers registry. (https://github.com/And1sS/Tiny-boot/tree/master/src/main/java/org/and1ss/tinyboot/framework/web/registry)
- Dispatcher servlet which combines everything together. (https://github.com/And1sS/Tiny-boot/blob/master/src/main/java/org/and1ss/tinyboot/framework/web/DispatcherServlet.java)

To define simple REST handler you need to create class, annotate it with @RestController and create some public handler methods.</br>
For example: 

```java
@RestController
@RequestMapping("/api/user")
public class UserResource {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = Objects.requireNonNull(userService);
    }

    @GetMapping("/all")
    public ResponseEntity getAllUsers() throws JsonProcessingException {
        return ResponseEntity.ok(objectMapper.writeValueAsString(userService.findAllUsers()));
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody String body) throws JsonProcessingException {
        final User user = objectMapper.readValue(body, User.class);
        return ResponseEntity.ok(objectMapper.writeValueAsString(userService.save(user)));
    }
}
```
Then you need to register this handler into HandlerMapper and pass it into DispatcherServlet instance.
Like this: 
```java
final HandlerMapper handlerMapper = new HandlerMapperImpl(new HandlerRegistryImpl());
handlerMapper.registerHandler(new UserResource(userService));

final Tomcat tomcat = getTomcat(8080, handlerMapper);
```
Just like in Spring MVC, but with reduced functionality.
Full example can be found at: https://github.com/And1sS/Tiny-boot/blob/master/src/main/java/org/and1ss/tinyboot/resource/UserResource.java

## ORM
My implementation of simple orm contains:
- Fixed size JDBC connections pool. (https://github.com/And1sS/Tiny-boot/tree/master/src/main/java/org/and1ss/tinyboot/framework/database/connection)
- Jdbc Types converters and their registry. (https://github.com/And1sS/Tiny-boot/tree/master/src/main/java/org/and1ss/tinyboot/framework/database/converters)
- Entity mapping with @Entity, @Table, @Column just like in JPA. (no relationships mapping implemented for now).
- Spring-like Repository interfaces with @Query methods and arguments, Entity mapping. (https://github.com/And1sS/Tiny-boot/tree/master/src/main/java/org/and1ss/tinyboot/framework/database/repository)
- Spring-like thread bound @Transactional capabilities with transaction propagation and inner transactions managed by Transaction Manager(https://github.com/And1sS/Tiny-boot/tree/master/src/main/java/org/and1ss/tinyboot/framework/database/repository).

To define simple repository and connect it to the database you need to: </br>
1. create interface with @Query methods and annotate it with @Repository.
For example: 
```java

public interface UserRepository {

    @Query("SELECT id, login, first_name, last_name, password FROM usr WHERE id = :id")
    Optional<User> findUserById(Long id);

    @Query("INSERT INTO usr (id, login, first_name, last_name, password) VALUES" +
            " (:id, ':login', ':firstName', ':lastName', ':password')")
    void save(Long id, String login, String firstName, String lastName, String password);

    @Query("SELECT * FROM usr WHERE first_name = ':firstName'")
    List<User> findUsersWithName(String firstName);

    @Query("SELECT * FROM usr")
    List<User> findAllUsers();

    @Query("select nextval('public.usr_sequence')")
    Long getNextId();

    @Query("DELETE FROM usr WHERE id = :id")
    void deleteUserWithId(Long id);
}
```
2. Create connection pool like so:
```java

final Properties applicationProperties = PropertiesUtil.loadProperties("application.properties");
final JdbcConnectionOptions jdbcConnectionOptions = getJdbcConnectionOptions(applicationProperties);

final JdbcConnectionFactory jdbcConnectionFactory =
        new JdbcFixedConnectionPoolFactoryImpl(10, jdbcConnectionOptions);
final JdbcStatementFactory jdbcStatementFactory = new JdbcStatementFactoryImpl(jdbcConnectionFactory);

private static JdbcConnectionOptions getJdbcConnectionOptions(Properties applicationProperties) {
    return JdbcConnectionOptions.builder()
            .url(String.format("jdbc:postgresql://%s", applicationProperties.getProperty("database.url")))
            .user(applicationProperties.getProperty("database.user"))
            .password(applicationProperties.getProperty("database.password"))
            .build();
}
```

3. Create JdbcTypeConverterRegistry like so: 
```java
return new JdbcTypeConverterRegistryImpl()
                .registerTypeConverter(new JdbcStringTypeConverter())
                .registerTypeConverter(new JdbcLongTypeConverter());
```

4. Create ResultSetMapper like so:
```java
final ResultSetMapper resultSetMapper = new ResultSetMapper(jdbcTypeConverterRegistry);
```

5. Create proxy to combine all together: 
```java
final UserRepository userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                new RepositoryInvocationHandler(
                        jdbcStatementFactory, resultSetMapper, (a, b, c) -> null, jdbcTypeConverterRegistry));
```

Additionally, to add transaction management:
1. Annotate your service class or its method with @Transactional just like in Spring:

```java
@Transactional
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    @Override
    @Transactional(propagationLevel = Transactional.PropagationLevel.REQUIRES_NEW)
    public Optional<User> findUserById(Long id) {
        return userRepository.findUserById(id);
    }

    ...other methods
}
```

2. Get proxied with transactions instance of your service:
```java
TransactionalUtil.wrapInTransaction(
                new UserServiceImpl(userRepository),
                UserService.class,
                jdbcConnectionFactory,
                transactionManager)
```

Full example can be found in this repository under domain, repository, resource and service packages + Application.java.

