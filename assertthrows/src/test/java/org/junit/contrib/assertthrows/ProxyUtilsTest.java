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
import org.junit.Test;
import org.junit.contrib.assertthrows.impl.ProxyUtils;

/**
 * Tests for the proxy utility class.
 *
 * @author Thomas Mueller
 */
public class ProxyUtilsTest {

    @Test
    public void testDefaultValue() {
        assertNull(ProxyUtils.getDefaultValue(Integer.class));
        assertEquals(
                Boolean.valueOf(false),
                ProxyUtils.getDefaultValue(boolean.class));
        assertEquals(
                Byte.valueOf((byte) 0),
                ProxyUtils.getDefaultValue(byte.class));
        assertEquals(
                Character.valueOf((char) 0),
                ProxyUtils.getDefaultValue(char.class));
        assertEquals(
                Short.valueOf((short) 0),
                ProxyUtils.getDefaultValue(short.class));
        assertEquals(
                Integer.valueOf(0),
                ProxyUtils.getDefaultValue(int.class));
        assertEquals(
                Long.valueOf(0L),
                ProxyUtils.getDefaultValue(long.class));
        assertEquals(
                Float.valueOf(0F),
                ProxyUtils.getDefaultValue(float.class));
        assertEquals(
                Double.valueOf(0D),
                ProxyUtils.getDefaultValue(double.class));
    }

}
