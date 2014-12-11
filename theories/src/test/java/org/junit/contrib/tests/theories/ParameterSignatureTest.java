package org.junit.contrib.tests.theories;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.contrib.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

@RunWith(Theories.class)
public class ParameterSignatureTest {
    @DataPoint public static Method getType() throws SecurityException, NoSuchMethodException {
        return ParameterSignatureTest.class.getMethod("getType", Method.class,
                int.class);
    }

    @DataPoint public static int ZERO = 0;
    @DataPoint public static int ONE = 1;

    @Theory public void getType(Method method, int index) {
        assumeTrue(index < method.getParameterTypes().length);
        assertEquals(method.getParameterTypes()[index], ParameterSignature.signatures(method).get(index).getType());
    }

    public void foo(@TestedOn(ints = {1, 2, 3}) int x) {
    }

    @Test public void getAnnotations() throws SecurityException, NoSuchMethodException {
        Method method = getClass().getMethod("foo", int.class);

        List<Annotation> annotations = ParameterSignature.signatures(method).get(0).getAnnotations();

        assertThat(annotations, CoreMatchers.<TestedOn> hasItem(isA(TestedOn.class)));
    }

    public void intMethod(int param) {
    }

    public void integerMethod(Integer param) {
    }

    public void numberMethod(Number param) {
    }

    @Test public void primitiveTypesShouldBeAcceptedAsWrapperTypes() throws Exception {
        List<ParameterSignature> signatures =
                ParameterSignature.signatures(getClass().getMethod("integerMethod", Integer.class));
        ParameterSignature integerSignature = signatures.get(0);

        assertTrue(integerSignature.canAcceptType(int.class));
    }

    @Test public void primitiveTypesShouldBeAcceptedAsWrapperTypeAssignables() throws Exception {
        List<ParameterSignature> signatures =
                ParameterSignature.signatures(getClass().getMethod("numberMethod", Number.class));
        ParameterSignature numberSignature = signatures.get(0);

        assertTrue(numberSignature.canAcceptType(int.class));
    }

    @Test public void wrapperTypesShouldBeAcceptedAsPrimitiveTypes() throws Exception {
        List<ParameterSignature> signatures =
                ParameterSignature.signatures(getClass().getMethod("intMethod", int.class));
        ParameterSignature intSignature = signatures.get(0);

        assertTrue(intSignature.canAcceptType(Integer.class));
    }
}
