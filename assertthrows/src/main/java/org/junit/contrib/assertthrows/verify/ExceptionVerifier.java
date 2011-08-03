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

import java.lang.ref.WeakReference;
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

    private static final ThreadLocal<Throwable> LAST_THROWN =
        new ThreadLocal<Throwable>();
    private static final ThreadLocal<WeakReference<VerifyingInvocationHandler>> LAST_HANDLER =
        new ThreadLocal<WeakReference<VerifyingInvocationHandler>>();

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
    public static <T> T createVerifyingProxy(ResultVerifier verifier, T obj) {
        if (obj == null) {
            throw new NullPointerException("The passed object is null");
        }
        verifyLastProxyWasUsed();
        VerifyingInvocationHandler handler = new VerifyingInvocationHandler(verifier, obj);
        setLastProxyHandler(handler);
        ProxyFactory factory = ProxyFactory.getFactory(obj.getClass());
        return factory.createProxy(obj, handler);
    }

    /**
     * Verify that the last proxy was actually used. Calling this method is
     * optional, as it is automatically verified before creating the next proxy.
     * Also, wrong usage is detected when the proxy is garbage collected.
     * However if may make sense to call this method explicitly in a tearDown
     * method or JUnit 4 Rule. This method is thread-safe, as it uses a
     * ThreadLocal internally.
     */
    public static void verifyLastProxyWasUsed() {
        WeakReference<VerifyingInvocationHandler> w = LAST_HANDLER.get();
        if (w != null) {
            VerifyingInvocationHandler last = w.get();
            if (last != null) {
                last.verifyCalled();
            }
        }
    }

    private static void setLastProxyHandler(VerifyingInvocationHandler handler) {
        WeakReference<VerifyingInvocationHandler> w =
            new WeakReference<VerifyingInvocationHandler>(handler);
        LAST_HANDLER.set(w);
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

    /**
     * Set the last thrown exception or error for the current thread.
     *
     * @param t the exception or error
     */
    public static void setLastThrown(Throwable t) {
        LAST_THROWN.set(t);
    }

    /**
     * Get the last thrown exception (if any) or error for the current thread.
     *
     * @return the exception or error, or null
     */
    public static Throwable getLastThrown() {
        return LAST_THROWN.get();
    }

    /**
     * An invocation handler that calls a result verifier after each method
     * call.
     */
    private static class VerifyingInvocationHandler implements InvocationHandler {

        private final Object obj;
        private final ResultVerifier verifier;
        private AssertionError called;

        VerifyingInvocationHandler(ResultVerifier verifier, Object obj) {
            this.verifier = verifier;
            this.obj = obj;
            this.called = new AssertionError(
                    "A proxy for the class\n" +
                    obj.getClass().getName() + "\n" +
                    "was created, but then no overridable method was called on it.\n" +
                    "See the stack trace for where the proxy was created.");
        }

        public void verifyCalled() {
            if (called != null) {
                AssertionError c = called;
                called = null;
                throw c;
            }
        }

        public void finalize() {
            if (called != null) {
                called.printStackTrace(System.err);
                called = null;
            }
        }
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            if ("finalize".equals(method.getName())) {
                // we _could_ support this method, but then detecting
                // that no method was called would no longer work
                return method.invoke(obj, args);
            }
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
    }

}
