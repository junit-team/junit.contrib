package org.junit.contrib.theories.internal;

import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.ParameterSupplier;
import org.junit.contrib.theories.ParametersSuppliedBy;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.PotentialAssignment.CouldNotGenerateValueException;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A potentially incomplete list of value assignments for a method's formal parameters.</p>
 *
 * <p>Instances are intended to be immutable.</p>
 */
public class Assignments {
    private final List<PotentialAssignment> fAssigned;
    private final List<ParameterSignature> fUnassigned;
    private final TestClass fClass;

    private Assignments(List<PotentialAssignment> assigned, List<ParameterSignature> unassigned,
            TestClass testClass) {
        fUnassigned = unassigned;
        fAssigned = assigned;
        fClass = testClass;
    }

    /**
     * Returns a new assignment list for {@code testMethod}, with no params assigned.
     */
    public static Assignments allUnassigned(Method testMethod, TestClass testClass)
            throws Exception {
        List<ParameterSignature> signatures =
                ParameterSignature.signatures(testClass.getOnlyConstructor());
        signatures.addAll(ParameterSignature.signatures(testMethod));
        return new Assignments(new ArrayList<PotentialAssignment>(), signatures, testClass);
    }

    public boolean isComplete() {
        return fUnassigned.size() == 0;
    }

    public ParameterSignature nextUnassigned() {
        return fUnassigned.get(0);
    }

    public Assignments assignNext(PotentialAssignment source) {
        List<PotentialAssignment> assigned = new ArrayList<PotentialAssignment>(fAssigned);
        assigned.add(source);

        return new Assignments(assigned, fUnassigned.subList(1, fUnassigned.size()), fClass);
    }

    public Object[] getActualValues(int start, int stop, boolean nullsOk)
            throws CouldNotGenerateValueException {
        Object[] values = new Object[stop - start];
        for (int i = start; i < stop; i++) {
            Object value = fAssigned.get(i).getValue();
            if (value == null && !nullsOk) {
                throw new CouldNotGenerateValueException();
            }
            values[i - start] = value;
        }
        return values;
    }

    public List<PotentialAssignment> potentialsForNextUnassigned()
            throws InstantiationException, IllegalAccessException {

        ParameterSignature unassigned = nextUnassigned();
        return getSupplier(unassigned).getValueSources(unassigned);
    }

    public ParameterSupplier getSupplier(ParameterSignature unassigned)
            throws InstantiationException, IllegalAccessException {

        ParameterSupplier supplier = getAnnotatedSupplier(unassigned);
        if (supplier != null) {
            return supplier;
        }

        return new AllMembersSupplier(fClass);
    }

    public ParameterSupplier getAnnotatedSupplier(ParameterSignature unassigned)
            throws InstantiationException, IllegalAccessException {

        ParametersSuppliedBy annotation = unassigned.findDeepAnnotation(ParametersSuppliedBy.class);
        if (annotation == null) {
            return null;
        }
        return annotation.value().newInstance();
    }

    public Object[] getConstructorArguments(boolean nullsOk) throws CouldNotGenerateValueException {
        return getActualValues(0, getConstructorParameterCount(), nullsOk);
    }

    public Object[] getMethodArguments(boolean nullsOk) throws CouldNotGenerateValueException {
        return getActualValues(getConstructorParameterCount(), fAssigned.size(), nullsOk);
    }

    private int getConstructorParameterCount() {
        List<ParameterSignature> signatures =
                ParameterSignature.signatures(fClass.getOnlyConstructor());
        return signatures.size();
    }

    public Object[] getArgumentStrings(boolean nullsOk) throws CouldNotGenerateValueException {
        Object[] values = new Object[fAssigned.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = fAssigned.get(i).getDescription();
        }
        return values;
    }
}
