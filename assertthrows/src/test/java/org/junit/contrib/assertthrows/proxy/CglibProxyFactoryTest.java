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
 * Test creating class proxies using the cglib proxy factory.
 *
 * @author Thomas Mueller
 */
public class CglibProxyFactoryTest {

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
    public void testStaticInnerClass() {
        createProxy(new StaticInnerClass()).toString();
        assertEquals("toString = StaticInnerClass", buff.toString());
    }

    @Test
    public void testPrivateInnerClass() {
        createProxy(new PrivateInnerClass()).toString();
        assertEquals("toString = PrivateInnerClass", buff.toString());
    }

    @Test
    public void testPrivateConstructor() {
        createProxy(PrivateConstructor.getInstance(10)).toString();
        assertEquals("toString = PrivateConstructor", buff.toString());
    }

    @Test
    public void testPrivateConstructorUsingReflection() {
        new AssertThrows(new IllegalArgumentException(
                "Could not create a new proxy instance for the base class " +
                PrivateConstructor.class.getName() +
                " (probably because objenesis is not used)")) {
            public void test() {
                createProxy(PrivateConstructor.getInstance(10), true).toString();
        }};
    }

    @Test
    public void testAnonymousClass() {
        createProxy(new Object() {
            public String toString() {
                return "Anonymous";
            }}).toString();
        assertEquals("toString = Anonymous", buff.toString());
    }

    @Test
    public void testFinalClass() {
        new AssertThrows(new IllegalArgumentException(
                "Can not create a proxy for the class " +
                "org.junit.contrib.assertthrows.proxy.CglibProxyFactoryTest$FinalClass")) {
            public void test() {
                createProxy(new FinalClass());
        }};
    }

    @Test
    public void testClassWithBrigeMethod() throws Exception {
        createProxy(new ClassWithBridgeMethod()).clone();
        assertEquals("clone = ClassWithBridgeMethod", buff.toString());
        // even brige methods should be intercepted (there are two clone methods)
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

    <T> T createProxy(T obj) {
        return createProxy(obj, false);
    }

    <T> T createProxy(final T obj, boolean forceUsingReflection) {
        return CglibProxyFactory.getInstance().createProxy(obj, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                buff.append(method.getName());
                Object o = method.invoke(obj, args);
                buff.append(" = ").append(o);
                methodCallCount++;
                return o;
            }
        }, forceUsingReflection);
    }

    /**
     * A simple static inner class.
     */
    static class StaticInnerClass {

        public String toString() {
            return "StaticInnerClass";
        }
    }

    /**
     * A private inner class.
     */
    private class PrivateInnerClass {

        PrivateInnerClass() {
            // nothing to do
        }

        public String toString() {
            return "PrivateInnerClass";
        }
    }

    /**
     * An inner class with a tricky private constructor.
     */
    static class PrivateConstructor {

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
     * A final class.
     */
    static final class FinalClass {
        public String toString() {
            return "FinalClass";
        }
    }

    /**
     * A class with a bridge method.
     */
    static class ClassWithBridgeMethod {
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
