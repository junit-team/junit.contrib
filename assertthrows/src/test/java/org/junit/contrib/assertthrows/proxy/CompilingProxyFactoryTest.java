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
    public void testAnonymousClass() {
        new AssertThrows(new IllegalArgumentException(
                "Creating a proxy for an anonymous inner class is not supported: " +
                "org.junit.contrib.assertthrows.proxy.CompilingProxyFactoryTest$" +
                "1$1")) {
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
                Object o = method.invoke(obj, args);
                buff.append(" = ").append(o);
                methodCallCount++;
                return o;
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

}
