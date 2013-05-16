package org.junit.contrib.tests.theories;

import org.junit.Test;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.internal.AllMembersSupplier;
import org.junit.runners.model.TestClass;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AllMembersSupplierTest {
    public static class HasDataPoints {
        @DataPoints
        public static Object[] objects = { 1, 2 };

        public HasDataPoints(Object obj) {
        }
    }

    @Test
    public void dataPointsAnnotationMeansTreatAsArrayOnly() throws Exception {
        List<PotentialAssignment> valueSources = new AllMembersSupplier(new TestClass(HasDataPoints.class))
                .getValueSources(ParameterSignature.signatures(
                        HasDataPoints.class.getConstructor(Object.class)).get(0));

        assertThat(valueSources.size(), is(2));
    }

    public static class HasDataPointsFieldWithNullValue {
        @DataPoints
        public static Object[] objects = { null, "a" };

        public HasDataPointsFieldWithNullValue(Object obj) {
        }
    }

    @Test
    public void dataPointsArrayFieldMayContainNullValue() throws Exception {
        List<PotentialAssignment> valueSources = new AllMembersSupplier(
                new TestClass(HasDataPointsFieldWithNullValue.class))
                    .getValueSources(ParameterSignature.signatures(
                            HasDataPointsFieldWithNullValue.class.getConstructor(Object.class))
                                    .get(0));

        assertThat(valueSources.size(), is(2));
    }

    public static class HasDataPointsMethodWithNullValue {
        @DataPoints
        public static Integer[] getObjects() {
            return new Integer[] { null, 1 };
        }

        public HasDataPointsMethodWithNullValue(Integer i) {
        }
    }

    @Test
    public void dataPointsArrayMethodMayContainNullValue() throws Exception {
        List<PotentialAssignment> valueSources = new AllMembersSupplier(
                new TestClass(HasDataPointsMethodWithNullValue.class)).getValueSources(
                    ParameterSignature.signatures(
                            HasDataPointsMethodWithNullValue.class.getConstructor(Integer.class))
                                    .get(0));

        assertThat(valueSources.size(), is(2));
    }
}
