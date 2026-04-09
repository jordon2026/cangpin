package com.jujing.museum.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 操作标题
     */
    String title() default "";

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型：0-其他 1-新增 2-修改 3-删除
     */
    int type() default 0;
}
