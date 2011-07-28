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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

/**
 * This proxy factory uses the cglib and (optionally) objenesis to create new
 * proxy objects for existing objects.
 *
 * @author Thomas Mueller
 */
public class CglibProxyFactory extends ProxyFactory {

    private static final CglibProxyFactory INSTANCE = new CglibProxyFactory();

    /**
     * The reflection object creator.
     */
    private static final ObjectCreator REFLECTION_OBJECT_CREATOR = new ReflectionObjectCreator();

    /**
     * The default object creator (objenesis if in the classpath, or the reflection creator if not).
     */
    private static final ObjectCreator OBJECT_CREATOR;

    static {
        ObjectCreator o;
        try {
            o = new ObjenesisObjectCreator();
        } catch (Throwable e) {
            o = REFLECTION_OBJECT_CREATOR;
        }
        OBJECT_CREATOR = o;
    }

    /**
     * Get an instance of this proxy factory.
     *
     * @return the proxy factory
     */
    public static CglibProxyFactory getInstance() {
        return INSTANCE;
    }

    public <T> T createProxy(T obj, InvocationHandler handler) {
        return createProxy(obj, handler, false);
    }

    /**
     * Create a proxy object for the given object. This will allow to disable
     * using objenesis to create the instance, even if it is in the classpath.
     *
     * @param <T> the type
     * @param obj the object
     * @param handler the invocation handler
     * @param forceUsingReflection whether to force using reflection to create a new instance,
     *          even if objenesis is in the classpath
     * @return the proxy
     * @throws IllegalArgumentException a proxy could not be generated for the
     *             given object
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(T obj, final InvocationHandler handler, boolean forceUsingReflection) {
        Class<?> c = obj.getClass();
        net.sf.cglib.proxy.InvocationHandler cglibHandler =
                new net.sf.cglib.proxy.InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return handler.invoke(proxy, method, args);
            }
        };
        Class<?> proxyClass = createProxyClass(c);
        Factory proxy = (Factory) newInstance(c, proxyClass, forceUsingReflection);
        proxy.setCallbacks(new Callback[] { cglibHandler, NoOp.INSTANCE });
        return (T) proxy;
    }

    private Object newInstance(Class<?> baseClass, Class<?> c, boolean forceUsingReflection) {
        ObjectCreator creator = forceUsingReflection ?
                REFLECTION_OBJECT_CREATOR : OBJECT_CREATOR;
        try {
            return creator.newInstance(c);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Could not create a new proxy instance for the base class " +
                    baseClass.getName() +
                    (REFLECTION_OBJECT_CREATOR == creator ?
                    " (probably because objenesis is not used)" : ""));
            iae.initCause(e);
            throw iae;
        }
    }

    private <T> Class<?> createProxyClass(Class<?> baseClass) {
        Enhancer enhancer = new Enhancer() {
            @SuppressWarnings("unchecked")
            protected void filterConstructors(Class sc, List constructors) {
                // don't filter, so that even classes without
                // visible constructors will work
            }
        };
        enhancer.setUseFactory(true);
        enhancer.setSuperclass(baseClass);
        enhancer.setCallbackType(net.sf.cglib.proxy.InvocationHandler.class);
        if (baseClass.getSigners() != null) {
            // if the package is signed, prepend the proxy package name
            enhancer.setNamingPolicy(new DefaultNamingPolicy() {
                public String getClassName(String prefix,
                        String source, Object key, Predicate names) {
                    return ProxyFactory.PROXY_PACKAGE_NAME + "." +
                            super.getClassName(prefix, source, key, names);
                }
            });
        }

        try {
            return enhancer.createClass();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Can not create a proxy for the class " + baseClass.getName(), e);
        }
    }

    /**
     * An tool to create new objects, if possible without calling any constructors.
     */
    interface ObjectCreator {
        Object newInstance(Class<?> c) throws Exception;
    }

    /**
     * An object creator that uses the objenesis library.
     */
    static class ObjenesisObjectCreator implements ObjectCreator {

        /**
         * A ObjenesisStd object, or null if objenesis is not in the classpath.
         */
        private static final Objenesis OBJENESIS = new ObjenesisStd();

        public Object newInstance(Class<?> c) {
            return OBJENESIS.newInstance(c);
        }

    }

    /**
     * An object creator that uses reflection
     */
    static class ReflectionObjectCreator implements ObjectCreator {

        public Object newInstance(Class<?> c) throws Exception {
            Constructor<?> cons = c.getConstructor(new Class<?>[] {});
            return cons.newInstance();
        }

    }

}
