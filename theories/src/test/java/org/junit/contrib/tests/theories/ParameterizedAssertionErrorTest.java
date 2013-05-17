package org.junit.contrib.tests.theories;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.contrib.theories.internal.ParameterizedAssertionError;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

@RunWith(Theories.class)
public class ParameterizedAssertionErrorTest {
    @DataPoint
    public static final String METHOD_NAME = "methodName";

    @DataPoint
    public static final NullPointerException NULL_POINTER_EXCEPTION = new NullPointerException();

    @DataPoint
    public static Object[] NO_OBJECTS = new Object[0];

    @DataPoint
    public static ParameterizedAssertionError A =
            new ParameterizedAssertionError(NULL_POINTER_EXCEPTION, METHOD_NAME);

    @DataPoint
    public static ParameterizedAssertionError B =
            new ParameterizedAssertionError(NULL_POINTER_EXCEPTION, METHOD_NAME);

    @DataPoint
    public static ParameterizedAssertionError B2 =
            new ParameterizedAssertionError(NULL_POINTER_EXCEPTION, "methodName2");

    @Theory
    public void equalParameterizedAssertionErrorsHaveSameToString(
            ParameterizedAssertionError a, ParameterizedAssertionError b) {
        assumeThat(a, is(b));

        assertThat(a.toString(), is(b.toString()));
    }

    @Theory
    public void differentParameterizedAssertionErrorsHaveDifferentToStrings(
            ParameterizedAssertionError a, ParameterizedAssertionError b) {
        assumeThat(a, not(b));

        assertThat(a.toString(), not(b.toString()));
    }

    @Theory
    public void equalsReturnsTrue(Throwable targetException, String methodName, Object[] params) {
        assertThat(new ParameterizedAssertionError(targetException, methodName, params),
                is(new ParameterizedAssertionError(targetException, methodName, params)));
    }

    @Theory(nullsAccepted = false)
    public void buildParameterizedAssertionError(String methodName, String param) {
        assertThat(new ParameterizedAssertionError(new RuntimeException(), methodName, param)
                .toString(),
                containsString(methodName));
    }

    @Test
    public void canJoinWhenToStringFails() {
        assertThat(ParameterizedAssertionError.join(" ", new Object() {
            @Override
            public String toString() {
                throw new UnsupportedOperationException();
            }
        }), is("[toString failed]"));
    }
}
