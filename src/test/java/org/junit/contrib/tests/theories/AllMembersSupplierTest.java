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
        @DataPoints public static final Object[] objects = { 1, 2 };

        public HasDataPoints(Object obj) {
        }
    }

    @Test
    public void dataPointsAnnotationMeansTreatAsArrayOnly() throws Exception {
        List<ParameterSignature> signatures =
            ParameterSignature.signatures(HasDataPoints.class.getConstructor(Object.class));

        List<PotentialAssignment> valueSources =
            new AllMembersSupplier(new TestClass(HasDataPoints.class)).getValueSources(signatures.get(0));

        assertThat(valueSources.size(), is(2));
    }
}
