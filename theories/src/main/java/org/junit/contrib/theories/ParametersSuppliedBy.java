package org.junit.contrib.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * <p>Marking a {@link Theory} method parameter with this annotation causes it to be supplied with values from
 * the named {@link ParameterSupplier} when run as a theory by the {@link Theories} runner.</p>
 *
 * <p>In addition, annotations themselves can be marked with this annotation, and then used similarly.
 * ParameterSuppliedBy annotations on parameters are detected by searching up this hierarchy such that these act as
 * syntactic sugar, making:</p>
 *
 * <pre>
 * &#064;ParametersSuppliedBy(Supplier.class)
 * public &#064;interface SpecialParameter { }
 *
 * &#064;Theory
 * public void theoryMethod(&#064SpecialParameter String param) {
 *   ...
 * }
 * </pre>
 *
 * <p>equivalent to:</p>
 *
 * <pre>
 * &#064;Theory
 * public void theoryMethod(&#064;ParametersSuppliedBy(Supplier.class) String param) {
 *   ...
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({ ANNOTATION_TYPE, PARAMETER })
public @interface ParametersSuppliedBy {
    Class<? extends ParameterSupplier> value();
}
