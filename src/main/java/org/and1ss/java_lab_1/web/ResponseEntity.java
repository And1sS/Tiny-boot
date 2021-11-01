package org.and1ss.java_lab_1.web;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

@Builder
@Value(staticConstructor = "of")
public class ResponseEntity {

    public ResponseStatus statusCode;

    @Singular
    public Map<String, String> headers;

    public Object body;
}
