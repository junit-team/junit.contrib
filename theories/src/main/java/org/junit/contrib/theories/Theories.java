package org.junit.contrib.theories;

import org.junit.Assert;
import org.junit.contrib.theories.PotentialAssignment.CouldNotGenerateValueException;
import org.junit.contrib.theories.internal.Assignments;
import org.junit.contrib.theories.internal.ParameterizedAssertionError;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Theories extends BlockJUnit4ClassRunner {
    public Theories(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        validateDataPointFields(errors);
    }

    private void validateDataPointFields(List<Throwable> errors) {
        Field[] fields = getTestClass().getJavaClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getAnnotation(DataPoint.class) == null) {
                continue;
            }
            if (!Modifier.isStatic(field.getModifiers())) {
                errors.add(new Error("DataPoint field " + field.getName() + " must be static"));
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                errors.add(new Error("DataPoint field " + field.getName() + " must be public"));
            }
        }
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        for (FrameworkMethod each : computeTestMethods()) {
            if (each.getAnnotation(Theory.class) != null) {
                each.validatePublicVoid(false, errors);
                each.validateNoTypeParametersOnArgs(errors);
            } else {
                each.validatePublicVoidNoArg(false, errors);
            }
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> testMethods = super.computeTestMethods();
        List<FrameworkMethod> theoryMethods = getTestClass().getAnnotatedMethods(Theory.class);
        testMethods.removeAll(theoryMethods);
        testMethods.addAll(theoryMethods);
        return testMethods;
    }

    @Override
    public Statement methodBlock(FrameworkMethod method) {
        return new TheoryAnchor(method, getTestClass());
    }

    public static class TheoryAnchor extends Statement {
        private final FrameworkMethod fTestMethod;
        private final TestClass fTestClass;
        private final List<AssumptionViolatedException> fInvalidParameters =
                new ArrayList<AssumptionViolatedException>();
        private int fSuccesses = 0;

        public TheoryAnchor(FrameworkMethod method, TestClass testClass) {
            fTestMethod = method;
            fTestClass = testClass;
        }

        private TestClass getTestClass() {
            return fTestClass;
        }

        @Override
        public void evaluate() throws Throwable {
            runWithAssignment(Assignments.allUnassigned(fTestMethod.getMethod(), getTestClass()));

            if (fSuccesses == 0) {
                Assert.fail("Never found parameters that satisfied method assumptions."
                        + "  Violated assumptions: " + fInvalidParameters);
            }
        }

        protected void runWithAssignment(Assignments parameterAssignment) throws Throwable {
            if (!parameterAssignment.isComplete()) {
                runWithIncompleteAssignment(parameterAssignment);
            } else {
                runWithCompleteAssignment(parameterAssignment);
            }
        }

        protected void runWithIncompleteAssignment(Assignments incomplete) throws Throwable {
            for (PotentialAssignment source : incomplete.potentialsForNextUnassigned()) {
                runWithAssignment(incomplete.assignNext(source));
            }
        }

        protected void runWithCompleteAssignment(final Assignments complete) throws Throwable {
            statementForCompleteAssignment(complete).evaluate();
        }

        private Statement statementForCompleteAssignment(final Assignments complete)
                throws InitializationError {
            return new BlockJUnit4ClassRunner(getTestClass().getJavaClass()) {
                @Override
                protected void collectInitializationErrors(List<Throwable> errors) {
                    // do nothing
                }

                @Override
                public Statement methodBlock(FrameworkMethod method) {
                    final Statement statement = super.methodBlock(method);

                    return new Statement() {
                        @Override
                        public void evaluate() throws Throwable {
                            try {
                                statement.evaluate();
                                handleDataPointSuccess();
                            } catch (AssumptionViolatedException e) {
                                handleAssumptionViolation(e);
                            } catch (Throwable e) {
                                reportParameterizedError(e, complete.getArgumentStrings(nullsOk()));
                            }
                        }
                    };
                }

                @Override
                protected Statement methodInvoker(FrameworkMethod method, Object test) {
                    return methodWithCompleteParameters(method, complete, test);
                }

                @Override
                public Object createTest() throws Exception {
                    return getTestClass().getOnlyConstructor().newInstance(
                            complete.getConstructorArguments(nullsOk()));
                }
            }.methodBlock(fTestMethod);
        }

        private Statement methodWithCompleteParameters(final FrameworkMethod method,
                final Assignments complete, final Object freshInstance) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        final Object[] values = complete.getMethodArguments(nullsOk());
                        method.invokeExplosively(freshInstance, values);
                    } catch (CouldNotGenerateValueException e) {
                        // ignore
                    }
                }
            };
        }

        protected void handleAssumptionViolation(AssumptionViolatedException e) {
            fInvalidParameters.add(e);
        }

        protected void reportParameterizedError(Throwable e, Object... params) throws Throwable {
            if (params.length == 0) {
                throw e;
            }
            throw new ParameterizedAssertionError(e, fTestMethod.getName(), params);
        }

        private boolean nullsOk() {
            Theory annotation = fTestMethod.getMethod().getAnnotation(Theory.class);
            return annotation != null && annotation.nullsAccepted();
        }

        protected void handleDataPointSuccess() {
            fSuccesses++;
        }
    }
}
