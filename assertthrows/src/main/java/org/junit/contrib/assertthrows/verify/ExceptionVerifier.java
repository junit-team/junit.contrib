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

import java.lang.reflect.Method;

/**
 * A result verifier that checks against an expected exception class.
 *
 * @author Thomas Mueller
 */
public class ExceptionVerifier implements ResultVerifier {

    private final Class<? extends Exception> expectedExceptionClass;
    private final String expectedMessage;

    /**
     * Create a new verifier that checks against the given class (or any of it's
     * subclasses), or doesn't check the exception class at all (if the passed
     * class is null).
     *
     * @param expectedExceptionClass the expected exception base class, or null
     * @param expectedMessage the expected message, or null
     */
    public ExceptionVerifier(
            Class<? extends Exception> expectedExceptionClass,
            String expectedMessage) {
        this.expectedExceptionClass = expectedExceptionClass;
        this.expectedMessage = expectedMessage;
    }

    public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
        if (t != null) {
            // the method did throw an exception
            if (expectedExceptionClass == null) {
                // don't verify the exception class
                return false;
            } else if (expectedExceptionClass.isAssignableFrom(t.getClass())) {
                // matching expected class
                if (expectedMessage == null) {
                    // don't verify the message
                    return false;
                }
                if (expectedMessage.equals(t.getMessage())) {
                    return false;
                }
                throw new AssertionError(
                        "Expected message:\n" + expectedMessage + "\n" +
                        "but was: " + t.getMessage());
            }
        }
        String expected;
        if (expectedExceptionClass == null) {
            expected = "Expected an exception to be thrown,\n";
        } else {
            expected = "Expected an exception of type\n" +
                    expectedExceptionClass.getSimpleName() +
                    " to be thrown,\n";
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
        AssertionError ae = new AssertionError(expected + but + result);
        if (t != null) {
            ae.initCause(t);
        }
        throw ae;
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
