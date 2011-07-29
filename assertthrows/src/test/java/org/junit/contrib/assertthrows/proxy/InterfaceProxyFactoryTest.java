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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.contrib.assertthrows.AssertThrows;

/**
 * Test creating proxies using the interface proxy factory.
 *
 * @author Thomas Mueller
 */
public class InterfaceProxyFactoryTest {

    StringBuilder buff = new StringBuilder();
    int methodCallCount;

    @Test
    public void testObject() {
        new AssertThrows(new IllegalArgumentException(
                "Can not create a proxy using the InterfaceProxyFactory, " +
                "because the class java.lang.Object does not implement any interfaces")) {
            public void test() {
                createProxy(new Object()).equals(null);
        }};
    }

    @Test
    public void testJavaUtilList() {
        List<String> list = new ArrayList<String>();
        createProxy(list).size();
        assertEquals("size = 0", buff.toString());
    }

    <T> T createProxy(final T obj) {
        return InterfaceProxyFactory.getInstance().createProxy(obj, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                buff.append(method.getName());
                Object o = method.invoke(obj, args);
                buff.append(" = ").append(o);
                methodCallCount++;
                return o;
            }
        });
    }

}
