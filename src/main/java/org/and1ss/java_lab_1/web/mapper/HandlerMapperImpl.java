package org.and1ss.java_lab_1.web.mapper;

import org.and1ss.java_lab_1.web.RequestEntity;
import org.and1ss.java_lab_1.web.RequestHandler;
import org.and1ss.java_lab_1.web.RequestMappingInfo;
import org.and1ss.java_lab_1.web.ResponseEntity;
import org.and1ss.java_lab_1.web.ResponseStatus;
import org.and1ss.java_lab_1.web.WebMethodType;
import org.and1ss.java_lab_1.web.annotations.RestController;
import org.and1ss.java_lab_1.web.annotations.methods.DeleteMapping;
import org.and1ss.java_lab_1.web.annotations.methods.GetMapping;
import org.and1ss.java_lab_1.web.annotations.methods.PatchMapping;
import org.and1ss.java_lab_1.web.annotations.methods.PostMapping;
import org.and1ss.java_lab_1.web.annotations.methods.PutMapping;
import org.and1ss.java_lab_1.web.annotations.methods.RequestMapping;
import org.and1ss.java_lab_1.web.registry.HandlerRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HandlerMapperImpl implements HandlerMapper {

    private static final Set<Class<? extends Annotation>> POSSIBLE_MAPPINGS =
            Set.of(
                    GetMapping.class, PostMapping.class,
                    PutMapping.class, PatchMapping.class,
                    DeleteMapping.class, RequestMapping.class);

    private final HandlerRegistry handlerRegistry;

    public HandlerMapperImpl(HandlerRegistry handlerRegistry) {
        this.handlerRegistry = Objects.requireNonNull(handlerRegistry);
    }

    @Override
    public void registerHandler(Object handler) {
        final Class<?> clazz = handler.getClass();
        if (!clazz.isAnnotationPresent(RestController.class)) {
            throw new RuntimeException("Controller class should be annotated with @RestController");
        }

        final RequestMapping requestMappingAnnotation = clazz.getAnnotation(RequestMapping.class);
        final String topLevelUrlPath = Optional.ofNullable(requestMappingAnnotation)
                .map(RequestMapping::value)
                .orElse("");

        Arrays.stream(clazz.getMethods())
                .forEach(method -> registerHandlerMethod(topLevelUrlPath, handler, method));
    }

    private void registerHandlerMethod(String topLevelUrlPath, Object target, Method method) {
        final List<Annotation> mappingAnnotations = POSSIBLE_MAPPINGS.stream()
                .map(method::getAnnotation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        final int mappingAnnotationsCount = mappingAnnotations.size();
        if (mappingAnnotationsCount > 1) {
            throw new RuntimeException(
                    "Ambiguous mapping found on handler method " + method + ": " + mappingAnnotations);
        } else if (mappingAnnotationsCount == 0) {
            return;
        }

        final RequestMappingInfo requestMappingInfo = getRequestMappingInfo(mappingAnnotations.get(0));
        final String resolvedUrlPath = topLevelUrlPath + requestMappingInfo.getPath();
        validatePath(resolvedUrlPath);

        final RequestMappingInfo resolvedRequestMappingInfo =
                RequestMappingInfo.of(resolvedUrlPath, requestMappingInfo.getMethodType());

        handlerRegistry.registerHandler(resolvedRequestMappingInfo, RequestHandler.of(target, method));
    }

    private RequestMappingInfo getRequestMappingInfo(Annotation mappingAnnotation) {
        return RequestMappingInfo.of("test", WebMethodType.GET);
//        return switch (mappingAnnotation) {
//            case RequestMapping rm -> RequestMappingInfo.of(rm.value(), rm.method());
//            case GetMapping gm -> RequestMappingInfo.of(gm.value(), WebMethodType.GET);
//            case PostMapping pm -> RequestMappingInfo.of(pm.value(), WebMethodType.POST);
//            case PutMapping pm -> RequestMappingInfo.of(pm.value(), WebMethodType.PUT);
//            case PatchMapping pm -> RequestMappingInfo.of(pm.value(), WebMethodType.PATCH);
//            case DeleteMapping dm -> RequestMappingInfo.of(dm.value(), WebMethodType.DELETE);
//            default -> throw new IllegalStateException(
//                    "Illegal annotation passed to be resolved as Mapping Annotation: " + mappingAnnotation);
//        };
    }

    private void validatePath(String path) {
    }

    @Override
    public ResponseEntity handle(RequestEntity request) {
        final RequestMappingInfo requestMappingInfo =
                RequestMappingInfo.of(request.getUrl(), request.getWebMethodType());
        final RequestHandler requestHandler = handlerRegistry.getHandler(requestMappingInfo);

        final Method handlerMethod = requestHandler.getMethod();
        final List<Object> parameters = resolveParameters(List.of(handlerMethod.getParameters()), request);

        try {
            final Object handlerResult = handlerMethod.invoke(requestHandler.getTarget(), parameters.toArray());
            return resolveHandlerResult(handlerResult);
        } catch (Throwable e) {
            return ResponseEntity.builder()
                    .statusCode(ResponseStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage())
                    .build();
        }
    }

    private List<Object> resolveParameters(List<Parameter> parameters, RequestEntity request) {
        return parameters.stream().map(a -> null).collect(Collectors.toList());
    }

    private ResponseEntity resolveHandlerResult(Object result) {
        return ResponseEntity.builder().build();
    }
}

