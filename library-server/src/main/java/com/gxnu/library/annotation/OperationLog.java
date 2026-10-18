package com.gxnu.library.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在管理端写接口上，AOP 切面在方法执行成功后自动留痕到 sys_operation_log
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作模块，如：自习室管理 */
    String module();

    /** 操作内容描述，如：新增自习室 */
    String content();
}
