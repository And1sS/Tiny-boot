package org.and1ss.java_lab_1.web;

import lombok.Value;

import java.lang.reflect.Method;

@Value(staticConstructor = "of")
public class RequestHandler {

    Object target;

    Method method;
}