package org.junit.contrib.tests.theories.runner;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.junit.runners.model.TestClass;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class UnsuccessfulWithDataPointFieldsTest {
    @RunWith(Theories.class)
    public static class HasAFailingTheory {
        @DataPoint public static final int ONE = 1;

        @Theory public void everythingIsZero(int x) {
            assertThat(x, is(0));
        }
    }

    @Test public void theoryClassMethodsShowUp() throws Exception {
        assertThat(new Theories(HasAFailingTheory.class).getDescription().getChildren().size(), is(1));
    }

    @Test public void theoryAnnotationsAreRetained() throws Exception {
        assertThat(new TestClass(HasAFailingTheory.class).getAnnotatedMethods(Theory.class).size(), is(1));
    }

    @Test public void canRunTheories() throws Exception {
        assertThat(testResult(HasAFailingTheory.class), hasSingleFailureContaining("Expected"));
    }

    @RunWith(Theories.class)
    public static class DoesntUseParams {
        @DataPoint public static final int ONE = 1;

        @Theory public void everythingIsZero(int x, int y) {
            assertThat(2, is(3));
        }
    }

    @Test public void reportBadParams() throws Exception {
        assertThat(
                testResult(DoesntUseParams.class),
                hasSingleFailureContaining("everythingIsZero(\"1\" <from ONE>, \"1\" <from ONE>)"));
    }

    @RunWith(Theories.class)
    public static class NullsOK {
        @DataPoint public static final String NULL = null;
        @DataPoint public static final String A = "A";

        @Theory public void everythingIsA(String a) {
            assertThat(a, is("A"));
        }
    }

    @Test public void nullsUsedUnlessProhibited() throws Exception {
        assertThat(testResult(NullsOK.class), hasSingleFailureContaining("null"));
    }

    @RunWith(Theories.class)
    public static class TheoriesMustBePublic {
        @DataPoint public static final int THREE = 3;

        @Theory void numbers(int x) {
        }
    }

    @Test public void theoriesMustBePublic() {
        assertThat(testResult(TheoriesMustBePublic.class), hasSingleFailureContaining("public"));
    }

    @RunWith(Theories.class)
    public static class DataPointFieldsMustBeStatic {
        @DataPoint public final int THREE = 3;
        @DataPoints public final int[] FOURS = new int[] { 4 };

        @Theory public void numbers(int x) {
        }
    }

    @Test public void dataPointFieldsMustBeStatic() {
        assertThat(
                testResult(DataPointFieldsMustBeStatic.class),
                CoreMatchers.<PrintableResult>both(failureCountIs(2))
                        .and(hasFailureContaining("DataPoint field THREE must be static"))
                        .and(hasFailureContaining("DataPoint field FOURS must be static")));
    }

    @RunWith(Theories.class)
    public static class DataPointMethodsMustBeStatic {
        @DataPoint public int singleDataPointMethod() {
            return 1;
        }

        @DataPoints public int[] dataPointArrayMethod() {
            return new int[] { 1, 2, 3 };
        }

        @Theory public void numbers(int x) {
        }
    }

    @Test
    public void dataPointMethodsMustBeStatic() {
        assertThat(
                testResult(DataPointMethodsMustBeStatic.class),
                CoreMatchers.<PrintableResult>both(failureCountIs(2))
                        .and(hasFailureContaining("DataPoint method singleDataPointMethod must be static"))
                        .and(hasFailureContaining("DataPoint method dataPointArrayMethod must be static")));
    }

    @RunWith(Theories.class)
    public static class DataPointFieldsMustBePublic {
        @DataPoint static final int THREE = 3;
        @DataPoints static final int[] THREES = new int[] { 3 };
        @DataPoint protected static final int FOUR = 4;
        @DataPoints protected static final int[] FOURS = new int[] { 4 };
        @DataPoint private static final int FIVE = 5;
        @DataPoints private static final int[] FIVES = new int[] { 5 };

        @Theory public void numbers(int x) {
        }
    }

    @Test public void dataPointFieldsMustBePublic() {
        PrintableResult result = testResult(DataPointFieldsMustBePublic.class);
        assertEquals(6, result.failureCount());

        assertThat(
                result,
                allOf(hasFailureContaining("DataPoint field THREE must be public"),
                        hasFailureContaining("DataPoint field THREES must be public"),
                        hasFailureContaining("DataPoint field FOUR must be public"),
                        hasFailureContaining("DataPoint field FOURS must be public"),
                        hasFailureContaining("DataPoint field FIVE must be public"),
                        hasFailureContaining("DataPoint field FIVES must be public")));
    }

    @RunWith(Theories.class)
    public static class DataPointMethodsMustBePublic {
        @DataPoint static int three() {
            return 3;
        }

        @DataPoints static int[] threes() {
            return new int[] { 3 };
        }

        @DataPoint protected static int four() {
            return 4;
        }

        @DataPoints protected static int[] fours() {
            return new int[] { 4 };
        }

        @DataPoint private static int five() {
            return 5;
        }

        @DataPoints private static int[] fives() {
            return new int[] { 5 };
        }

        @Theory public void numbers(int x) {
        }
    }

    @Test public void dataPointMethodsMustBePublic() {
        PrintableResult result = testResult(DataPointMethodsMustBePublic.class);
        assertEquals(6, result.failureCount());

        assertThat(
                result,
                allOf(hasFailureContaining("DataPoint method three must be public"),
                        hasFailureContaining("DataPoint method threes must be public"),
                        hasFailureContaining("DataPoint method four must be public"),
                        hasFailureContaining("DataPoint method fours must be public"),
                        hasFailureContaining("DataPoint method five must be public"),
                        hasFailureContaining("DataPoint method fives must be public")));
    }
}
