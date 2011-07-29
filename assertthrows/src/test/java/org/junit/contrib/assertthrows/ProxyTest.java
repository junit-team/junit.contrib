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
import static org.junit.Assert.assertNull;
import static org.junit.contrib.assertthrows.AssertThrows.assertThrows;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Tests that are using a the <code>assertThrows</code> methods.
 *
 * @author Thomas Mueller
 */
public class ProxyTest {

    @Test
    public void testExpectedException() {
        List<String> list = new ArrayList<String>();
        assertThrows(list).get(0);
    }

    @Test
    public void testExpectedExceptionClass() {
        final List<String> list = new ArrayList<String>();
        assertThrows(IndexOutOfBoundsException.class, list).get(0);
        assertThrows(Exception.class, list).get(0);
    }

    @Test
    public void testExpectedExceptionAsObject() {
        final List<String> list = new ArrayList<String>();
        assertThrows(
                new IndexOutOfBoundsException("Index: 0, Size: 0"), list).
                get(0);
    }

    @Test
    public void testDetectNoExceptionWasThrown() {
        final List<String> list = new ArrayList<String>();
        Throwable e;

        e = new AssertThrows() { public void test() {
            assertThrows(list).size();
        }}.getLastThrown();
        assertEquals("Expected an exception to be thrown,\n" +
                "but the method size() returned 0",
                e.getMessage());
        assertNull(e.getCause());

        e = new AssertThrows() { public void test() {
            assertThrows(NullPointerException.class, list).size();
        }}.getLastThrown();
        assertEquals("Expected an exception of type\n" +
                "NullPointerException to be thrown,\n" +
                "but the method size() returned 0",
                e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void testWrongException() {
        final List<String> list = new ArrayList<String>();
        Throwable e;

        e = new AssertThrows() { public void test() {
            assertThrows(NullPointerException.class, list).get(0);
        }}.getLastThrown();
        assertEquals("Expected an exception of type\n" +
                "NullPointerException to be thrown,\n" +
                "but the method get(0) threw an exception of type\n" +
                "IndexOutOfBoundsException " +
                "(see in the 'Caused by' for the exception that was thrown)",
                e.getMessage());
        assertEquals(IndexOutOfBoundsException.class, e.getCause().getClass());

        e = new AssertThrows() { public void test() {
            assertThrows(new IndexOutOfBoundsException(), list).get(0);
        }}.getLastThrown();
        assertEquals("Expected exception message <null>, but got <Index: 0, Size: 0>",
                e.getMessage());
        assertEquals(IndexOutOfBoundsException.class, e.getCause().getClass());

        e = new AssertThrows() { public void test() {
            assertThrows(new Exception("Index: 0, Size: 0"), list).get(0);
        }}.getLastThrown();
        assertEquals("Expected an exception of type\n" +
                "Exception to be thrown,\n" +
                "but the method get(0) threw an exception of type\n" +
                "IndexOutOfBoundsException " +
                "(see in the 'Caused by' for the exception that was thrown)",
                e.getMessage());
        assertEquals(IndexOutOfBoundsException.class, e.getCause().getClass());
    }

    @Test
    public void testWrongUsage() {
        final List<String> list = new ArrayList<String>();
        Throwable e;

        e = new AssertThrows() { public void test() {
            assertThrows((List<String>) null).get(0);
        }}.getLastThrown();
        assertEquals("The passed object is null", e.getMessage());

        e = new AssertThrows() { public void test() {
            assertThrows((Class<Exception>) null, list).get(0);
        }}.getLastThrown();
        assertEquals("The passed exception class is null", e.getMessage());

        e = new AssertThrows() { public void test() {
            assertThrows((Exception) null, list).get(0);
        }}.getLastThrown();
        assertEquals("The passed exception is null", e.getMessage());
    }

    @Test
    public void testMethodReturnsPrimitive() {
        Number n = new BadNumber();
        assertThrows(n).doubleValue();
        assertThrows(n).floatValue();
        assertThrows(n).intValue();
        assertThrows(n).longValue();
        assertThrows(n).byteValue();
        assertThrows(n).shortValue();
    }

    /**
     * A number class that throws UnsupportedOperationException when trying to
     * convert a number.
     */
    public static class BadNumber extends Number {

        private static final long serialVersionUID = 1L;

        @Override
        public double doubleValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public float floatValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int intValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long longValue() {
            throw new UnsupportedOperationException();
        }

    }

}
