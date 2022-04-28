package org.and1ss.tinyboot.framework.web;

import lombok.Value;

@Value(staticConstructor = "of")
public class RequestMappingInfo {

    String path;

    MethodType methodType;
}
