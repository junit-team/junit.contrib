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
        final List<String> list = new ArrayList<String>();
        assertThrows(list).get(0);
    }

    @Test
    public void testExpectedExceptionClass() {
        final List<String> list = new ArrayList<String>();
        assertThrows(IndexOutOfBoundsException.class, list).get(0);
    }

    @Test
    public void testDetectNoExceptionWasThrown() {
        final List<String> list = new ArrayList<String>();
        try {
            assertThrows(list).size();
        } catch (AssertionError e) {
            assertEquals("Expected an exception to be thrown,\n" +
                    "but the method size() returned 0",
                    e.getMessage());
            assertNull(e.getCause());
        }
        try {
            assertThrows(NullPointerException.class, list).size();
        } catch (AssertionError e) {
            assertEquals("Expected an exception of type\n" +
                    "NullPointerException to be thrown,\n" +
                    "but the method size() returned 0",
                    e.getMessage());
            assertNull(e.getCause());
        }
    }

    @Test
    public void testWrongException() {
        final List<String> list = new ArrayList<String>();
        try {
            assertThrows(NullPointerException.class, list).get(0);
        } catch (AssertionError e) {
            assertEquals("Expected an exception of type\n" +
                    "NullPointerException to be thrown,\n" +
                    "but the method get(0) threw an exception of type\n" +
                    "IndexOutOfBoundsException " +
                    "(see in the 'Caused by' for the exception that was thrown)",
                    e.getMessage());
            assertEquals(IndexOutOfBoundsException.class, e.getCause().getClass());
        }
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
