package org.junit.contrib.theories.suppliers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.contrib.theories.ParametersSuppliedBy;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * <p>Marking a {@link Theory} method {@code int} parameter with this annotation causes it to be supplied with values
 * from the integer array given when run as a theory by the {@link Theories} runner. For example, the below method
 * would be called three times by the Theories runner, once with each of the {@code int} parameters specified.
 *
 * <pre>
 * &#064;Theory
 * public void shouldPassForSomeInts(&#064;TestedOn(ints={1, 2, 3}) int param) {
 *     ...
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@ParametersSuppliedBy(TestedOnSupplier.class)
public @interface TestedOn {
    int[] ints();
}
