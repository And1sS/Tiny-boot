package org.and1ss.java_lab_1.framework.web.mapper;

import org.and1ss.java_lab_1.framework.web.RequestEntity;
import org.and1ss.java_lab_1.framework.web.ResponseEntity;

import java.lang.reflect.InvocationTargetException;

public interface HandlerMapper {

    void registerHandler(Object handler);

    ResponseEntity handle(RequestEntity request) throws InvocationTargetException, IllegalAccessException;
}
