package cn.bingod.antipyretic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bin
 * @since 2017/7/7
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Param {
    String value() default "";

    int type() default 0;
}
