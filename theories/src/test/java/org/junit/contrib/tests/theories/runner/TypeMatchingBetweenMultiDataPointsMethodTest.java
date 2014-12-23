package org.junit.contrib.tests.theories.runner;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class TypeMatchingBetweenMultiDataPointsMethodTest {
    @RunWith(Theories.class)
    public static class WithWrongfullyTypedDataPointsMethod {
        @DataPoint public static String[] correctlyTyped = {"Good", "Morning"};

        @DataPoints public static String[] wrongfullyTyped() {
            return new String[] { "Hello", "World" };
        }

        @Theory public void testTheory(String[] array) {
        }
    }

    @Test public void ignoreWrongTypedDataPointsMethod() {
        assertThat(testResult(WithWrongfullyTypedDataPointsMethod.class), isSuccessful());
    }

    @RunWith(Theories.class)
    public static class WithCorrectlyTypedDataPointsMethod {
        @DataPoint public static String[] correctlyTyped = {"Good", "Morning"};

        @DataPoints public static String[][] anotherCorrectlyTyped() {
            return new String[][] { { "Hello", "World" } };
        }

        @Theory public void testTheory(String[] array) {
        }
    }

    @Test public void pickUpMultiPointDataPointMethods() throws Exception {
        assertThat(testResult(WithCorrectlyTypedDataPointsMethod.class), isSuccessful());
    }
}
