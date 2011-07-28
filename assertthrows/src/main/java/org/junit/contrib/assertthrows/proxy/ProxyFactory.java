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
import java.util.HashMap;

/**
 * A proxy factory can create new proxy objects for existing objects.
 *
 * @author Thomas Mueller
 */
public abstract class ProxyFactory {

    /**
     * The package name for proxy classes.
     */
    public static final String PROXY_PACKAGE_NAME = "proxy";

    /**
     * This map contains the proxy factory to use for the given class. It is
     * only used if the proxy was explicitly set for the given class.
     */
    private static final HashMap<Class<?>, ProxyFactory> FACTORY_MAP =
        new HashMap<Class<?>, ProxyFactory>();

    /**
     * The cglib proxy factory, or null if cglib is not in the classpath.
     */
    private static final ProxyFactory CGLIB_FACTORY;

    static {
        ProxyFactory instance;
        try {
             instance = CglibProxyFactory.getInstance();
        } catch (Throwable e) {
            // not available
            instance = CompilingProxyFactory.getInstance();
        }
        CGLIB_FACTORY = instance;
    }

    /**
     * Get the default proxy factory that is used for classes that don't
     * implement interfaces.
     *
     * @return the proxy factory
     */
    public static ProxyFactory getClassProxyFactory() {
        if (CGLIB_FACTORY != null) {
            return CGLIB_FACTORY;
        }
        return CompilingProxyFactory.getInstance();
    }

    /**
     * Set the proxy factory to use for this class from now on.
     *
     * @param c the class
     * @param factory the factory to use for the given class
     */
    public static void setProxyFactory(Class<?> c, ProxyFactory factory) {
        FACTORY_MAP.put(c, factory);
    }

    /**
     * Use the default class proxy factory for this class from now on, even if
     * the class does implement an interface.
     *
     * @param c the class
     */
    public static void useClassProxyFactory(Class<?> c) {
        setProxyFactory(c, getClassProxyFactory());
    }

    /**
     * Get the most appropriate proxy factory for the given class.
     *
     * @param c the class
     * @return the proxy factory
     */
    public static ProxyFactory getFactory(Class<?> c) {
        ProxyFactory factory = FACTORY_MAP.get(c);
        if (factory != null) {
            return factory;
        }
        Class<?>[] interfaces = c.getInterfaces();
        if (interfaces.length > 0) {
            return InterfaceProxyFactory.getInstance();
        }
        return getClassProxyFactory();
    }

    /**
     * Create a proxy object for the given object.
     *
     * @param <T> the type
     * @param obj the object
     * @param handler the invocation handler
     * @return the proxy
     * @throws IllegalArgumentException a proxy could not be generated for the given object
     */
    public abstract <T> T createProxy(T obj, InvocationHandler handler);

}
