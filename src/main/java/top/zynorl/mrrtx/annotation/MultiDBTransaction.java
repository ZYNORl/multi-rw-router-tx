package top.zynorl.mrrtx.annotation;

import java.lang.annotation.*;

/**
 * Created by zynorl on 2023/9/15 20:57
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface MultiDBTransaction {

}
