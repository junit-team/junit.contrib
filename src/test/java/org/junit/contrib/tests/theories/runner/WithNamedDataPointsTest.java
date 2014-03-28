package org.junit.contrib.tests.theories.runner;

import java.util.List;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.FromDataPoints;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.contrib.tests.theories.TheoryTestUtils.*;

public class WithNamedDataPointsTest {
    @RunWith(Theories.class)
    public static class HasSpecificDatapointsParameters {
        @DataPoints public static final String[] badStrings = new String[] { "bad" };
        @DataPoint public static final String badString = "also bad";
        @DataPoints("named") public static final String[] goodStrings = new String[] { "expected", "also expected" };
        @DataPoint("named") public static final String goodString = "expected single value";

        @DataPoints("named") public static String[] methodStrings() {
            return new String[] { "expected method value" };
        }

        @DataPoint("named") public static String methodString() {
            return "expected single method string";
        }

        @DataPoints public static String[] otherMethod() {
            return new String[] { "other method value" };
        }

        @DataPoint public static String otherSingleValueMethod() {
            return "other single value string";
        }

        @Theory public void theory(@FromDataPoints("named") String param) {
        }
    }

    @Test public void onlyUseSpecificDataPointsIfSpecified() throws Throwable {
        List<PotentialAssignment> assignments =
                potentialAssignments(HasSpecificDatapointsParameters.class.getMethod("theory", String.class));

        assertEquals(5, assignments.size());
        for (PotentialAssignment assignment : assignments) {
            assertThat((String) assignment.getValue(), containsString("expected"));
        }
    }
}
