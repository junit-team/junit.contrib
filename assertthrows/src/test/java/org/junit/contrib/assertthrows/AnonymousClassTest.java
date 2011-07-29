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
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Tests that are using an anonymous class.
 *
 * @author Thomas Mueller
 */
public class AnonymousClassTest {

    @Test
    public void testExpectedException() {
        new AssertThrows() { public void test() {
            Integer.parseInt("x");
        }};
    }

    @Test
    public void testExpectedExceptionClass() {
        new AssertThrows(NumberFormatException.class) { public void test() {
            Integer.parseInt("x");
        }};
    }

    @Test
    public void testExpectedAssertionFailure() {
        new AssertThrows() {
            public void test() {
                fail();
            }
        };
    }

    @Test
    public void testDetectNoExceptionWasThrown() {
        final List<String> list = new ArrayList<String>();
        try {
            new AssertThrows() {
                public void test() {
                    list.size();
                }
            };
            fail();
        } catch (AssertionError e) {
            assertEquals("Expected an exception to be thrown,\n" +
                    "but the method returned successfully",
                    e.getMessage());
            assertNull(e.getCause());
        }
        try {
            new AssertThrows(NullPointerException.class) {
                public void test() {
                    list.size();
                }
            };
            fail();
        } catch (AssertionError e) {
            assertEquals("Expected an exception of type\n" +
                    "NullPointerException to be thrown,\n" +
                    "but the method returned successfully",
                    e.getMessage());
            assertNull(e.getCause());
        }
    }

    @Test
    public void testWrongException() {
        final List<String> list = new ArrayList<String>();
        try {
            new AssertThrows(NullPointerException.class) {
                public void test() {
                    list.get(0);
                }
            };
            fail();
        } catch (AssertionError e) {
            assertEquals("Expected an exception of type\n" +
                    "NullPointerException to be thrown,\n" +
                    "but the method threw an exception of type\n" +
                    "IndexOutOfBoundsException " +
                    "(see in the 'Caused by' for the exception that was thrown)",
                    e.getMessage());
            assertEquals(IndexOutOfBoundsException.class.getName(),
                    e.getCause().getClass().getName());
        }
        try {
            new AssertThrows(new IndexOutOfBoundsException()) {
                public void test() {
                    list.get(0);
                }
            };
            fail();
        } catch (AssertionError e) {
            assertEquals("Expected exception message <null>, but got <Index: 0, Size: 0>",
                    e.getMessage());
            assertEquals(IndexOutOfBoundsException.class, e.getCause().getClass());
        }
        try {
            new AssertThrows(new Exception("Index: 0, Size: 0")) {
                public void test() {
                    list.get(0);
                }
            };
            fail();
        } catch (AssertionError e) {
            assertEquals("Expected an exception of type\n" +
                    "Exception to be thrown,\n" +
                    "but the method threw an exception of type\n" +
                    "IndexOutOfBoundsException " +
                    "(see in the 'Caused by' for the exception that was thrown)",
                    e.getMessage());
            assertEquals(IndexOutOfBoundsException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testWrongUsage() {
        final List<String> list = new ArrayList<String>();
        new AssertThrows(new NullPointerException(
                "The passed exception class is null")) {
            public void test() {
                new AssertThrows((Class<Exception>) null) { public void test() {
                    list.get(0);
                }};
        }};
        new AssertThrows(new NullPointerException(
                "The passed exception is null")) {
            public void test() {
                new AssertThrows((Exception) null) { public void test() {
                    list.get(0);
                }};
        }};
    }

}
