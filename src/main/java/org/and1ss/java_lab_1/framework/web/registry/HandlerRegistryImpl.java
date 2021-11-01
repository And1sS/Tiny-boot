package org.and1ss.java_lab_1.framework.web.registry;

import org.and1ss.java_lab_1.framework.web.RequestHandler;
import org.and1ss.java_lab_1.framework.web.RequestMappingInfo;
import org.bigtesting.routd.Route;
import org.bigtesting.routd.Router;
import org.bigtesting.routd.TreeRouter;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegistryImpl implements HandlerRegistry {

    private final Router router;
    private final Map<RequestMappingInfo, RequestHandler> mappingInfoToRequestHandler;

    public HandlerRegistryImpl() {
        this.router = new TreeRouter();
        this.mappingInfoToRequestHandler = new HashMap<>();
    }

    @Override
    public void registerHandler(RequestMappingInfo requestMappingInfo, RequestHandler requestHandler) {
        if (mappingInfoToRequestHandler.containsKey(requestMappingInfo)) {
            throw new RuntimeException("Ambiguous mapping for route: " + requestMappingInfo);
        }

        final String path = requestMappingInfo.getPath();
        router.add(new Route(path));
        mappingInfoToRequestHandler.put(requestMappingInfo, requestHandler);
    }

    @Override
    public RequestHandler getHandler(RequestMappingInfo requestMappingInfo) {
        final Route route = router.route(requestMappingInfo.getPath());
        if (route == null) {
            throw new IllegalArgumentException("Method not allowed: " + requestMappingInfo);
        }

        final RequestMappingInfo resolvedMappingInfo =
                RequestMappingInfo.of(route.getResourcePath(), requestMappingInfo.getMethodType());
        return mappingInfoToRequestHandler.get(resolvedMappingInfo);
    }

    @Override
    public String getPathVariable(RequestMappingInfo requestMappingInfo, String variableName) {
        final String path = requestMappingInfo.getPath();
        return router.route(path).getNamedParameter(variableName, path);
    }
}
