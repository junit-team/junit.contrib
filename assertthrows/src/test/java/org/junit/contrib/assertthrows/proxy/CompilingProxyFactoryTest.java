/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.junit.contrib.assertthrows.proxy;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import org.junit.Test;
import org.junit.contrib.assertthrows.AssertThrows;

/**
 * Test creating class proxies using the compiling proxy factory.
 *
 * @author Thomas Mueller
 */
public class CompilingProxyFactoryTest {

    StringBuilder buff = new StringBuilder();
    int methodCallCount;

    @Test
    public void testObject() {
        createProxy(new Object()).equals(null);
        assertEquals("equals = false", buff.toString());
    }

    @Test
    public void testJavaUtilRandom() {
        createProxy(new Random()).nextInt(1);
        assertEquals("nextInt = 0", buff.toString());
    }

    @Test
    public void testPublicStaticInnerClass() {
        createProxy(new PublicStaticInnerClass()).toString();
        assertEquals("toString = PublicStaticInnerClass", buff.toString());
    }

    @Test
    public void testProxyOnProxy() {
        final PublicStaticInnerClass x = createProxy(new PublicStaticInnerClass());
        new AssertThrows() { public void test() {
            createProxy(x);
        }};
    }

    @Test
    public void testAnonymousClass() {
        new AssertThrows(new IllegalArgumentException(
                "Creating a proxy for an anonymous inner class is not supported: " +
                "org.junit.contrib.assertthrows.proxy.CompilingProxyFactoryTest$" +
                "2$1")) {
            public void test() {
                createProxy(new Object() {
                    // anonymous
                });
        }};
    }


    @Test
    public void testPublicInnerClass() {
        // in theory, it would be possible to support it,
        // but the outer class would also need to be extended
        new AssertThrows(new IllegalArgumentException(
                "Creating a proxy for a non-static inner class is not supported: " +
                "org.junit.contrib.assertthrows.proxy.CompilingProxyFactoryTest$" +
                "PublicInnerClass")) {
            public void test() {
                createProxy(new PublicInnerClass());
        }};
    }

    @Test
    public void testNonPublicStaticInnerClass() {
        new AssertThrows(new IllegalArgumentException(
                "Creating a proxy for a non-public inner class is not supported: " +
                "org.junit.contrib.assertthrows.proxy.CompilingProxyFactoryTest$" +
                "StaticInnerClass")) {
            public void test() {
                createProxy(new StaticInnerClass());
        }};
    }

    @Test
    public void testPrivateConstructor() {
        new AssertThrows(new IllegalArgumentException(
                "Creating a proxy for a static inner class " +
                "without public constructor is not supported: " +
                "org.junit.contrib.assertthrows.proxy.CompilingProxyFactoryTest$" +
                "PrivateConstructor")) {
            public void test() {
                createProxy(PrivateConstructor.getInstance(10));
        }};
    }

    @Test
    public void testNonDefaultConstructor() {
        new AssertThrows(new IllegalArgumentException(
                "Could not create a new instance of the class " +
                "proxy.org.junit.contrib.assertthrows.proxy.NonDefaultConstructorProxy")) {
            public void test() {
                createProxy(new NonDefaultConstructor(10));
        }};
    }

    @Test
    public void testFinalClass() {
        new AssertThrows(new IllegalArgumentException(
                "Creating a proxy for a final class is not supported: " +
                "org.junit.contrib.assertthrows.proxy.CompilingProxyFactoryTest$FinalClass")) {
            public void test() {
                createProxy(new FinalClass());
        }};
    }

    @Test
    public void testClassWithBridgeMethod() throws Exception {
        createProxy(new ClassWithBridgeMethod()).clone();
        assertEquals("clone = ClassWithBridgeMethod", buff.toString());
        // even bridge methods should be intercepted (there are two clone methods)
        callCloneMethods(0, new ClassWithBridgeMethod());
        callCloneMethods(1, createProxy(new ClassWithBridgeMethod()));
    }

    @Test
    public void testVarArgs() {
        createProxy(new ClassWithVarArgsMethod()).sum(1.2, 3.0);
        assertEquals("sum = 4.2", buff.toString());
    }

    @Test
    public void testSpecialFields() {
        createProxy(new ClassWithSpecialFields(1, 2, 3)).toString(4, 5);
        assertEquals("toString = 1 2 3 4 5", buff.toString());
    }

    @Test
    public void testConstructorWithPrimitiveArguments() {
        ClassWithPrimitiveConstructorArguments x;
        x = new ClassWithPrimitiveConstructorArguments(false, (byte) 0, (char) 0, (short) 0,
                0, 0, 0.0f, 0.0d, null);
        createProxy(x).toString();
        assertEquals("toString = ClassWithPrimitiveConstructorArguments", buff.toString());
    }

