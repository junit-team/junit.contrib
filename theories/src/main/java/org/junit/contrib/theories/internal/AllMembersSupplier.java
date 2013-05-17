package org.junit.contrib.theories.internal;

import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.ParameterSupplier;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Supplies Theory parameters based on all public members of the target class.
 */
public class AllMembersSupplier extends ParameterSupplier {
    static class MethodParameterValue extends PotentialAssignment {
        private final FrameworkMethod fMethod;

        private MethodParameterValue(FrameworkMethod dataPointMethod) {
            fMethod = dataPointMethod;
        }

        @Override
        public Object getValue() throws CouldNotGenerateValueException {
            try {
                return fMethod.invokeExplosively(null);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("unexpected: argument length is checked");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("unexpected: getMethods returned an inaccessible method");
            } catch (Throwable e) {
                throw new CouldNotGenerateValueException();
                // do nothing, just look for more values
            }
        }

        @Override
        public String getDescription() throws CouldNotGenerateValueException {
            return fMethod.getName();
        }
    }

    private final TestClass fClass;

    /**
     * Constructs a new supplier for {@code type}
     */
    public AllMembersSupplier(TestClass type) {
        fClass = type;
    }

    @Override
    public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
        List<PotentialAssignment> list = new ArrayList<PotentialAssignment>();

        addFields(sig, list);
        addSinglePointMethods(sig, list);
        addMultiPointMethods(sig, list);

        return list;
    }

    private void addMultiPointMethods(ParameterSignature sig, List<PotentialAssignment> list) {
        for (FrameworkMethod dataPointsMethod : fClass.getAnnotatedMethods(DataPoints.class)) {
            try {
                addMultiPointArrayValues(sig, dataPointsMethod.getName(), list,
                        dataPointsMethod.invokeExplosively(null));
            } catch (Throwable e) {
                // ignore and move on
            }
        }
    }

    private void addSinglePointMethods(ParameterSignature sig, List<PotentialAssignment> list) {
        for (FrameworkMethod dataPointMethod : fClass.getAnnotatedMethods(DataPoint.class)) {
            if (sig.canAcceptResultOf(dataPointMethod)) {
                list.add(new MethodParameterValue(dataPointMethod));
            }
        }
    }

    private void addFields(ParameterSignature sig, List<PotentialAssignment> list) {
        for (Field field : fClass.getJavaClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                Type type = field.getGenericType();
                if (sig.canAcceptArrayType(type) && field.getAnnotation(DataPoints.class) != null) {
                    try {
                        addArrayValues(field.getName(), list, getStaticFieldValue(field));
                    } catch (Throwable e) {
                        // ignore and move on
                    }
                } else if (sig.canAcceptType(type)
                        && field.getAnnotation(DataPoint.class) != null) {
                    list.add(PotentialAssignment.forValue(field.getName(),
                            getStaticFieldValue(field)));
                }
            }
        }
    }

    private void addArrayValues(String name, List<PotentialAssignment> list, Object array) {
        for (int i = 0; i < Array.getLength(array); i++) {
            list.add(potentialAssignmentForArrayElement(name, array, i));
        }
    }

    private void addMultiPointArrayValues(ParameterSignature sig, String name,
            List<PotentialAssignment> list, Object array) throws Throwable {
        for (int i = 0; i < Array.getLength(array); i++) {
            if (!sig.canAcceptValue(Array.get(array, i))) {
                return;
            }
            list.add(potentialAssignmentForArrayElement(name, array, i));
        }
    }

    private PotentialAssignment potentialAssignmentForArrayElement(String name, Object array,
            int index) {
        return PotentialAssignment.forValue(name + '[' + index + ']', Array.get(array, index));
    }

    private Object getStaticFieldValue(final Field field) {
        try {
            return field.get(null);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("unexpected: field from getClass doesn't exist on object");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("unexpected: getFields returned an inaccessible field");
        }
    }
}
