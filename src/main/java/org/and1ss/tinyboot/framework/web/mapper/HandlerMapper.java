package org.and1ss.tinyboot.framework.web.mapper;

import org.and1ss.tinyboot.framework.web.RequestEntity;
import org.and1ss.tinyboot.framework.web.ResponseEntity;

import java.lang.reflect.InvocationTargetException;

public interface HandlerMapper {

    void registerHandler(Object handler);

    ResponseEntity handle(RequestEntity request) throws InvocationTargetException, IllegalAccessException;
}
