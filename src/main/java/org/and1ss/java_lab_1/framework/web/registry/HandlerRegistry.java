package org.and1ss.java_lab_1.framework.web.registry;

import org.and1ss.java_lab_1.framework.web.RequestHandler;
import org.and1ss.java_lab_1.framework.web.RequestMappingInfo;

public interface HandlerRegistry {

    void registerHandler(RequestMappingInfo requestMappingInfo, RequestHandler requestHandler);

    RequestHandler getHandler(RequestMappingInfo requestMappingInfo);

    String getPathVariable(RequestMappingInfo requestMappingInfo, String variableName);
}
