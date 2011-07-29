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
package org.junit.contrib.assertthrows.verify;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.contrib.assertthrows.proxy.ProxyFactory;
import org.junit.contrib.assertthrows.proxy.ReflectionUtils;

/**
 * A result verifier that checks against an expected exception class.
 *
 * @author Thomas Mueller
 */
public class ExceptionVerifier implements ResultVerifier {

    private final Class<? extends Exception> expectedExceptionClass;
    private final Exception expectedException;

    /**
     * Create a new verifier that only checks if an exception or error was thrown.
     */
    public ExceptionVerifier() {
        this.expectedExceptionClass = null;
        this.expectedException = null;
    }

    /**
     * Create a new verifier that checks against a given exception class, or any
     * of it's subclasses.
     *
     * @param expectedExceptionClass the expected exception base class (may not
     *            be null)
     */
    public ExceptionVerifier(Class<? extends Exception> expectedExceptionClass) {
        if (expectedExceptionClass == null) {
            throw new NullPointerException("The passed exception class is null");
        }
        this.expectedExceptionClass = expectedExceptionClass;
        this.expectedException = null;
    }

    /**
     * Create a new verifier that checks against a given exception class for an
     * exact match, plus additionally checks the message.
     *
     * @param expectedException the expected exception (may not be null)
     */
    public ExceptionVerifier(Exception expectedException) {
        if (expectedException == null) {
            throw new NullPointerException("The passed exception is null");
        }
        this.expectedExceptionClass = expectedException.getClass();
        if (expectedExceptionClass == null) {
            // overcareful
            throw new NullPointerException("The passed exception class is null");
        }
        this.expectedException = expectedException;
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
    public static <T> T createVerifyingProxy(final ResultVerifier verifier, final T obj) {
        if (obj == null) {
            throw new NullPointerException("The passed object is null");
        }
        InvocationHandler handler = new InvocationHandler() {
            private Exception called = new Exception("No method was called on " + obj);
            public void finalize() {
                if (called != null) {
                    called.printStackTrace(System.err);
                }
            }
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                called = null;
                while (true) {
                    Object ret = null;
                    Throwable thrown = null;
                    try {
                        ret = method.invoke(obj, args);
                    } catch (InvocationTargetException e) {
                        thrown = e.getTargetException();
                    }
                    if (!verifier.verify(ret, thrown, method, args)) {
                        return ReflectionUtils.getDefaultValue(method.getReturnType());
                    }
                }
            }
        };
        ProxyFactory factory = ProxyFactory.getFactory(obj.getClass());
        return factory.createProxy(obj, handler);
    }

    public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
        if (t != null) {
            // the method did throw an exception
            if (expectedExceptionClass == null) {
                // don't verify the exception class
                return false;
            } else if (expectedExceptionClass.isAssignableFrom(t.getClass())) {
                // matching expected class
                if (expectedException == null) {
                    // don't verify the exception itself
                    return false;
                }
                // the exception class must match exactly
                if (expectedException.getClass().equals(t.getClass())) {
                    String expectedMsg = expectedException.getMessage();
                    String msg = t.getMessage();
                    if (expectedMsg == null) {
                        if (msg == null) {
                            // both messages are null
                            return false;
                        }
                    } else if (expectedMsg.equals(msg)) {
                        // messages match
                        return false;
                    }
                    String message =  "Expected exception message <" + expectedMsg +
                            ">, but got <" + msg + ">";
                    throw buildAssertionError(message, t);
                }
            }
        }
        String type;
        if (expectedExceptionClass == null) {
            type = "";
        } else {
            type = " of type\n" +
                expectedExceptionClass.getSimpleName();
        }
        String but;
        if (m == null) {
            but = "but the method ";
        } else {
            but = "but the method " + formatMethodCall(m, args) + " ";
        }
        String result;
        if (t == null) {
            if (returnValue == null) {
                result = "returned successfully";
            } else {
                result = "returned " + returnValue;
            }
        } else {
            result = "threw an exception of type\n" +
            t.getClass().getSimpleName() +
            " (see in the 'Caused by' for the exception that was thrown)";
        }
        String message =
            "Expected an exception" + type + " to be thrown,\n" +
            but + result;
        throw buildAssertionError(message, t);
    }

    private AssertionError buildAssertionError(String message, Throwable got) {
        AssertionError ae = new AssertionError(message);
        if (got != null) {
            ae.initCause(got);
        }
        return ae;
    }

    /**
     * Format a method call, including arguments, for an exception message.
     *
     * @param m the method
     * @param args the arguments
     * @return the formatted string
     */
    private static String formatMethodCall(Method m, Object... args) {
        StringBuilder buff = new StringBuilder();
        buff.append(m.getName()).append('(');
        for (int i = 0; args != null && i < args.length; i++) {
            Object a = args[i];
            if (i > 0) {
                buff.append(", ");
            }
            buff.append(a == null ? "null" : a.toString());
        }
        buff.append(")");
        return buff.toString();
    }

}
