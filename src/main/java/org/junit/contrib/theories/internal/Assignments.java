package org.junit.contrib.theories.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.ParameterSupplier;
import org.junit.contrib.theories.ParametersSuppliedBy;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.runners.model.TestClass;

import static java.util.Collections.*;
import static org.javaruntype.type.Types.*;

/**
 * A potentially incomplete list of value assignments for a method's formal
 * parameters
 */
public class Assignments {
    private List<PotentialAssignment> fAssigned;

    private final List<ParameterSignature> fUnassigned;

    private final TestClass fClass;

    private Assignments(List<PotentialAssignment> assigned, List<ParameterSignature> unassigned, TestClass testClass) {
        fUnassigned = unassigned;
        fAssigned = assigned;
        fClass = testClass;
    }

    /**
     * Returns a new assignment list for {@code testMethod}, with no params assigned.
     */
    public static Assignments allUnassigned(Method testMethod, TestClass testClass) throws Exception {
        List<ParameterSignature> signatures = ParameterSignature.signatures(testClass.getOnlyConstructor());
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

    public Object[] getActualValues(int start, int stop) throws PotentialAssignment.CouldNotGenerateValueException {
        Object[] values = new Object[stop - start];
        for (int i = start; i < stop; i++) {
            values[i - start] = fAssigned.get(i).getValue();
        }
        return values;
    }

    public List<PotentialAssignment> potentialsForNextUnassigned() throws Throwable {
        ParameterSignature unassigned = nextUnassigned();
        List<PotentialAssignment> assignments = getSupplier(unassigned).getValueSources(unassigned);

        if (assignments.size() == 0) {
            assignments = generateAssignmentsFromTypeAlone(unassigned);
        }

        return assignments;
    }

    private List<PotentialAssignment> generateAssignmentsFromTypeAlone(ParameterSignature unassigned) {
        org.javaruntype.type.Type<?> paramType = forJavaLangReflectType(unassigned.getType());
        Class<?> klass = paramType.getRawClass();

        if (klass.isEnum()) {
            return new EnumSupplier(klass).getValueSources(unassigned);
        } else if (klass.equals(Boolean.class) || klass.equals(boolean.class)) {
            return new BooleanSupplier().getValueSources(unassigned);
        } else {
            return emptyList();
        }
    }

    private ParameterSupplier getSupplier(ParameterSignature unassigned) throws Exception {
        ParametersSuppliedBy annotation = unassigned.findDeepAnnotation(ParametersSuppliedBy.class);

        if (annotation != null) {
            return buildParameterSupplierFromClass(annotation.value());
        } else {
            return new AllMembersSupplier(fClass);
        }
    }

    private ParameterSupplier buildParameterSupplierFromClass(Class<? extends ParameterSupplier> cls) throws Exception {
        Constructor<?>[] supplierConstructors = cls.getConstructors();

        for (Constructor<?> constructor : supplierConstructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].equals(TestClass.class)) {
                return (ParameterSupplier) constructor.newInstance(fClass);
            }
        }

        return cls.newInstance();
    }

    public Object[] getConstructorArguments()
            throws PotentialAssignment.CouldNotGenerateValueException {
        return getActualValues(0, getConstructorParameterCount());
    }

    public Object[] getMethodArguments() throws PotentialAssignment.CouldNotGenerateValueException {
        return getActualValues(getConstructorParameterCount(), fAssigned.size());
    }

    public Object[] getAllArguments() throws PotentialAssignment.CouldNotGenerateValueException {
        return getActualValues(0, fAssigned.size());
    }

    private int getConstructorParameterCount() {
        List<ParameterSignature> signatures = ParameterSignature.signatures(fClass.getOnlyConstructor());
        return signatures.size();
    }

    public Object[] getArgumentStrings(boolean nullsOk) throws PotentialAssignment.CouldNotGenerateValueException {
        Object[] values = new Object[fAssigned.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = fAssigned.get(i).getDescription();
        }
        return values;
    }
}
