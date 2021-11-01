package org.and1ss.java_lab_1.web.registry;

import org.and1ss.java_lab_1.web.RequestHandler;
import org.and1ss.java_lab_1.web.RequestMappingInfo;

public interface HandlerRegistry {

    void registerHandler(RequestMappingInfo requestMappingInfo, RequestHandler requestHandler);

    RequestHandler getHandler(RequestMappingInfo requestMappingInfo);
}
