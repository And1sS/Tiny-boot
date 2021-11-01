package org.and1ss.java_lab_1.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.and1ss.java_lab_1.domain.User;
import org.and1ss.java_lab_1.framework.web.ResponseEntity;
import org.and1ss.java_lab_1.framework.web.annotations.RestController;
import org.and1ss.java_lab_1.framework.web.annotations.args.RequestBody;
import org.and1ss.java_lab_1.framework.web.annotations.methods.GetMapping;
import org.and1ss.java_lab_1.framework.web.annotations.methods.PostMapping;
import org.and1ss.java_lab_1.framework.web.annotations.methods.RequestMapping;
import org.and1ss.java_lab_1.service.UserService;

import java.util.Objects;

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
