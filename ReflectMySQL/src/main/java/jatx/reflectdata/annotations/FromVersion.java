package jatx.reflectdata.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FromVersion {
    int value() default 0;
}
