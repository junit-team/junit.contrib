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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import org.junit.contrib.assertthrows.impl.ProxyCodeGenerator;
import org.junit.contrib.assertthrows.impl.ResultVerifier;

/**
 * A facility to test for exceptions.
 *
 * @author Thomas Mueller
 */
public abstract class AssertThrows {

    protected final ResultVerifier verifier;

    /**
     * Create a new assertion object, and call the test method to verify the
     * expected exception is thrown.
     *
     * @param expectedExceptionClass the expected exception class
     */
    public AssertThrows(final Class<? extends Exception> expectedExceptionClass) {
        this(new ResultVerifier() {
            public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
                if (t == null) {
                    throw new AssertionError("Expected an exception of type " +
                            expectedExceptionClass.getSimpleName() +
                            " to be thrown, but the method returned successfully");
                }
                if (!expectedExceptionClass.isAssignableFrom(t.getClass())) {
                    AssertionError ae = new AssertionError(
                            "Expected an exception of type\n" +
                            expectedExceptionClass.getSimpleName() +
                            " to be thrown, but the method under test threw an exception of type\n" +
                            t.getClass().getSimpleName() +
                            " (see in the 'Caused by' for the exception that was thrown)");
                    ae.initCause(t);
                    throw ae;
                }
                return false;
            }
        });
    }

    /**
     * Create a new assertion object, and call the test method to verify the
     * expected exception is thrown.
     */
    public AssertThrows() {
        this(new ResultVerifier() {
            public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
                if (t == null) {
                    throw new AssertionError(
                            "Expected an exception to be thrown, but the method returned successfully");
                }
                // all exceptions are fine
                return false;
            }
        });
    }

    private AssertThrows(ResultVerifier verifier) {
        this.verifier = verifier;
        verify();
    }

    /**
     * Run the <code>test()</code> method and verify it throws an exception.
     */
    protected void verify() {
        boolean success = false;
        try {
            test();
            success = true;
        } catch (Exception e) {
            verifier.verify(null, e, null);
        } catch (Error e) {
            verifier.verify(null, e, null);
        }
        if (success) {
            verifier.verify(null, null, null);
        }
    }

    /**
     * Verify the next method call on the object will throw an exception.
     *
     * @param <T> the class of the object
     * @param obj the object to wrap
     * @return a proxy for the object
     */
    public static <T> T assertThrows(final T obj) {
        return assertThrows(new ResultVerifier() {
            public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
                if (t == null) {
                    throw new AssertionError("Expected an exception to be thrown, but the method returned " +
                            returnValue +
                            " when calling " + ProxyCodeGenerator.formatMethodCall(m, args));
                }
                return false;
            }
        }, obj);
    }

    /**
     * Verify the next method call on the object will throw an exception.
     *
     * @param <T> the class of the object
     * @param expectedExceptionClass the expected exception class to be thrown
     * @param obj the object to wrap
     * @return a proxy for the object
     */
    public static <T> T assertThrows(final Class<? extends Exception> expectedExceptionClass, final T obj) {
        return assertThrows(new ResultVerifier() {
            public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
                if (t == null) {
                    throw new AssertionError("Expected an exception of type " +
                            expectedExceptionClass.getSimpleName() +
                            " to be thrown, but the method returned " +
                            returnValue +
                            " when calling " + ProxyCodeGenerator.formatMethodCall(m, args));
                }
                if (!expectedExceptionClass.isAssignableFrom(t.getClass())) {
                    AssertionError ae = new AssertionError(
                            "Expected an exception of type\n" +
                            expectedExceptionClass.getSimpleName() +
                            " to be thrown, but the method under test threw an exception of type\n" +
                            t.getClass().getSimpleName() +
                            " (see in the 'Caused by' for the exception that was thrown) " +
                            " for " + ProxyCodeGenerator.formatMethodCall(m, args));
                    ae.initCause(t);
                    throw ae;
                }
                return false;
            }
        }, obj);
    }

    /**
     * Verify the next method call on the object will throw an exception.
     *
     * @param <T> the class of the object
     * @param verifier the result verifier to call
     * @param obj the object to wrap
     * @return a proxy for the object
     */
    @SuppressWarnings("unchecked")
    private static <T> T assertThrows(final ResultVerifier verifier, final T obj) {
        Class<?> c = obj.getClass();
        InvocationHandler ih = new InvocationHandler() {
            private Exception called = new Exception("No method called");
            public void finalize() {
                if (called != null) {
                    called.printStackTrace(System.err);
                }
            }
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                try {
                    called = null;
                    Object ret = method.invoke(obj, args);
                    verifier.verify(ret, null, method, args);
                    return ret;
                } catch (InvocationTargetException e) {
                    verifier.verify(null, e.getTargetException(), method, args);
                    Class<?> retClass = method.getReturnType();
                    if (!retClass.isPrimitive()) {
                        return null;
                    }
                    if (retClass == boolean.class) {
                        return false;
                    } else if (retClass == byte.class) {
                        return (byte) 0;
                    } else if (retClass == char.class) {
                        return (char) 0;
                    } else if (retClass == short.class) {
                        return (short) 0;
                    } else if (retClass == int.class) {
                        return 0;
                    } else if (retClass == long.class) {
                        return 0L;
                    } else if (retClass == float.class) {
                        return 0F;
                    } else if (retClass == double.class) {
                        return 0D;
                    }
                    return null;
                }
            }
        };
        if (!ProxyCodeGenerator.isGenerated(c)) {
            Class<?>[] interfaces = c.getInterfaces();
            if (Modifier.isFinal(c.getModifiers()) || (interfaces.length > 0)) {
                // interface class proxies
                if (interfaces.length == 0) {
                    throw new AssertionError("Can not create a proxy for the class " +
                            c.getSimpleName() +
                            " because it doesn't implement any interfaces and is final");
                }
                return (T) Proxy.newProxyInstance(c.getClassLoader(), interfaces, ih);
            }
        }
        try {
            Class<?> pc = ProxyCodeGenerator.getClassProxy(c);
            Constructor cons = pc.getConstructor(new Class<?>[] { InvocationHandler.class });
            return (T) cons.newInstance(new Object[] { ih });
        } catch (Exception e) {
            throw convert(e);
        }
    }

    /**
     * Create a proxy class that extends the given class. When calling
     * assertThrows for objects of this class, the class proxy is used from now
     * on.
     *
     * @param clazz the class
     */
    public static void createClassProxy(Class<?> clazz) {
        try {
            ProxyCodeGenerator.getClassProxy(clazz);
        } catch (Exception e) {
            throw convert(e);
        }
    }

    private static AssertionError convert(Exception e) {
        AssertionError ae = new AssertionError(e.getMessage());
        ae.initCause(e);
        return ae;
    }

    /**
     * The test method that is called.
     *
     * @throws Exception the expected exception
     */
    public abstract void test() throws Exception;

}
