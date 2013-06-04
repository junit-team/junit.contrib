package org.junit.contrib.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Theory {
    boolean nullsAccepted() default true;
}
