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

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Method;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.contrib.assertthrows.verify.ExceptionVerifier;

/**
 * An example on how to create a custom <code>assertThrows</code> method. This
 * example verifies the error code of a <code>SQLException</code>.
 *
 * @author Thomas Mueller
 */
public class CustomAssertThrowsTest {

    @Test
    public void testSQLErrorCode() throws Exception {
        final TestClass obj = new TestClass();

        assertThrowsErrorCode(100, obj).throwThis(new SQLException("", "", 100));

        new AssertThrows() { public void test() throws Exception {
            assertThrowsErrorCode(101, obj).throwThis(new SQLException("", "", 100));
        }};
        Throwable e = AssertThrows.getLastThrown();
        assertEquals("Expected error code: 101 got: 100", e.getMessage());

        new AssertThrows() { public void test() throws Exception {
            assertThrowsErrorCode(101, obj).throwThis(new Exception());
        }};
        e = AssertThrows.getLastThrown();
        assertEquals("Expected an exception of type\n" +
                "SQLException to be thrown,\n" +
                "but the method throwThis(java.lang.Exception) threw an exception of type\n" +
                "Exception (see in the 'Caused by' for the exception that was thrown)",
                e.getMessage());
    }

    public static <T> T assertThrowsErrorCode(final int errorCode, T obj) {
        return ExceptionVerifier.createVerifyingProxy(new SQLErrorCodeVerifier(errorCode), obj);
    }

    /**
     * A class that can throw any exception.
     */
    public static class TestClass {
        public void throwThis(Exception e) throws Exception {
            throw e;
        }
    }

    /**
     * A verifier that checks the error code, and only allows SQLExceptions.
     */
    public static class SQLErrorCodeVerifier extends ExceptionVerifier {
        private final int errorCode;

        SQLErrorCodeVerifier(int errorCode) {
            super(SQLException.class);
            this.errorCode = errorCode;
        }

        public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
            if (t instanceof SQLException) {
                SQLException e = (SQLException) t;
                if (e.getErrorCode() != errorCode) {
                    AssertionError ae = new AssertionError(
                            "Expected error code: " + errorCode + " got: " + e.getErrorCode());
                    ae.initCause(t);
                    throw ae;
                }
            }
            return super.verify(returnValue, t, m, args);
        }

    }

}
