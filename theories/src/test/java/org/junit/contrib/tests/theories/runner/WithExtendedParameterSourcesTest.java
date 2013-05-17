package org.junit.contrib.tests.theories.runner;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.contrib.theories.suppliers.TestedOn;
import org.junit.experimental.results.ResultMatchers;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class WithExtendedParameterSourcesTest {
    @RunWith(Theories.class)
    public static class ParameterAnnotations {
        @Theory
        public void everythingIsOne(@TestedOn(ints = { 1 }) int number) {
            assertThat(number, is(1));
        }
    }

    @Test
    public void testedOnLimitsParameters() throws Exception {
        assertThat(testResult(ParameterAnnotations.class), ResultMatchers.isSuccessful());
    }

    @RunWith(Theories.class)
    public static class ShouldFilterNull {
        @DataPoint
        public static String NULL = null;

        @DataPoint
        public static String A = "a";

        @Theory(nullsAccepted = false)
        public void allStringsAreNonNull(String s) {
            assertThat(s, notNullValue());
        }
    }

    @Test
    public void shouldFilterNull() {
        assertThat(testResult(ShouldFilterNull.class), isSuccessful());
    }

    @RunWith(Theories.class)
    public static class DataPointArrays {
        public static String log = "";

        @DataPoints
        public static String[] STRINGS = new String[] { "A", "B" };

        @Theory
        public void addToLog(String string) {
            log += string;
        }
    }

    @Test
    public void getDataPointsFromArray() {
        DataPointArrays.log = "";
        JUnitCore.runClasses(DataPointArrays.class);
        assertThat(DataPointArrays.log, is("AB"));
    }

    @RunWith(Theories.class)
    public static class DataPointArrayMethod {
        public static String log = "";

        @DataPoints
        public static String[] STRINGS() {
            return new String[] { "A", "B" };
        }

        @Theory
        public void addToLog(String string) {
            log += string;
        }
    }

    @Test
    public void getDataPointsFromArrayMethod() {
        DataPointArrayMethod.log = "";
        JUnitCore.runClasses(DataPointArrayMethod.class);
        assertThat(DataPointArrayMethod.log, is("AB"));
    }

    @RunWith(Theories.class)
    public static class DataPointMalformedArrayMethods {
        public static String log = "";

        @DataPoints
        public static String[] STRINGS() {
            return new String[] { "A", "B" };
        }

        @DataPoints
        public static String STRING() {
            return "C";
        }

        @DataPoints
        public static int[] INTS() {
            return new int[] { 1, 2, 3 };
        }

        @Theory
        public void addToLog(String string) {
            log += string;
        }
    }

    @Test
    public void getDataPointsFromArrayMethodInSpiteOfMalformedness() {
        DataPointArrayMethod.log = "";
        JUnitCore.runClasses(DataPointArrayMethod.class);
        assertThat(DataPointArrayMethod.log, is("AB"));
    }

    @RunWith(Theories.class)
    public static class DataPointArrayToBeUsedForWholeParameter {
        public static String log = "";

        @DataPoint
        public static String[] STRINGS = new String[] { "A", "B" };

        @Theory
        public void addToLog(String[] strings) {
            log += strings[0];
        }
    }

    @Test
    public void dataPointCanBeArray() {
        DataPointArrayToBeUsedForWholeParameter.log = "";
        JUnitCore.runClasses(DataPointArrayToBeUsedForWholeParameter.class);
        assertThat(DataPointArrayToBeUsedForWholeParameter.log, is("A"));
    }
}
