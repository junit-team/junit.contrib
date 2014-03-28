package org.junit.contrib.theories.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.javaruntype.type.Types;
import org.junit.Assume;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.ParameterSupplier;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class AllMembersSupplier extends ParameterSupplier {
    static class MethodParameterValue extends PotentialAssignment {
        private final FrameworkMethod fMethod;

        private MethodParameterValue(FrameworkMethod dataPointMethod) {
            fMethod = dataPointMethod;
        }

        @Override public Object getValue() throws CouldNotGenerateValueException {
            try {
                return fMethod.invokeExplosively(null);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("unexpected: argument length is checked");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("unexpected: getMethods returned an inaccessible method");
            } catch (Throwable throwable) {
                DataPoint annotation = fMethod.getAnnotation(DataPoint.class);
                Assume.assumeTrue(annotation == null || !isAssignableToAnyOf(annotation.ignoredExceptions(), throwable));

                throw new CouldNotGenerateValueException(throwable);
            }
        }

        @Override public String getDescription() throws CouldNotGenerateValueException {
            return fMethod.getName();
        }
    }

    private final TestClass fClass;

    public AllMembersSupplier(TestClass type) {
        fClass = type;
    }

    @Override public List<PotentialAssignment> getValueSources(ParameterSignature sig) throws Throwable {
        List<PotentialAssignment> assignments = new ArrayList<PotentialAssignment>();

        addSinglePointFields(sig, assignments);
        addMultiPointFields(sig, assignments);
        addSinglePointMethods(sig, assignments);
        addMultiPointMethods(sig, assignments);

        return assignments;
    }

    private void addMultiPointMethods(ParameterSignature sig, List<PotentialAssignment> assignments) throws Throwable {
        for (FrameworkMethod each : getDataPointsMethods(sig)) {
            org.javaruntype.type.Type<?> type = Types.forJavaLangReflectType(each.getMethod().getGenericReturnType());

            if ((type.isArray() && sig.canPotentiallyAcceptType(type.getComponentClass())) ||
                    Types.forJavaLangReflectType(Iterable.class).isAssignableFrom(type)) {
                try {
                    addDataPointsValues(type, sig, each.getName(), assignments, each.invokeExplosively(null));
                } catch (Throwable e) {
                    DataPoints annotation = each.getAnnotation(DataPoints.class);
                    if (annotation != null && isAssignableToAnyOf(annotation.ignoredExceptions(), e)) {
                        return;
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private void addSinglePointMethods(ParameterSignature sig, List<PotentialAssignment> assignments) {
        for (FrameworkMethod each : getSingleDataPointMethods(sig)) {
            if (sig.canAcceptType(each.getMethod().getGenericReturnType())) {
                assignments.add(new MethodParameterValue(each));
            }
        }
    }

    private void addMultiPointFields(ParameterSignature sig, List<PotentialAssignment> assignments) {
        for (Field each : getDataPointsFields(sig)) {
            addDataPointsValues(Types.forJavaLangReflectType(each.getGenericType()), sig, each.getName(), assignments,
                    getStaticFieldValue(each));
        }
    }

    private void addSinglePointFields(ParameterSignature sig, List<PotentialAssignment> assignments) {
        for (Field each : getSingleDataPointFields(sig)) {
            Object value = getStaticFieldValue(each);

            if (sig.canAcceptType(each.getGenericType())) {
                assignments.add(PotentialAssignment.forValue(each.getName(), value));
            }
        }
    }

    private void addDataPointsValues(org.javaruntype.type.Type<?> type, ParameterSignature sig, String name,
                                     List<PotentialAssignment> assignments, Object value) {
        if (type.isArray()) {
            addArrayValues(sig, name, assignments, value);
        } else if (Types.forJavaLangReflectType(Iterable.class).isAssignableFrom(type)) {
            addIterableValues(sig, name, assignments, (Iterable<?>) value);
        }
    }

    private void addArrayValues(ParameterSignature sig, String name, List<PotentialAssignment> assignments,
            Object array) {

        for (int i = 0, len = Array.getLength(array); i < len; i++) {
            Object value = Array.get(array, i);
            if (sig.canAcceptValue(value)) {
                assignments.add(PotentialAssignment.forValue(name + "[" + i + "]", value));
            }
        }
    }

    private void addIterableValues(ParameterSignature sig, String name, List<PotentialAssignment> assignments,
                                   Iterable<?> iterable) {
        Iterator<?> iterator = iterable.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Object value = iterator.next();
            if (sig.canAcceptValue(value)) {
                assignments.add(PotentialAssignment.forValue(name + "[" + i + "]", value));
            }
            i += 1;
        }
    }

    private Object getStaticFieldValue(Field field) {
        try {
            return field.get(null);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("unexpected: field from getClass doesn't exist on object");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("unexpected: getFields returned an inaccessible field");
        }
    }

    private static boolean isAssignableToAnyOf(Class<?>[] types, Object target) {
        for (Class<?> each : types) {
            if (each.isAssignableFrom(target.getClass())) {
                return true;
            }
        }
        return false;
    }

    protected Collection<FrameworkMethod> getDataPointsMethods(ParameterSignature sig) {
        return fClass.getAnnotatedMethods(DataPoints.class);
    }

    protected Collection<Field> getSingleDataPointFields(ParameterSignature sig) {
        Collection<Field> validFields = new ArrayList<Field>();

        for (FrameworkField each : fClass.getAnnotatedFields(DataPoint.class)) {
            validFields.add(each.getField());
        }

        return validFields;
    }

    protected Collection<Field> getDataPointsFields(ParameterSignature sig) {
        Collection<Field> validFields = new ArrayList<Field>();

        for (FrameworkField each : fClass.getAnnotatedFields(DataPoints.class)) {
            validFields.add(each.getField());
        }

        return validFields;
    }

    protected Collection<FrameworkMethod> getSingleDataPointMethods(ParameterSignature sig) {
        return fClass.getAnnotatedMethods(DataPoint.class);
    }
}
