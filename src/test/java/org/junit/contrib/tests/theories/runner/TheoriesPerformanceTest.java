package org.junit.contrib.tests.theories.runner;

import org.junit.Test;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class TheoriesPerformanceTest {
    @RunWith(Theories.class)
    public static class UpToTen {
        @DataPoints public static final int[] ints = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        @Theory public void threeInts(int x, int y, int z) {
            // pass always
        }
    }

    private static final boolean TESTING_PERFORMANCE = true;

    // If we do not share the same instance of TestClass, repeatedly parsing the
    // class's annotations looking for @Befores and @Afters gets really costly.
    //
    // Likewise, the TestClass must be passed into AllMembersSupplier, or the
    // annotation parsing is again costly.
    @Test public void tryCombinationsQuickly() {
        assumeTrue(TESTING_PERFORMANCE);
        assertThat(testResult(UpToTen.class), isSuccessful());
    }
}
