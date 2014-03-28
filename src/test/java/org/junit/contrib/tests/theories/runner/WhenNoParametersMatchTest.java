package org.junit.contrib.tests.theories.runner;

import org.hamcrest.Matcher;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.junit.experimental.results.PrintableResult.*;

@RunWith(Theories.class)
public class WhenNoParametersMatchTest {
    @DataPoints public static final int[] ints = { 0, 1, 3, 5, 1776 };

    @DataPoints public static final Matcher<?>[] matchers = { not(0), is(1) };

    @RunWith(Theories.class)
    public static class AssumptionsFail {
        @DataPoint public static int DATA = 0;

        @DataPoint public static Matcher<Integer> MATCHER = null;

        @Theory public void nonZeroIntsAreFun(int x) {
            assumeThat(x, MATCHER);
        }
    }

    @Theory public void showFailedAssumptionsWhenNoParametersFound(int data, Matcher matcher) throws Exception {
        @SuppressWarnings("unchecked")
        Matcher<Integer> typed = (Matcher<Integer>) matcher;

        assumeThat(data, not(typed));

        AssumptionsFail.DATA = data;
        AssumptionsFail.MATCHER = typed;

        String result = testResult(AssumptionsFail.class).toString();

        assertThat(result, containsString(matcher.toString()));
        assertThat(result, containsString("" + data));
    }
}
