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
package org.junit.contrib.assertthrows.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * A generator for classes that extend another class (class proxies).
 *
 * @author Thomas Mueller
 */
public class ProxyClassGenerator {

    private static SourceCompiler compiler = new SourceCompiler();

    private static HashMap<Class<?>, Class<?>> proxyMap = new HashMap<Class<?>, Class<?>>();

    /**
     * Check whether there is already a proxy class generated.
     *
     * @param c the class
     * @return true if yes
     */
    public static boolean isGenerated(Class<?> c) {
        return proxyMap.containsKey(c);
    }

    /**
     * Generate a proxy class. The returned class extends the given class.
     *
     * @param c the class to extend
     * @return the proxy class
     * @throws IllegalArgumentException if it was not possible to create a proxy
     *             for the passed class
     */
    public static Class<?> getClassProxy(Class<?> c) throws IllegalArgumentException {
        Class<?> p = proxyMap.get(c);
        if (p != null) {
            return p;
        }
        if (c.isAnonymousClass()) {
            throw new IllegalArgumentException(
                    "Creating a proxy for the anonymous inner class " +
                    "is not supported: " + c.getName());
        }
        if (c.getEnclosingClass() != null && !Modifier.isPublic(c.getModifiers())) {
            throw new IllegalArgumentException(
                    "Creating a proxy for a non-public inner class " +
                    "is not supported: " + c.getName());
        }
        ProxyCodeGenerator cg = new ProxyCodeGenerator();
        String packageName = ProxyUtils.getPackageName(c);
        if (packageName.startsWith("java.")) {
            packageName = "proxy." + packageName;
        }
        String className = c.getSimpleName() + "Proxy";
        cg.setName(packageName, className);
        cg.generateClassProxy(c);
        StringWriter sw = new StringWriter();
        cg.write(new PrintWriter(sw));
        String code = sw.toString();
        String name = packageName + "." + className;
        compiler.setSource(name, code);
        // System.out.println(code);
        try {
            Class<?> pc = compiler.getClass(name);
            proxyMap.put(c, pc);
            return pc;
        } catch (ClassNotFoundException e) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Could not create a proxy class for " + c.getName());
            iae.initCause(e);
            throw iae;
        }
    }

}
