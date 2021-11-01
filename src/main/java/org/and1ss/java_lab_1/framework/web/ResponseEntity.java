package org.and1ss.java_lab_1.framework.web;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Builder
@Value(staticConstructor = "of")
public class ResponseEntity {

    public ResponseStatus statusCode;

    public Map<String, String> headers;

    public Object body;
}
