package org.and1ss.tinyboot.framework.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(staticName = "of")
public class ResponseEntity {

    public ResponseStatus statusCode;

    public Map<String, String> headers;

    public Object body;

    public static ResponseEntity ok(Object body) {
        return of(ResponseStatus.OK, Map.of(), body);
    }

    public static ResponseEntity badRequest(Object body) {
        return of(ResponseStatus.BAD_REQUEST, Map.of(), body);
    }
}

