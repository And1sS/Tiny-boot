package org.and1ss.tinyboot.framework.database.repository;

import java.lang.reflect.Method;

public interface RepositoryMethodParser {

    String parseMethod(Method method, Class<?> entityClass, Object[] args);
}
