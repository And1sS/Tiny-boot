package org.and1ss.tinyboot.framework.web.annotations.methods;

import org.and1ss.tinyboot.framework.web.MethodType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value() default "";

    MethodType method() default MethodType.GET;
}
