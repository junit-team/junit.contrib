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
import static org.junit.contrib.assertthrows.AssertThrows.assertThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A sample test case.
 *
 * @author Thomas Mueller
 */
public class MyListTest {

    private MyList list;

    @Before
    public void before() {
        list = new MyList();
    }

    @After
    public void after() {
        list.close();
    }

    @Test
    public void testAdd() {
        // empty
        assertEquals(0, list.size());
        // trying to add null is not allowed
        assertThrows(list).add(null);
        assertEquals(0, list.size());
        // one element
        list.add("Apple");
        assertEquals(1, list.size());
        assertEquals("Apple", list.get(0));
        // two elements
        list.add("Banana");
        assertEquals(2, list.size());
        assertEquals("Apple", list.get(0));
        assertEquals("Banana", list.get(1));
    }

    @Test
    public void testGet() {
        // empty
        assertThrows(list).get(-1);
        assertThrows(list).get(0);
        // one element
        list.add("Apple");
        assertEquals("Apple", list.get(0));
        assertThrows(list).get(-1);
        assertThrows(list).get(1);
        // empty again
        list.remove(0);
        assertThrows(list).get(0);
    }

    @Test
    public void testSet() {
        // empty
        assertThrows(list).set(0, "Kiwi");
        // one element
        list.add("Apple");
        list.set(0, "Apfel");
        assertEquals("Apfel", list.get(0));
        assertThrows(list).set(1, "Kiwi");
        // two elements
        list.add("Banana");
        list.set(1, "Banane");
        assertEquals("Banane", list.get(1));
    }

    @Test
    public void testEmptyList() {
        assertEquals(0, list.size());
        assertThrows(list).set(0, "Apple");
        assertThrows(list).remove(0);
        assertThrows(list).get(0);
    }

    @Test
    public void testClose() {
        list.add("Apple");
        list.close();
        // closing multiple times is allowed
        list.close();
        // everything else isn't
        assertThrows(list).add("Kiwi");
        assertThrows(list).get(0);
        assertThrows(list).remove(0);
        assertThrows(list).size();
    }

}
