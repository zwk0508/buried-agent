package com.zwk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedMethod {
    /**
     * 方法名称
     *
     * @return 方法名称
     */
    String value() default "";

    /**
     * 方法描述符
     *
     * @return 方法描述符
     */
    String descriptor() default "";
}
