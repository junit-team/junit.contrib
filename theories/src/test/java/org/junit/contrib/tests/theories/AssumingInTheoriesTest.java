package org.junit.contrib.tests.theories;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import static org.junit.contrib.tests.theories.TheoryTestUtils.*;

@RunWith(Theories.class)
public class AssumingInTheoriesTest {
    @Test public void noTheoryAnnotationMeansAssumeShouldIgnore() {
        Assume.assumeTrue(false);
    }

    @Test public void theoryMeansOnlyAssumeShouldFail() throws InitializationError {
        Result result = runTheoryClass(TheoryWithNoUnassumedParameters.class);
        Assert.assertEquals(1, result.getFailureCount());
    }

    /**
     * Simple class that SHOULD fail because no parameters are met.
     */
    public static class TheoryWithNoUnassumedParameters {
        @DataPoint public final static boolean FALSE = false;

        @Theory public void theoryWithNoUnassumedParameters(boolean value) {
            Assume.assumeTrue(value);
        }
    }
}
