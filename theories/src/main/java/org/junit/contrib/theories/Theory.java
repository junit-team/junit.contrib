package org.junit.contrib.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Theory {
    boolean nullsAccepted() default true;
}