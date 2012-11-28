package org.junit.contrib.tests.theories;

import com.thoughtworks.paranamer.Paranamer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class FakeParanamer implements Paranamer {
    private final String[] names;

    public FakeParanamer(String... names) {
        this.names = names.clone();
    }

    public FakeParanamer(Method method) {
        Class<?>[] types = method.getParameterTypes();
        names = new String[types.length];
        for (int i = 0; i < types.length; ++i)
            names[i] = "arg" + i;
    }

    public String[] lookupParameterNames(AccessibleObject accessibleObject) {
        return names.clone();
    }

    public String[] lookupParameterNames(AccessibleObject accessibleObject, boolean b) {
        return names.clone();
    }
}
