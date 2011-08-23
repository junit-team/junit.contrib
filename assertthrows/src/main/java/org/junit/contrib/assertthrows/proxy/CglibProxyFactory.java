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
import java.lang.reflect.Modifier;
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
 * This proxy factory uses the cglib and (optionally) Objenesis to create new
 * proxy objects for existing objects.
 *
 * @author Thomas Mueller
 */
public class CglibProxyFactory extends ProxyFactory {

    /**
     * The default object creator (Objenesis if in the classpath, or the
     * reflection creator if not). Implementation note: this field might be set
     * to null by Tomcat when unloading a web application that uses this proxy
     * factory.
     */
    private ObjectCreator objectCreator;

    /**
     * Whether using Objenesis to create objects is allowed.
     */
    private boolean useObjenesis = true;

    /**
     * Get the object creator.
     *
     * @return the object creator (never null)
     */
    public ObjectCreator getObjectCreator() {
        if (objectCreator == null) {
            ObjectCreator o = new ReflectionObjectCreator();
            if (useObjenesis) {
                try {
                    o = new ObjenesisObjectCreator();
                } catch (Throwable e) {
                    useObjenesis = false;
                }
            }
            objectCreator = o;
        }
        return objectCreator;
    }

    public void setUseObjenesis(boolean useObjenesis) {
        this.useObjenesis = useObjenesis;
        objectCreator = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(T obj, final InvocationHandler handler) {
        Class<?> c = obj.getClass();
        net.sf.cglib.proxy.InvocationHandler cglibHandler =
                new net.sf.cglib.proxy.InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return handler.invoke(proxy, method, args);
            }
        };
        Class<?> proxyClass = createProxyClass(c);
        Factory proxy = (Factory) newInstance(c, proxyClass);
        proxy.setCallbacks(new Callback[] { cglibHandler, NoOp.INSTANCE });
        return (T) proxy;
    }

    private Object newInstance(Class<?> baseClass, Class<?> c) {
        ObjectCreator creator = getObjectCreator();
        try {
            return creator.newInstance(c);
        } catch (Exception e) {
            IllegalArgumentException ia = new IllegalArgumentException(
                    "Could not create a new proxy instance for the base class " +
                    baseClass.getName() +
                    (useObjenesis ? "" :
                    " (probably because Objenesis is not used)"));
            ia.initCause(e);
            throw ia;
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
     * A tool to create new objects, if possible without calling any constructors.
     */
    interface ObjectCreator {
        Object newInstance(Class<?> c) throws Exception;
    }

    /**
     * An object creator that uses the Objenesis library.
     */
    static class ObjenesisObjectCreator implements ObjectCreator {

        /**
         * A ObjenesisStd object, or null if Objenesis is not in the classpath.
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
            Constructor<?> constructor = null;
            int paramCount = Integer.MAX_VALUE;
            for (Constructor<?> cons : c.getConstructors()) {
                if (Modifier.isPublic(cons.getModifiers())) {
                    int pc = cons.getParameterTypes().length;
                    if (pc < paramCount) {
                        constructor = cons;
                        paramCount = pc;
                    }
                }
            }
            if (constructor == null) {
                throw new IllegalArgumentException(
                        "No public constructor was found for: " + c.getName());
            }
            Object[] params = new Object[paramCount];
            for (int i = 0; i < paramCount; i++) {
                Class<?> p = constructor.getParameterTypes()[i];
                params[i] = ReflectionUtils.getDefaultValue(p);
            }
            return constructor.newInstance(params);
        }

    }

}
