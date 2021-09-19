package org.and1ss.java_lab_1.database.repository;

import java.lang.reflect.Method;

public interface RepositoryMethodParser {

    String parseMethod(Method method, Class<?> entityClass, Object[] args);
}
