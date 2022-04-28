package org.and1ss.tinyboot.framework.web.mapper;

import org.and1ss.tinyboot.framework.web.MethodType;
import org.and1ss.tinyboot.framework.web.RequestEntity;
import org.and1ss.tinyboot.framework.web.RequestHandler;
import org.and1ss.tinyboot.framework.web.RequestMappingInfo;
import org.and1ss.tinyboot.framework.web.ResponseEntity;
import org.and1ss.tinyboot.framework.web.annotations.RestController;
import org.and1ss.tinyboot.framework.web.annotations.args.PathVariable;
import org.and1ss.tinyboot.framework.web.annotations.args.RequestBody;
import org.and1ss.tinyboot.framework.web.annotations.args.RequestParam;
import org.and1ss.tinyboot.framework.web.annotations.methods.DeleteMapping;
import org.and1ss.tinyboot.framework.web.annotations.methods.GetMapping;
import org.and1ss.tinyboot.framework.web.annotations.methods.PatchMapping;
import org.and1ss.tinyboot.framework.web.annotations.methods.PostMapping;
import org.and1ss.tinyboot.framework.web.annotations.methods.PutMapping;
import org.and1ss.tinyboot.framework.web.annotations.methods.RequestMapping;
import org.and1ss.tinyboot.framework.web.registry.HandlerRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HandlerMapperImpl implements HandlerMapper {

    private static final Set<Class<? extends Annotation>> POSSIBLE_MAPPINGS =
            Set.of(
                    GetMapping.class, PostMapping.class,
                    PutMapping.class, PatchMapping.class,
                    DeleteMapping.class, RequestMapping.class);
    private static final String URL_PATH_REGEX = "[a-z0-9\\-._~%!$&'()*+,;=:@\\/]*";

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
        if (mappingAnnotation instanceof RequestMapping) {
            final RequestMapping rm = (RequestMapping) mappingAnnotation;
            return RequestMappingInfo.of(rm.value(), rm.method());
        } else if (mappingAnnotation instanceof GetMapping) {
            return RequestMappingInfo.of(((GetMapping) mappingAnnotation).value(), MethodType.GET);
        } else if (mappingAnnotation instanceof PostMapping) {
            return RequestMappingInfo.of(((PostMapping) mappingAnnotation).value(), MethodType.POST);
        } else if (mappingAnnotation instanceof PutMapping) {
            return RequestMappingInfo.of(((PutMapping) mappingAnnotation).value(), MethodType.PUT);
        } else if (mappingAnnotation instanceof PatchMapping) {
            return RequestMappingInfo.of(((PatchMapping) mappingAnnotation).value(), MethodType.PATCH);
        } else if (mappingAnnotation instanceof DeleteMapping) {
            return RequestMappingInfo.of(((DeleteMapping) mappingAnnotation).value(), MethodType.DELETE);
        }
        throw new IllegalStateException(
                    "Illegal annotation passed to be resolved as Mapping Annotation: " + mappingAnnotation);
//        return switch (mappingAnnotation) {
//            case RequestMapping rm -> RequestMappingInfo.of(rm.value(), rm.method());
//            case GetMapping gm -> RequestMappingInfo.of(gm.value(), MethodType.GET);
//            case PostMapping pm -> RequestMappingInfo.of(pm.value(), MethodType.POST);
//            case PutMapping pm -> RequestMappingInfo.of(pm.value(), MethodType.PUT);
//            case PatchMapping pm -> RequestMappingInfo.of(pm.value(), MethodType.PATCH);
//            case DeleteMapping dm -> RequestMappingInfo.of(dm.value(), MethodType.DELETE);
//            default -> throw new IllegalStateException(
//                    "Illegal annotation passed to be resolved as Mapping Annotation: " + mappingAnnotation);
//        };
    }

    private void validatePath(String path) {
    }

    @Override
    public ResponseEntity handle(RequestEntity request) throws InvocationTargetException, IllegalAccessException {
        final RequestMappingInfo requestMappingInfo =
                RequestMappingInfo.of(request.getUrl(), request.getWebMethodType());
        final RequestHandler requestHandler = handlerRegistry.getHandler(requestMappingInfo);
        if (requestHandler == null) {
            throw new IllegalArgumentException("Method not allowed: " + requestMappingInfo);
        }

        final Method handlerMethod = requestHandler.getMethod();
        final List<Object> parameters = resolveParameters(
                List.of(handlerMethod.getParameters()), List.of(handlerMethod.getParameterTypes()), request);

        final Object handlerResult = handlerMethod.invoke(requestHandler.getTarget(), parameters.toArray());
        return resolveHandlerResult(handlerResult);
    }

    private List<Object> resolveParameters(List<Parameter> parameters,
                                           List<Class<?>> parameterTypes,
                                           RequestEntity request) {

        return IntStream.range(0, parameters.size())
                .mapToObj(idx -> resolveParameter(parameters.get(idx), parameterTypes.get(idx), request))
                .collect(Collectors.toList());
    }

    private Object resolveParameter(Parameter parameter, Class<?> parameterType, RequestEntity request) {
        if (parameter.isAnnotationPresent(RequestParam.class)) {
            return resolveRequestParam(parameter, parameterType, request);
        }
        if (parameter.isAnnotationPresent(PathVariable.class)) {
            return resolvePathVariable(parameter, parameterType, request);
        }
        if (parameter.isAnnotationPresent(RequestBody.class)) {
            return resolveRequestBody(parameterType, request);
        }

        return null;
    }

    private static Object resolveRequestParam(Parameter parameter, Class<?> parameterType, RequestEntity request) {
        final RequestParam requestParamAnnotation = parameter.getAnnotation(RequestParam.class);
        final String name = requestParamAnnotation.value();
        if (name == null) {
            throw new IllegalStateException("Request param name should not be null");
        }
        final Object resolvedParameter =
                resolveParameter(parameter, parameterType, request.getQueryParameters().get(name));

        if (resolvedParameter == null && requestParamAnnotation.required()) {
            throw new IllegalArgumentException("Request param " + name + " is required, but not provided");
        }
        return resolvedParameter;
    }

    private Object resolvePathVariable(Parameter parameter, Class<?> parameterType, RequestEntity request) {
        final PathVariable pathVariableAnnotation = parameter.getAnnotation(PathVariable.class);
        final String name = pathVariableAnnotation.value();
        if (name == null) {
            throw new IllegalStateException("Path variable name should not be null");
        }

        final String pathVariable = handlerRegistry.getPathVariable(request.toRequestMappingInfo(), name);
        return resolveParameter(parameter, parameterType, pathVariable);
    }

    private static Object resolveRequestBody(Class<?> parameterType, RequestEntity request) {
        if (parameterType.isAssignableFrom(String.class)) {
            return request.getBody();
        }

        // TODO: Add converters
        return null;
    }

    private static Object resolveParameter(Parameter parameter, Class<?> parameterType, String value) {
        if (value == null) {
            return null;
        }

        if (parameterType.isAssignableFrom(String.class)) {
            return value;
        }

        // TODO: Add converters
        return null;
    }

    private ResponseEntity resolveHandlerResult(Object result) {
        if (result instanceof ResponseEntity) {
            return (ResponseEntity) result;
        }

        // TODO: Add converters
        return ResponseEntity.builder()
                .build();
    }
}

