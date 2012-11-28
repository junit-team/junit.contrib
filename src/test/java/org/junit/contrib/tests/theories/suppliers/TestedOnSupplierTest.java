package org.junit.contrib.tests.theories.suppliers;

import com.thoughtworks.paranamer.Paranamer;
import org.junit.Test;
import org.junit.contrib.tests.theories.FakeParanamer;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.suppliers.TestedOn;
import org.junit.contrib.theories.suppliers.TestedOnSupplier;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TestedOnSupplierTest {
    public void foo(@TestedOn(ints = { 1 }) int x) {
    }

    @Test
    public void descriptionStatesParameterName() throws Exception {
        TestedOnSupplier supplier = new TestedOnSupplier();

        List<PotentialAssignment> assignments = supplier.getValueSources(signatureOfFoo());

        assertThat(assignments.get(0).getDescription(), is("ints"));
    }

    private ParameterSignature signatureOfFoo() throws Exception {
        Method method = getClass().getMethod("foo", int.class);
        Paranamer paranamer = new FakeParanamer("x");
        return ParameterSignature.signatures(method, paranamer).get(0);
    }
}
