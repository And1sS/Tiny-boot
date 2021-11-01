package org.and1ss.java_lab_1.framework.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
public @interface Transactional {

    int isolationLevel() default Connection.TRANSACTION_READ_COMMITTED;

    PropagationLevel propagationLevel() default PropagationLevel.REQUIRED;

    enum PropagationLevel {REQUIRED, REQUIRES_NEW}
}
