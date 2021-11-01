package org.and1ss.java_lab_1.web.mapper;

import org.and1ss.java_lab_1.web.RequestEntity;
import org.and1ss.java_lab_1.web.ResponseEntity;

public interface HandlerMapper {

    void registerHandler(Object handler);

    ResponseEntity handle(RequestEntity request);
}