    @Test
    public void testMultipleConstructors() {
        ClassWithMultipleConstructors x = new ClassWithMultipleConstructors(1, 2);
        createProxy(x).toString();
        assertEquals("toString = ClassWithMultipleConstructors", buff.toString());
    }

    private void callCloneMethods(
            int expectedCallCount,
            ClassWithBridgeMethod obj) throws Exception {
        int methodCount = 0;
        for (Method m : obj.getClass().getDeclaredMethods()) {
            if (m.toString().indexOf("clone") >= 0) {
                methodCount++;
                methodCallCount = 0;
                m.invoke(obj);
                assertEquals(expectedCallCount, methodCallCount);
            }
        }
        assertEquals(2, methodCount);
    }

    <T> T createProxy(final T obj) {
        return CompilingProxyFactory.getInstance().createProxy(obj, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                buff.append(method.getName());
                methodCallCount++;
                try {
                    Object o = method.invoke(obj, args);
                    buff.append(" = ").append(o);
                    return o;
                } catch (InvocationTargetException e) {
                    buff.append(" threw ").append(e.getTargetException());
                    throw e;
                }
            }
        });
    }

    /**
     * A simple public static inner class.
     */
    public static class PublicStaticInnerClass {
        public String toString() {
            return "PublicStaticInnerClass";
        }
    }

    /**
     * A public inner class.
     */
    public class PublicInnerClass {
        public String toString() {
            return "PublicInnerClass";
        }
    }

    /**
     * A non-public static inner class.
     */
    static class StaticInnerClass {
        public String toString() {
            return "StaticInnerClass";
        }
    }

    /**
     * An inner class with a tricky private constructor.
     */
    public static class PrivateConstructor {
        private PrivateConstructor(int x) {
            if (x != 10) {
                throw new IllegalArgumentException();
            }
        }
        static PrivateConstructor getInstance(int x) {
            return new PrivateConstructor(x);
        }
        public String toString() {
            return "PrivateConstructor";
        }
    }

    /**
     * An inner class with a tricky private constructor.
     */
    public static class NonDefaultConstructor {
        public NonDefaultConstructor(int x) {
            if (x != 10) {
                throw new IllegalArgumentException();
            }
        }
        public String toString() {
            return "PrivateConstructor";
        }
    }

    /**
     * A final class.
     */
    public static final class FinalClass {
        public String toString() {
            return "FinalClass";
        }
    }

    /**
     * A class with a bridge method.
     */
    public static class ClassWithBridgeMethod {
        public ClassWithBridgeMethod clone() {
            try {
                return (ClassWithBridgeMethod) super.clone();
            } catch (Exception e) {
                return this;
            }
        }
        public String toString() {
            return "ClassWithBridgeMethod";
        }
    }

    /**
     * A class with a var-args method.
     */
    public static class ClassWithVarArgsMethod {
        public double sum(double... args) {
            double s = 0.0;
            for (double x : args) {
                s += x;
            }
            return s;
        }
    }

    /**
     * A class with specially named fields (that conflict with the default field
     * names used in the compiling class proxy).
     */
    public static class ClassWithSpecialFields {
        public int ih, ih0, ih1;
        public ClassWithSpecialFields(int a, int b, int c) {
            ih = a;
            ih0 = b;
            ih1 = c;
        }
        public String toString(int d, int e) {
            return ih + " " + ih0 + " " + ih1 + " " + d + " " + e;
        }
    }

    /**
     * A class that uses primitive constructor arguments.
     */
    public static class ClassWithPrimitiveConstructorArguments {
        public ClassWithPrimitiveConstructorArguments(boolean a, byte b, char c,
                short d, int e, long f, float g, double h, String i) {
            if (a || b != 0 || c != 0 || d != 0 || e != 0 || f != 0 ||
                    g != 0 || h != 0 || i != null) {
                throw new IllegalArgumentException();
            }
        }
        public String toString() {
            return "ClassWithPrimitiveConstructorArguments";
        }
    }

    /**
     * A class with multiple constructors.
     */
    public static class ClassWithMultipleConstructors {
        private ClassWithMultipleConstructors(int a, int b, int c) {
            if (a != 1 || b != 2 || c != 3) {
                throw new IllegalArgumentException();
            }
        }
        public ClassWithMultipleConstructors(int a, int b) {
            if (a != 1 || b != 2) {
                throw new IllegalArgumentException();
            }
        }
        protected ClassWithMultipleConstructors(int a) {
            // ok
        }
        public String toString() {
            new ClassWithMultipleConstructors(1, 2, 3);
            new ClassWithMultipleConstructors(1, 2);
            return "ClassWithMultipleConstructors";
        }
    }

}
