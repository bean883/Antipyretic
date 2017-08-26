package cn.bingod.antipyretic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bin
 * @since 2017/5/12
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Table {
    String value();

    String packageName() default "";
}
