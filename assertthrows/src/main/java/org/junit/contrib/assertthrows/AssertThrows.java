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

import org.junit.contrib.assertthrows.proxy.ProxyFactory;
import org.junit.contrib.assertthrows.verify.ExceptionVerifier;
import org.junit.contrib.assertthrows.verify.ResultVerifier;

/**
 * A facility to test for exceptions.
 *
 * @author Thomas Mueller
 */
public abstract class AssertThrows {

    private final ResultVerifier verifier;
    private Throwable lastThrown;

    /**
     * Create a new <code>AssertThrows</code> object, and call the test method
     * to verify an exception or error is thrown.
     * <p>
     * For the test to pass, the <code>test()</code> method must throw any kind
     * of <code>Exception</code> or <code>Error</code> (
     * <code>AssertionError</code>, <code>StackOverflowError</code>, and so on).
     */
    public AssertThrows() {
        this(new ExceptionVerifier());
    }

    /**
     * Create a new <code>AssertThrows</code> object, and call the test method
     * to verify the expected exception is thrown.
     * <p>
     * For the test to pass, the class of the thrown exception must match the
     * expected exception, or it must be a subclass of the expected exception.
     *
     * @param expectedExceptionClass the expected exception class (must not be
     *            null)
     */
    public AssertThrows(Class<? extends Exception> expectedExceptionClass) {
        this(new ExceptionVerifier(expectedExceptionClass));
    }

    /**
     * Create a new <code>AssertThrows</code> object, and call the test method
     * to verify the expected exception is thrown.
     * <p>
     * For the test to pass, the exception class must match exactly, and the message
     * must match exactly. If the message of the expected exception is null, then
     * the message of the thrown exception must also be null.
     *
     * @param expectedException the expected exception (must not be null)
     */
    public AssertThrows(Exception expectedException) {
        this(new ExceptionVerifier(expectedException));
    }

    /**
     * Create a new <code>AssertThrows</code> object, and call the test method
     * as many times as the result verifier requests.
     * <p>
     * This constructor is usually not required within unit tests, except to
     * extend this facility to do something new, such as repeat a test until it
     * works.
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
            lastThrown = null;
            try {
                test();
                // can't call verifier.verify here, because it can
                // throw an exception itself (which must not  be caught)
            } catch (Throwable e) {
                lastThrown = e;
            }
            if (!verifier.verify(null, lastThrown, null)) {
                return;
            }
        }
    }

    /**
     * Verify that the next method call on the returned object throws an exception.
     * <p>
     * For the test to pass, the method must throw any kind
     * of <code>Exception</code> or <code>Error</code> (
     * <code>AssertionError</code>, <code>StackOverflowError</code>, and so on).
     *
     * @param <T> the class of the object
     * @param obj the object to wrap (must not be null)
     * @return a proxy for the object
     */
    public static <T> T assertThrows(T obj) {
        return ExceptionVerifier.createVerifyingProxy(
                new ExceptionVerifier(), obj);
    }

    /**
     * Verify that the next method call on the returned object throws the expected
     * exception.
     * <p>
     * For the test to pass, the class of the thrown exception must match the
     * expected exception, or it must be a subclass of the expected exception.
     *
     * @param <T> the class of the object
     * @param expectedExceptionClass the expected exception class (must not be null)
     * @param obj the object to wrap (must not be null)
     * @return a proxy for the object
     */
    public static <T> T assertThrows(Class<? extends Exception> expectedExceptionClass, T obj) {
        return ExceptionVerifier.createVerifyingProxy(
                new ExceptionVerifier(expectedExceptionClass), obj);
    }

    /**
     * Verify that the next method call on the object throws the expected
     * exception.
     * <p>
     * For the test to pass, the exception class must match exactly, and the message
     * must match exactly. If the message of the expected exception is null, then
     * the message of the thrown exception must also be null.
     *
     * @param <T> the class of the object
     * @param expectedException the expected exception (must not be null)
     * @param obj the object to wrap (must not be null)
     * @return a proxy for the object
     */
    public static <T> T assertThrows(Exception expectedException, T obj) {
        return ExceptionVerifier.createVerifyingProxy(
                new ExceptionVerifier(expectedException), obj);
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
     * The test method that is called.
     *
     * @throws Exception the expected exception
     */
    public abstract void test() throws Exception;

    /**
     * Get the last exception or error (if any) that was thrown by the method
     * <code>test()</code>.
     *
     * @return the last thrown exception or error, or null
     */
    public Throwable getLastThrown() {
        return lastThrown;
    }

}
