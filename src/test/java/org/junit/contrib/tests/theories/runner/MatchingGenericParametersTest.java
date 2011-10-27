package org.junit.contrib.tests.theories.runner;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class MatchingGenericParametersTest {
    @DataPoint public static final List<String> strings = Arrays.asList("what");
    @DataPoint public static final List<Integer> ints = Arrays.asList(1);

    @Theory
    public void regex(List<String> strings, List<Integer> ints) {
        assertThat(strings.get(0), is(String.class));
        assertThat(ints.get(0), is(Integer.class));
    }
}
