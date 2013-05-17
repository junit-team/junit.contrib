package org.junit.contrib.tests.theories.runner;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class MatchingGenericParametersTest {
    @RunWith(Theories.class)
    public static class CanDecideBetweenDifferentElementTypesOfLists {
        @DataPoint
        public static final List<String> strings = Arrays.asList("what");
        @DataPoint
        public static final List<Integer> ints = Arrays.asList(1);

        @Theory
        public void contentsOfLists(List<String> strings, List<Integer> ints) {
            assertThat(strings.get(0), is("what"));
            assertThat(ints.get(0), is(1));
        }
    }

    @Test
    public void differentElementTypes() {
        assertThat(testResult(CanDecideBetweenDifferentElementTypesOfLists.class), isSuccessful());
    }

    @RunWith(Theories.class)
    public static class CanMatchListOfSpecificTypeToListOfUnknownType {
        @DataPoint
        public static final List<String> strings = Arrays.asList("what");
        @DataPoint
        public static final List<Integer> ints = Arrays.asList(1);
        public static int count;

        @Theory
        public void contentsOfLists(List<?> items) {
            count++;
        }
    }

    @Test
    public void listOfSpecificToListOfUnknown() {
        assertThat(testResult(CanMatchListOfSpecificTypeToListOfUnknownType.class), isSuccessful());
        assertEquals(2, CanMatchListOfSpecificTypeToListOfUnknownType.count);
    }

    @RunWith(Theories.class)
    public static class CanMatchListOfUpperBoundedTypeToListOfSpecificType {
        @DataPoint
        public static final List<Integer> ints = Arrays.asList(1);

        @Theory
        public void contentsOfLists(List<? extends Number> items) {
            assertEquals(1, items.get(0));
        }
    }

    @Test
    public void listOfSpecificToListOfUpperBounded() {
        assertThat(testResult(CanMatchListOfUpperBoundedTypeToListOfSpecificType.class),
                isSuccessful());
    }

    @RunWith(Theories.class)
    public static class CanMatchListOfLowerBoundedTypeToListOfSpecificType {
        @DataPoint
        public static final List<Integer> ints = Arrays.asList(1);

        @Theory
        public void contentsOfLists(List<? super Integer> items) {
            assertEquals(1, items.get(0));
        }
    }

    @Test
    public void listOfSpecificToListOfLowerBounded() {
        assertThat(testResult(CanMatchListOfLowerBoundedTypeToListOfSpecificType.class),
                isSuccessful());
    }
}
