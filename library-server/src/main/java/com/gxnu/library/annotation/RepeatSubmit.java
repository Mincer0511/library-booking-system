package com.gxnu.library.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重复提交注解：标注在 Controller 方法上，
 * interval 毫秒内同一用户提交相同参数的请求将被拒绝（防止前端手抖连点 / 脚本重复提交）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /** 防重复间隔（毫秒） */
    int interval() default 3000;
}
