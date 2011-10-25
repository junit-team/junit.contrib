package org.junit.contrib.theories.suppliers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.contrib.theories.ParametersSuppliedBy;

@ParametersSuppliedBy(TestedOnSupplier.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestedOn {
	int[] ints();
}
