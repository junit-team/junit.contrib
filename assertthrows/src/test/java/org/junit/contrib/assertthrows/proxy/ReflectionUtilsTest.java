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
package org.junit.contrib.assertthrows.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Random;
import org.junit.Test;

/**
 * Tests for the proxy utility classes.
 *
 * @author Thomas Mueller
 */
public class ReflectionUtilsTest {

    @Test
    public void testDefaultValue() {
        assertNull(ReflectionUtils.getDefaultValue(Integer.class));
        assertEquals(
                null,
                ReflectionUtils.getDefaultValue(void.class));
        assertEquals(
                Boolean.valueOf(false),
                ReflectionUtils.getDefaultValue(boolean.class));
        assertEquals(
                Byte.valueOf((byte) 0),
                ReflectionUtils.getDefaultValue(byte.class));
        assertEquals(
                Character.valueOf((char) 0),
                ReflectionUtils.getDefaultValue(char.class));
        assertEquals(
                Short.valueOf((short) 0),
                ReflectionUtils.getDefaultValue(short.class));
        assertEquals(
                Integer.valueOf(0),
                ReflectionUtils.getDefaultValue(int.class));
        assertEquals(
                Long.valueOf(0L),
                ReflectionUtils.getDefaultValue(long.class));
        assertEquals(
                Float.valueOf(0F),
                ReflectionUtils.getDefaultValue(float.class));
        assertEquals(
                Double.valueOf(0D),
                ReflectionUtils.getDefaultValue(double.class));
    }

    @Test
    public void testNonPrimitiveClass() {
        assertEquals(Object.class,
                ReflectionUtils.getNonPrimitiveClass(Object.class));
        assertEquals(Void.class,
                ReflectionUtils.getNonPrimitiveClass(void.class));
        assertEquals(Boolean.class,
                ReflectionUtils.getNonPrimitiveClass(boolean.class));
        assertEquals(Byte.class,
                ReflectionUtils.getNonPrimitiveClass(byte.class));
        assertEquals(Character.class,
                ReflectionUtils.getNonPrimitiveClass(char.class));
        assertEquals(Short.class,
                ReflectionUtils.getNonPrimitiveClass(short.class));
        assertEquals(Integer.class,
                ReflectionUtils.getNonPrimitiveClass(int.class));
        assertEquals(Long.class,
                ReflectionUtils.getNonPrimitiveClass(long.class));
        assertEquals(Float.class,
                ReflectionUtils.getNonPrimitiveClass(float.class));
        assertEquals(Double.class,
                ReflectionUtils.getNonPrimitiveClass(double.class));
    }

    @Test
    public void testPackageName() {
        assertEquals("", ReflectionUtils.getPackageName(int.class));
        assertEquals("java.lang", ReflectionUtils.getPackageName(Integer.class));
        Class<?> p = CompilingProxyFactory.getInstance().getClassProxy(Random.class);
        assertEquals("proxy.java.util", ReflectionUtils.getPackageName(p));
    }

    @Test
    public void testUniqueFieldName() {
        assertEquals("test", CompilingProxyFactory.getUniqueFieldName(
                ReflectionUtilsTest.class, "test"));

        /**
         * A very simple test class.
         */
        class Test {

            /**
             * This field can't be overwritten in s subclass.
             */
            public String value;

            public String toString() {
                return value;
            }
        }

        /**
         * Another test class with a similar field.
         */
        class Test2 extends Test {

            /**
             * Another field that can't be overwritten.
             */
            protected String value0;

            public String toString() {
                return value0;
            }
        }

        assertEquals("value1", CompilingProxyFactory.getUniqueFieldName(Test2.class, "value"));
    }

}
