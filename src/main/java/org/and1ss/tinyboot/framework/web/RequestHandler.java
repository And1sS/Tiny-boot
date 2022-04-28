package org.and1ss.tinyboot.framework.web;

import lombok.Value;

import java.lang.reflect.Method;

@Value(staticConstructor = "of")
public class RequestHandler {

    Object target;

    Method method;
}
