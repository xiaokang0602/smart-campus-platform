package com.school.common;

import java.lang.annotation.*;

/**
 * 操作日志注解：标注后由 AOP 自动记录到 operation_log 表
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    /** 操作模块 */
    String module();

    /** 动作：CREATE/UPDATE/DELETE/EXPORT/LOGIN/LOGOUT/APPROVE/REOPEN_EXAM */
    String action();

    /** 操作对象类型 */
    String targetType() default "";
}
