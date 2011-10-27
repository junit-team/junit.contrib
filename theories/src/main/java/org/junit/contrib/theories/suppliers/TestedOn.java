package org.junit.contrib.theories.suppliers;

import org.junit.contrib.theories.ParametersSuppliedBy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ParametersSuppliedBy(TestedOnSupplier.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestedOn {
    int[] ints();
}
