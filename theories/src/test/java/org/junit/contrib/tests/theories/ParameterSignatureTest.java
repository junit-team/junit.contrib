package org.junit.contrib.tests.theories;

import com.thoughtworks.paranamer.Paranamer;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.contrib.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

@RunWith(Theories.class)
public class ParameterSignatureTest {
    @DataPoint
    public static Method getType() throws Exception {
        return ParameterSignatureTest.class.getMethod("getType", Method.class, int.class);
    }

    @DataPoint
    public static int ZERO = 0;

    @DataPoint
    public static int ONE = 1;

    @Theory
    public void getType(Method method, int index) {
        assumeTrue(index < method.getParameterTypes().length);

        Paranamer paranamer = new FakeParanamer("method", "index");

        assertEquals(method.getParameterTypes()[index],
                ParameterSignature.signatures(method, paranamer).get(index).getType());
    }

    public void foo(@TestedOn(ints = { 1, 2, 3 }) int x) {
    }

    @Test
    public void getAnnotations() throws Exception {
        Method method = ParameterSignatureTest.class.getMethod("foo", int.class);
        Paranamer paranamer = new FakeParanamer("x");

        List<Annotation> annotations = ParameterSignature.signatures(method, paranamer).get(0).getAnnotations();

        assertThat(annotations, CoreMatchers.<TestedOn> hasItem(isA(TestedOn.class)));
    }
}
