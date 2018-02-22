package org.roy.netty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Created: Feb 22, 2018 9:53:50 AM</p>
 * 
 * @author luoyh(Roy)
 * @version 1.0
 * @since 1.7
 */
@Target({
    ElementType.METHOD, ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rest {
    
    String value() default "";

}
