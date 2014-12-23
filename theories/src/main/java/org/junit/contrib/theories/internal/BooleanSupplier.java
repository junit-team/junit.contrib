package org.junit.contrib.theories.internal;

import java.util.Arrays;
import java.util.List;

import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.ParameterSupplier;
import org.junit.contrib.theories.PotentialAssignment;

public class BooleanSupplier extends ParameterSupplier {
    @Override public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
        return Arrays.asList(
                PotentialAssignment.forValue("true", true),
                PotentialAssignment.forValue("false", false));
    }
}
