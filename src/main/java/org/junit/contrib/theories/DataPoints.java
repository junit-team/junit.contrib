package org.junit.contrib.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * <p>Marking an array or iterable-typed field or method with this annotation will cause the values in the array
 * or iterable given to be used as potential value for theory parameters in that class when run with the
 * {@link Theories} runner.</p>
 *
 * <p>Data Points will only be considered as potential values for parameters for which their types are assignable.
 * When multiple sets of DataPoints exist with overlapping types, more control can be obtained by naming the
 * data points using the value of this annotation, e.g. with <code>&#064;DataPoints({"dataset1", "dataset2"})</code>,
 * and then specifying which named set to consider as potential values for each parameter using the
 * {@link FromDataPoints} annotation.</p>
 *
 * <p>Parameters with no specified source will use all data points that are assignable to the parameter type as
 * potential values, including named sets of data points.</p>
 *
 * <p>Data points methods whose array types aren't assignable from the target parameter type (and so can't possibly
 * return relevant values) will not be called when generating values for that parameter. Iterable-typed data points
 * methods must always be called though, as this information is not available here after generic type erasure,
 * so expensive methods returning iterable data points are a bad idea.</p>
 *
 * <pre>
 * &#064;DataPoints
 * public static String[] dataPoints = new String[] { ... };
 *
 * &#064;DataPoints
 * public static String[] generatedDataPoints() {
 *     return new String[] { ... };
 * }
 *
 * &#064;Theory
 * public void theoryMethod(String param) {
 *     ...
 * }
 * </pre>
 *
 * @see DataPoint
 * @see FromDataPoints
 * @see Theories
 * @see Theory
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface DataPoints {
    String[] value() default {};

    Class<? extends Throwable>[] ignoredExceptions() default {};
}
