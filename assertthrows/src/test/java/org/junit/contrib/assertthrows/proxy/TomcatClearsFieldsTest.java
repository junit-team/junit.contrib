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

import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.contrib.assertthrows.AssertThrows;
import org.junit.contrib.assertthrows.verify.ExceptionVerifier;
import org.junit.contrib.assertthrows.verify.ResultVerifier;

/**
 * Tests what happens if Tomcat clears static fields when re-loading a web
 * application. See also: http://svn.apache.org/repos/asf/tomcat/trunk/java/
 * org/apache/catalina/loader/WebappClassLoader.java
 * <p>
 * Background: Tomcat and Glassfish 3 set most static fields (final or
 * non-final) to <code>null</code> when unloading a web application, to ensure
 * old objects can get garbage collection. This can lead to problems that are
 * hard to understand and detect.
 *
 * @author Thomas Mueller
 */
public class TomcatClearsFieldsTest {

    private static final String[] KNOWN_REFRESHED = {
        "org.junit.contrib.assertthrows.proxy.CompilingProxyFactory.compiler",
        "org.junit.contrib.assertthrows.proxy.CompilingProxyFactory.proxyMap"
    };

    private ArrayList<String> errors = new ArrayList<String>();

    @Test
    public void test() throws Exception {
        initClasses();

        clear();

        if (errors.size() > 0) {
            fail("Tomcat may clear the field above when reloading the web app: " + errors);
        }
        for (String s : KNOWN_REFRESHED) {
            String className = s.substring(0, s.lastIndexOf('.'));
            String fieldName = s.substring(s.lastIndexOf('.') + 1);
            Class<?> clazz = Class.forName(className);
            try {
                clazz.getDeclaredField(fieldName);
            } catch (Exception e) {
                fail(s);
            }
        }
    }

    private void initClasses() {
        List<String> list = new ArrayList<String>();
        CglibProxyFactory.getInstance().createProxy(list, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                return null;
            }
        });
        CompilingProxyFactory.getInstance().createProxy(list, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                return null;
            }
        });
        InterfaceProxyFactory.getInstance().createProxy(list, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                return null;
            }
        });
        ProxyFactory.getClassProxyFactory();
        ProxyFactory.getFactory(List.class);
    }

    private void clear() throws Exception {
        ArrayList<Class <?>> classes = new ArrayList<Class<?>>();

        classes.add(AssertThrows.class);

        classes.add(CglibProxyFactory.class);
        classes.add(Compiler.class);
        classes.add(CompilingProxyFactory.class);
        classes.add(CompilingProxyFactory.CodeGenerator.class);
        classes.add(InterfaceProxyFactory.class);
        classes.add(ProxyFactory.class);
        classes.add(ReflectionUtils.class);

        classes.add(ExceptionVerifier.class);
        classes.add(ResultVerifier.class);

        for (Class<?> clazz : classes) {
            clearClass(clazz);
        }
    }

    /**
     * This is how Tomcat resets the fields as of 2009-01-30.
     *
     * @param clazz the class to clear
     */
    private void clearClass(Class<?> clazz) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().isPrimitive() || field.getName().indexOf("$") != -1) {
                continue;
            }
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                continue;
            }
            field.setAccessible(true);
            Object o = field.get(null);
            if (o == null) {
                continue;
            }
            if (Modifier.isFinal(modifiers)) {
                if (field.getType().getName().startsWith("java.")) {
                    continue;
                }
                if (field.getType().getName().startsWith("javax.")) {
                    continue;
                }
                clearInstance(o);
            } else {
                clearField(clazz.getName() + "." + field.getName() + " = " + o);
            }
        }
    }

    private void clearInstance(Object instance) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.getType().isPrimitive() || (field.getName().indexOf("$") != -1)) {
                continue;
            }
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                continue;
            }
            field.setAccessible(true);
            Object o = field.get(instance);
            if (o == null) {
                continue;
            }
            // loadedByThisOrChild
            if (o.getClass().getName().startsWith("java.lang.")) {
                continue;
            }
            if (o.getClass().isArray() && o.getClass().getComponentType().isPrimitive()) {
                continue;
            }
            clearField(instance.getClass().getName() + "." + field.getName());
        }
    }

    private void clearField(String s) {
        for (String k : KNOWN_REFRESHED) {
            if (s.startsWith(k)) {
                return;
            }
        }
        errors.add(s);
    }

}
