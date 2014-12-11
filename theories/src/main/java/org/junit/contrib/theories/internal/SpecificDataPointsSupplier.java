package org.junit.contrib.theories.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.FromDataPoints;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class SpecificDataPointsSupplier extends AllMembersSupplier {
    public SpecificDataPointsSupplier(TestClass testClass) {
        super(testClass);
    }

    @Override protected Collection<Field> getSingleDataPointFields(ParameterSignature sig) {
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();

        List<Field> fieldsWithMatchingNames = new ArrayList<Field>();

        for (Field each : super.getSingleDataPointFields(sig)) {
            String[] fieldNames = each.getAnnotation(DataPoint.class).value();
            if (Arrays.asList(fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(each);
            }
        }

        return fieldsWithMatchingNames;
    }

    @Override protected Collection<Field> getDataPointsFields(ParameterSignature sig) {
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();

        List<Field> fieldsWithMatchingNames = new ArrayList<Field>();

        for (Field each : super.getDataPointsFields(sig)) {
            String[] fieldNames = each.getAnnotation(DataPoints.class).value();
            if (Arrays.asList(fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(each);
            }
        }

        return fieldsWithMatchingNames;
    }

    @Override protected Collection<FrameworkMethod> getSingleDataPointMethods(ParameterSignature sig) {
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();

        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();

        for (FrameworkMethod each : super.getSingleDataPointMethods(sig)) {
            String[] methodNames = each.getAnnotation(DataPoint.class).value();
            if (Arrays.asList(methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(each);
            }
        }

        return methodsWithMatchingNames;
    }

    @Override protected Collection<FrameworkMethod> getDataPointsMethods(ParameterSignature sig) {
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();

        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();

        for (FrameworkMethod each : super.getDataPointsMethods(sig)) {
            String[] methodNames = each.getAnnotation(DataPoints.class).value();
            if (Arrays.asList(methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(each);
            }
        }

        return methodsWithMatchingNames;
    }
}
