package org.and1ss.java_lab_1.framework.web;

import org.and1ss.java_lab_1.framework.web.mapper.HandlerMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DispatcherServlet extends HttpServlet {

    private final HandlerMapper handlerMapper;

    public DispatcherServlet(HandlerMapper handlerMapper) {
        this.handlerMapper = Objects.requireNonNull(handlerMapper);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            final RequestEntity requestEntity = toRequestEntity(request);
            final ResponseEntity responseEntity = handlerMapper.handle(requestEntity);
            writeToResponse(responseEntity, response);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            final String errorMessage = Optional.ofNullable(e.getMessage()).orElse(e.toString());
            response.setContentLength(errorMessage.length());
            response.getWriter().print(errorMessage);
        } catch (Throwable e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            final String errorMessage = Optional.ofNullable(e.getMessage()).orElse(e.toString());
            response.setContentLength(errorMessage.length());
            response.getWriter().print(errorMessage);
        }
    }

    private RequestEntity toRequestEntity(HttpServletRequest request) throws IOException {
        return RequestEntity.builder()
                .webMethodType(MethodType.valueOf(request.getMethod()))
                .url(request.getRequestURI())
                .queryParameters(resolveQueryParameters(request))
                .headers(resolveHeaders(request))
                .body(resolveBody(request))
                .build();
    }

    private Map<String, String> resolveQueryParameters(HttpServletRequest request) {
        final String queryString = request.getQueryString();
        final String resolvedQueryString = queryString != null ? queryString : "";
        return Arrays.stream(resolvedQueryString.split("&"))
                .map(DispatcherServlet::resolveQueryStringEntry)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map.Entry<String, String> resolveQueryStringEntry(String entry) {
        if (entry.isBlank()) {
            return null;
        }

        final String[] values = entry.split("=");
        if (values.length != 2) {
            throw new RuntimeException("");
        }

        return Map.entry(values[0], values[1]);
    }

    private Map<String, String> resolveHeaders(HttpServletRequest request) {
        final Map<String, String> headers = new HashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.put(headerName, headerValues.nextElement());
            }
        }

        return headers;
    }

    private String resolveBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private void writeToResponse(ResponseEntity responseEntity, HttpServletResponse response) throws IOException {
        response.setStatus(responseEntity.getStatusCode().getCode());

        responseEntity.getHeaders().forEach(response::addHeader);

        final String servletBody = toServletBody(responseEntity.getBody());
        response.setContentLength(servletBody.length());
        response.getWriter().print(servletBody);
    }

    private String toServletBody(Object o) {
        // TODO: Add converter
        return o.toString();
    }
}
