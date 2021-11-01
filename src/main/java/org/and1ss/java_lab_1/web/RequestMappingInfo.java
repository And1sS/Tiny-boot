package org.and1ss.java_lab_1.web;

import lombok.Value;

@Value(staticConstructor = "of")
public class RequestMappingInfo {

    String path;

    WebMethodType methodType;
}
