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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * A generator for proxies that generate one or multiple interfaces.
 *
 * @author Thomas Mueller
 */
public class InterfaceProxyFactory extends ProxyFactory {

    private static final InterfaceProxyFactory INSTANCE = new InterfaceProxyFactory();

    /**
     * Get an instance of this proxy factory.
     *
     * @return the proxy factory
     */
    public static InterfaceProxyFactory getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(T obj, final InvocationHandler handler) {
        Class<?> c = obj.getClass();
        Class<?>[] interfaces = c.getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException(
                    "Can not create a proxy using the " +
                    InterfaceProxyFactory.class.getSimpleName() +
                    ", because the class " + c.getName() +
                    " does not implement any interfaces");
        }
        return (T) Proxy.newProxyInstance(c.getClassLoader(), interfaces, handler);
    }

}
