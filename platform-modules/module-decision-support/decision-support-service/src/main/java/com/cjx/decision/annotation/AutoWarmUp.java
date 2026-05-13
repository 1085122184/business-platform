package com.cjx.decision.annotation;

import java.lang.annotation.*;

/**
 * 自动缓存预热标记注解
 * 标注了此注解的方法，将会在每天定时任务触发时自动执行。
 * 注意：被标注的方法必须是 public，且必须有且仅有一个 LocalDate 类型的参数。
 * * @author cuijixu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoWarmUp{

    String value() default "";
    int order() default 0;
}
