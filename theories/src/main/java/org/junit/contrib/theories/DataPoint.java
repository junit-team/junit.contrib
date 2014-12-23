package org.junit.contrib.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * <p>Marking a field or method with this annotation will cause the field value or the value returned by the method
 * to be used as a potential value for a theory parameter in that class, when run with the {@link Theories} runner.</p>
 *
 * <p>A data point is only considered as a potential value for parameters for which its type is assignable.
 * When multiple data points exist with overlapping types, more control can be obtained by naming each data point
 * using the value of this annotation, e.g. with <code>&#064;DataPoint({"dataset1", "dataset2"})</code>, and then
 * specifying which named set to consider as potential values for each parameter using the
 * {@link FromDataPoints} annotation.</p>
 *
 * <p>Parameters with no specified source will use all data points that are assignable to the parameter type
 * as potential values, including named sets of data points.</p>
 *
 * <pre>
 * &#064;DataPoint
 * public static String dataPoint = "value";
 *
 * &#064;DataPoint("generated")
 * public static String generatedDataPoint() {
 *     return "generated value";
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
@Target({FIELD, METHOD})
public @interface DataPoint {
    String[] value() default {};

    Class<? extends Throwable>[] ignoredExceptions() default {};
}
