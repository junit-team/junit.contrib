package org.junit.contrib.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.contrib.theories.internal.SpecificDataPointsSupplier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * <p>Marking a parameter of a {@link Theory} method with this annotation will limit the data points considered
 * as potential values for that parameter to only the {@link DataPoints} with the given name.</p>
 *
 * <p>Data points without names will not be considered as values for any parameters annotated with
 * &#064FromDataPoints.</p>
 *
 * <pre>
 * &#064;DataPoints
 * public static String[] unnamed = new String[] { ... };
 *
 * &#064;DataPoints("regexes")
 * public static String[] regexStrings = new String[] { ... };
 *
 * &#064;DataPoints({"forMatching", "alphanumeric"})
 * public static String[] testStrings = new String[] { ... };
 *
 * &#064;Theory
 * public void stringTheory(String param) {
 *     // This will be called with every value in 'regexStrings',
 *     // 'testStrings' and 'unnamed'.
 * }
 *
 * &#064;Theory
 * public void regexTheory(&#064;FromDataPoints("regexes") String regex,
 *                         &#064;FromDataPoints("forMatching") String value) {
 *     // This will be called with only the values in 'regexStrings' as
 *     // regex, only the values in 'testStrings' as value, and none
 *     // of the values in 'unnamed'.
 * }
 * </pre>
 *
 * @see DataPoint
 * @see DataPoints
 * @see Theories
 * @see Theory
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@ParametersSuppliedBy(SpecificDataPointsSupplier.class)
public @interface FromDataPoints {
    String value();
}
