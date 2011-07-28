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
package org.junit.contrib.assertthrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.contrib.assertthrows.proxy.ProxyFactory;
import org.junit.contrib.assertthrows.proxy.ReflectionUtils;
import org.junit.contrib.assertthrows.verify.ExceptionVerifier;
import org.junit.contrib.assertthrows.verify.ResultVerifier;

/**
 * A facility to test for exceptions.
 *
 * @author Thomas Mueller
 */
public abstract class AssertThrows {

    private final ResultVerifier verifier;

    /**
     * Create a new AssertThrows object, and call the test method to verify the
     * expected exception is thrown.
     *
     * @param expectedExceptionClass the expected exception class
     */
    public AssertThrows(Class<? extends Exception> expectedExceptionClass) {
        this(new ExceptionVerifier(expectedExceptionClass, null));
    }

    /**
     * Create a new AssertThrows object, and call the test method to verify the
     * expected exception is thrown.
     *
     * @param expectedException the expected exception
     */
    public AssertThrows(Exception expectedException) {
        this(expectedException == null ?
                new ExceptionVerifier(null, null) :
                new ExceptionVerifier(
                        expectedException.getClass(),
                        expectedException.getMessage()));
    }

    /**
     * Create a new AssertThrows object, and call the test method to verify an
     * exception or error is thrown. That means for a successful result, the
     * test() method must throw any kind of Exception or Error (AssertionError,
     * StackOverflowError, and so on).
     */
    public AssertThrows() {
        this(new ExceptionVerifier(null, null));
    }

    /**
     * Create a new AssertThrows object, and call the test method as many times
     * as the result verifier requests. It is usually not required to use this
     * constructor, except to extend this facility to do something new, such as
     * repeat a test until it works.
     */
    protected AssertThrows(ResultVerifier verifier) {
        this.verifier = verifier;
        verify();
    }

    /**
     * Run the <code>test()</code> method and verify it throws an exception.
     * This method is called by the constructor.
     */
    protected void verify() {
        while (true) {
            try {
                test();
                // can't call verifier.verify here, because it can
                // throw an exception itself (which must not  be caught)
            } catch (Throwable e) {
                if (verifier.verify(null, e, null)) {
                    continue;
                }
                break;
            }
            if (verifier.verify(null, null, null)) {
                continue;
            }
            break;
        }
    }

    /**
     * Verify that the next method call on the object throws an exception.
     *
     * @param <T> the class of the object
     * @param obj the object to wrap
     * @return a proxy for the object
     */
    public static <T> T assertThrows(T obj) {
        return createVerifyingProxy(new ExceptionVerifier(null, null), obj);
    }

    /**
     * Verify that the next method call on the object throws the expected
     * exception.
     *
     * @param <T> the class of the object
     * @param expectedExceptionClass the expected exception class to be thrown
     * @param obj the object to wrap
     * @return a proxy for the object
     */
    public static <T> T assertThrows(Class<? extends Exception> expectedExceptionClass, T obj) {
        return createVerifyingProxy(new ExceptionVerifier(expectedExceptionClass, null), obj);
    }

    /**
     * Verify that the next method call on the object throws the expected
     * exception.
     *
     * @param <T> the class of the object
     * @param expectedException the expected exception
     * @param obj the object to wrap
     * @return a proxy for the object
     */
    public static <T> T assertThrows(Exception expectedException, T obj) {
        return createVerifyingProxy(expectedException == null ?
                new ExceptionVerifier(null, null) :
                new ExceptionVerifier(expectedException.getClass(), expectedException.getMessage()),
                obj);
    }

    /**
     * Test using a class proxy for this class from now on, even if the class
     * does implement an interface. This allows to test classes that don't
     * implement an interface, and methods that are not part of any interface.
     *
     * @param c the class
     */
    public static void useClassProxy(Class<?> c) {
        ProxyFactory.useClassProxyFactory(c);
    }

    /**
     * Create a verifying proxy for the given object. It is usually not required
     * to use this method in a test directly, except to extend this facility to
     * do something new, such as repeat a test until it works.
     *
     * @param <T> the class of the object
     * @param verifier the result verifier to call after each method call
     * @param obj the object to wrap
     * @return a proxy for the object
     * @throws IllegalArgumentException if it was not possible to create a proxy
     *             for the passed object
     */
    protected static <T> T createVerifyingProxy(final ResultVerifier verifier, final T obj) {
        InvocationHandler handler = new InvocationHandler() {
            private Exception called = new Exception("No method was called on " + obj);
            public void finalize() {
                if (called != null) {
                    called.printStackTrace(System.err);
                }
            }
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                try {
                    called = null;
                    Object ret;
                    do {
                        ret = method.invoke(obj, args);
                    } while (verifier.verify(ret, null, method, args));
                    return ret;
                } catch (InvocationTargetException e) {
                    verifier.verify(null, e.getTargetException(), method, args);
                    return ReflectionUtils.getDefaultValue(method.getReturnType());
                }
            }
        };
        ProxyFactory factory = ProxyFactory.getFactory(obj.getClass());
        return factory.createProxy(obj, handler);
    }

    /**
     * The test method that is called.
     *
     * @throws Exception the expected exception
     */
    public abstract void test() throws Exception;

}
