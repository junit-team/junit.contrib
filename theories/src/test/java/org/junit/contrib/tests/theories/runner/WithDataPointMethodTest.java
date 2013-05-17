package org.junit.contrib.tests.theories.runner;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.contrib.theories.internal.Assignments;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class WithDataPointMethodTest {
    @RunWith(Theories.class)
    public static class HasDataPointMethod {
        @DataPoint
        public static int oneHundred() {
            return 100;
        }

        @Theory
        public void allIntsOk(int x) {
        }
    }

    @RunWith(Theories.class)
    public static class HasUglyDataPointMethod {
        @DataPoint
        public static int oneHundred() {
            return 100;
        }

        @DataPoint
        public static int oneUglyHundred() {
            throw new RuntimeException();
        }

        @Theory
        public void allIntsOk(int x) {
        }
    }

    @Test
    public void pickUpDataPointMethods() {
        assertThat(testResult(HasDataPointMethod.class), isSuccessful());
    }

    @Test
    public void ignoreExceptionsFromDataPointMethods() {
        assertThat(testResult(HasUglyDataPointMethod.class), isSuccessful());
    }

    @RunWith(Theories.class)
    public static class DataPointMethodReturnsMutableObject {
        @DataPoint
        public static List<Object> empty() {
            return new ArrayList<Object>();
        }

        @DataPoint
        public static int ONE = 1;

        @DataPoint
        public static int TWO = 2;

        @Theory
        public void everythingsEmpty(List<Object> first, int number) {
            assertThat(first.size(), is(0));
            first.add("a");
        }
    }

    @Test
    public void mutableObjectsAreCreatedAfresh() {
        assertThat(failures(DataPointMethodReturnsMutableObject.class), empty());
    }

    @RunWith(Theories.class)
    public static class HasDateMethod {
        @DataPoint
        public int oneHundred() {
            return 100;
        }

        public Date notADataPoint() {
            return new Date();
        }

        @Theory
        public void allIntsOk(int x) {
        }

        @Theory
        public void onlyStringsOk(String s) {
        }

        @Theory
        public void onlyDatesOk(Date d) {
        }
    }

    @Test
    public void ignoreDataPointMethodsWithWrongTypes() throws Exception {
        assertThat(potentialValues(HasDateMethod.class.getMethod("onlyStringsOk", String.class))
                .toString(),
                not(containsString("100")));
    }

    @Test
    public void ignoreDataPointMethodsWithoutAnnotation() throws Throwable {
        assertThat(potentialValues(HasDateMethod.class.getMethod("onlyDatesOk", Date.class)).size(),
                is(0));
    }

    private List<PotentialAssignment> potentialValues(Method method) throws Exception {
        return Assignments.allUnassigned(method,
                new TestClass(HasDateMethod.class)).potentialsForNextUnassigned();
    }

    private List<Failure> failures(Class<?> type) {
        return JUnitCore.runClasses(type).getFailures();
    }

    private Matcher<Iterable<Failure>> empty() {
        return everyItem(nullValue(Failure.class));
    }
}
