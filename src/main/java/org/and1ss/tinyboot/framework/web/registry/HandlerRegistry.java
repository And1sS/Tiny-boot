package org.and1ss.tinyboot.framework.web.registry;

import org.and1ss.tinyboot.framework.web.RequestHandler;
import org.and1ss.tinyboot.framework.web.RequestMappingInfo;

public interface HandlerRegistry {

    void registerHandler(RequestMappingInfo requestMappingInfo, RequestHandler requestHandler);

    RequestHandler getHandler(RequestMappingInfo requestMappingInfo);

    String getPathVariable(RequestMappingInfo requestMappingInfo, String variableName);
}
