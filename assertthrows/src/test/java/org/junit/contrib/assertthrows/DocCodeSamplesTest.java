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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.contrib.assertthrows.AssertThrows.assertThrows;

/**
 * Test that match the code samples in the documentation.
 *
 * @author Thomas Mueller
 */
public class DocCodeSamplesTest {

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIndexOutOfBoundsException() {
        List<String> emptyList = new ArrayList<String>();
        emptyList.get(0);
    }

    @Test
    public void testTryCatchFail() {
        List<String> emptyList = new ArrayList<String>();
        try {
            emptyList.get(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testProxy() {
        List<String> emptyList = new ArrayList<String>();
        assertThrows(emptyList).get(0);
        assertThrows(IndexOutOfBoundsException.class, emptyList).get(0);
        assertThrows(new IndexOutOfBoundsException(
                "Index: 0, Size: 0"), emptyList).
                get(0);
    }

    @Test
    public void testClassProxy() {
        ArrayList<String> emptyList = new ArrayList<String>();
        AssertThrows.useClassProxy(ArrayList.class);
        assertThrows(emptyList).get(0);
    }

    @Test
    public void testAnonymousClass() {
        new AssertThrows() { public void test() {
            Integer.parseInt("x");
        }};
        new AssertThrows(NumberFormatException.class) { public void test() {
            Integer.parseInt("x");
        }};
        new AssertThrows(new NumberFormatException("For input string: \"x\"")) {
            public void test() {
                Integer.parseInt("x");
        }};
        new AssertThrows() { public void test() {
            Integer.parseInt("x");
        }};
        Throwable t = AssertThrows.getLastThrown();
        assertEquals("For input string: \"x\"", t.getMessage());
    }

    @Test
    public void testDetectNoMethodWasCalled() {
        new AssertThrows() { public void test() {
            List<String> list = new ArrayList<String>();
            assertThrows(list);
            assertThrows(list).get(0);
        }};
    }

}
