package org.and1ss.java_lab_1.framework.web;

import lombok.Value;

@Value(staticConstructor = "of")
public class RequestMappingInfo {

    String path;

    MethodType methodType;
}
