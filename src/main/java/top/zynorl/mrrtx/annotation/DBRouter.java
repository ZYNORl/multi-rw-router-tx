package top.zynorl.mrrtx.annotation;

import java.lang.annotation.*;

/**
 * Created by zynorl on 2023/9/14 20:17
 *
 * 路由注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouter {

    // 分库分表字段
    String key() default "";

    // 是否分表
    boolean splitTable() default false;

}
