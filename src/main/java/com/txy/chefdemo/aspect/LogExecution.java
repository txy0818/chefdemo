package com.txy.chefdemo.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecution {
    // 是否记录方法耗时
    boolean logTime() default true;

    // 方法的返回值类型，用于AOP处理
    Class<?> returnType() default Object.class;
}