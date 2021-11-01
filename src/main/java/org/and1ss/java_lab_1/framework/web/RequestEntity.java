package org.and1ss.java_lab_1.framework.web;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Builder
@Value(staticConstructor = "of")
public class RequestEntity {

    String url;

    Map<String, String> queryParameters;

    MethodType webMethodType;

    Map<String, String> headers;

    String body;

    public RequestMappingInfo toRequestMappingInfo() {
        return RequestMappingInfo.of(url, webMethodType);
    }
}
