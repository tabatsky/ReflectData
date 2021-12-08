package jatx.reflectdata.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultMaxLength {
    int value() default 256;
}
