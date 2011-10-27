package org.junit.contrib.tests.theories.runner;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

public class WithOnlyTestAnnotationsTest {
    @RunWith(Theories.class)
    public static class HonorExpectedException {
        @Test(expected = NullPointerException.class)
        public void shouldThrow() {
        }
    }

    @Test
    public void honorExpected() throws Exception {
        assertThat(testResult(HonorExpectedException.class).failureCount(), is(1));
    }

    @RunWith(Theories.class)
    public static class HonorExpectedExceptionPasses {
        @Test(expected = NullPointerException.class)
        public void shouldThrow() {
            throw new NullPointerException();
        }
    }

    @Test
    public void honorExpectedPassing() throws Exception {
        assertThat(testResult(HonorExpectedExceptionPasses.class), isSuccessful());
    }

    @RunWith(Theories.class)
    public static class HonorTimeout {
        @Test(timeout = 5)
        public void shouldStop() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    @Test
    public void honorTimeout() throws Exception {
        assertThat(testResult(HonorTimeout.class), failureCountIs(1));
    }

    @RunWith(Theories.class)
    static public class ErrorWhenTestHasParametersDespiteTheories {
        @DataPoint public static final int ZERO = 0;

        @Test
        public void testMethod(int i) {
        }
    }

    @Test
    public void testErrorWhenTestHasParametersDespiteTheories() {
        JUnitCore core = new JUnitCore();
        Result result = core.run(ErrorWhenTestHasParametersDespiteTheories.class);
        assertEquals(1, result.getFailureCount());
        String message = result.getFailures().get(0).getMessage();
        assertThat(message, containsString("should have no parameters"));
    }
}
