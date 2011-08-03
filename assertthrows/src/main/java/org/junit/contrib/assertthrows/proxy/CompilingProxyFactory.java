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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A generator for classes that extend another class (class proxies).
 *
 * @author Thomas Mueller
 */
public class CompilingProxyFactory extends ProxyFactory {

    /**
     * The compiler to use. Implementation note: this field might be set
     * to null by Tomcat when unloading a web application that uses this proxy
     * factory.
     */
    private Compiler compiler = new Compiler();

    /**
     * A cache of compiled classes. Implementation note: this field might be set
     * to null by Tomcat when unloading a web application that uses this proxy
     * factory.
     */
    private HashMap<Class<?>, Class<?>> proxyMap = new HashMap<Class<?>, Class<?>>();

    private boolean useSystemJavaCompiler = true;

    @SuppressWarnings("unchecked")
    public <T> T createProxy(T obj, final InvocationHandler handler) {
        Class<?> c = obj.getClass();
        Class<?> pc = getClassProxy(c);
        Constructor<?> cons;
        try {
            cons = pc.getConstructor(new Class<?>[] { InvocationHandler.class });
            return (T) cons.newInstance(new Object[] { handler });
        } catch (Exception e) {
            IllegalArgumentException ia = new IllegalArgumentException(
                    "Could not create a new instance of the class " +
                    pc.getName());
            ia.initCause(e);
            throw ia;
        }
    }

    public void setUseSystemJavaCompiler(boolean useSystemJavaCompiler) {
        this.useSystemJavaCompiler = useSystemJavaCompiler;
        getCompiler().setUseSystemJavaCompiler(useSystemJavaCompiler);
    }

    private Compiler getCompiler() {
        if (compiler == null) {
            compiler = new Compiler();
            compiler.setUseSystemJavaCompiler(useSystemJavaCompiler);
        }
        return compiler;
    }

    /**
     * Generate a proxy class. The returned class extends the given class.
     *
     * @param c the class to extend
     * @return the proxy class
     * @throws IllegalArgumentException if it was not possible to create a proxy
     *             for the passed class
     */
    public Class<?> getClassProxy(Class<?> c) throws IllegalArgumentException {
        HashMap<Class<?>, Class<?>> map = getProxyMap();
        Class<?> p = map.get(c);
        if (p != null) {
            return p;
        }
        if (c.isAnonymousClass()) {
            throw new IllegalArgumentException(
                    "Creating a proxy for an anonymous inner class " +
                    "is not supported: " + c.getName());
        }
        if (Modifier.isFinal(c.getModifiers())) {
            throw new IllegalArgumentException(
                    "Creating a proxy for a final class " +
                    "is not supported: " + c.getName());
        }
        if (c.getEnclosingClass() != null) {
            if (!Modifier.isPublic(c.getModifiers())) {
                throw new IllegalArgumentException(
                        "Creating a proxy for a non-public inner class " +
                        "is not supported: " + c.getName());
            } else if (!Modifier.isStatic(c.getModifiers())) {
                // in theory, it would be possible to support it,
                // but the outer class would also need to be extended
                throw new IllegalArgumentException(
                        "Creating a proxy for a non-static inner class " +
                        "is not supported: " + c.getName());
            }
            boolean hasPublicConstructor = false;
            for (Constructor<?> constructor : c.getConstructors()) {
                if (Modifier.isPublic(constructor.getModifiers())) {
                    hasPublicConstructor = true;
                    break;
                }
            }
            if (!hasPublicConstructor) {
                throw new IllegalArgumentException(
                        "Creating a proxy for a static inner class " +
                        "without public constructor is not supported: " + c.getName());
            }
        }
        CodeGenerator gen = new CodeGenerator();
        String packageName = ReflectionUtils.getPackageName(c);
        packageName = ProxyFactory.PROXY_PACKAGE_NAME + "." + packageName;
        String className = c.getSimpleName() + "Proxy";
        gen.setName(packageName, className);
        gen.generateClassProxy(c);
        StringWriter sw = new StringWriter();
        gen.write(new PrintWriter(sw));
        String code = sw.toString();
        String name = packageName + "." + className;
        Compiler comp = getCompiler();
        comp.setSource(name, code);
        // System.out.println(code);
        try {
            Class<?> pc = comp.getClass(name);
            map.put(c, pc);
            return pc;
        } catch (ClassNotFoundException e) {
            IllegalArgumentException ia = new IllegalArgumentException(
                    "Could not create a proxy class for " + c.getName());
            ia.initCause(e);
            throw ia;
        }
    }

    public HashMap<Class<?>, Class<?>> getProxyMap() {
        if (proxyMap == null) {
            proxyMap = new HashMap<Class<?>, Class<?>>();
        }
        return proxyMap;
    }

    /**
     * Get a unique field name for the given class. The preferred field name is
     * used if possible, but this field is already declared in this class or any
     * superclass, then a new field name is generated.
     *
     * @param c the class
     * @param preferredFieldName the preferred field name
     * @return a unique field name
     */
    public static String getUniqueFieldName(Class<?> c, String preferredFieldName) {
        if (!hasField(c, preferredFieldName)) {
            return preferredFieldName;
        }
        for (int i = 0;; i++) {
            String name = preferredFieldName + i;
            if (!hasField(c, name)) {
                return name;
            }
        }
    }

    /**
     * Check whether the given class knows the given field, that is, if the
     * field is protected or public in the class or one of its superclasses.
     *
     * @param c the class
     * @param fieldName the field name
     * @return true if the field is known
     */
    public static boolean hasField(Class<?> c, String fieldName) {
        do {
            for (Field f : c.getDeclaredFields()) {
                if (f.getName().equals(fieldName)) {
                    return true;
                }
            }
            c = c.getSuperclass();
        } while (c != null);
        return false;
    }

    /**
     * A Java source code generator for class proxies.
     *
     * @author Thomas Mueller
     */
    public static class CodeGenerator {

        private final TreeSet<String> imports = new TreeSet<String>();
        private final TreeMap<String, Method> methods = new TreeMap<String, Method>();
        private final HashSet<String> overriddenMethods = new HashSet<String>();
        private String packageName;
        private String className;
        private Class<?> extendsClass;
        private Constructor<?> constructor;
        private String invocationHandlerFieldName = "ih";

        void setName(String packageName, String className) {
            this.packageName = packageName;
            this.className = className;
        }

        void generateClassProxy(Class<?> clazz) {
            imports.clear();
            addImport(InvocationHandler.class);
            addImport(Method.class);
            addImport(clazz);
            invocationHandlerFieldName = getUniqueFieldName(clazz, invocationHandlerFieldName);
            extendsClass = clazz;
            int doNotOverride = Modifier.FINAL | Modifier.STATIC |
                    Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.VOLATILE;
            Class<?> dc = clazz;

            while (dc != null) {
                addImport(dc);
                for (Method m : dc.getDeclaredMethods()) {
                    if ((m.getModifiers() & doNotOverride) == 0) {
                        addMethod(m);
                    } else if (Modifier.isVolatile(m.getModifiers())) {
                        // there is a bridge method, so it's overridden
                        addOverriddenMethod(m);
                    }
                }
                dc = dc.getSuperclass();
            }
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                if (Modifier.isPrivate(c.getModifiers())) {
                    continue;
                }
                if (constructor == null) {
                    constructor = c;
                } else if (c.getParameterTypes().length < constructor.getParameterTypes().length) {
                    constructor = c;
                }
            }
        }

        private void addOverriddenMethod(Method m) {
            String methodKey = getMethodKey(m);
            overriddenMethods.add(methodKey);
        }

        private void addMethod(Method m) {
            String methodKey = getMethodKey(m);
            if (methods.containsKey(methodKey)) {
                // already declared in a subclass
                return;
            }
            if (overriddenMethods.contains(methodKey)) {
                // already overridden (using a bridge method)
                return;
            }
            addImport(m.getReturnType());
            for (Class<?> c : m.getParameterTypes()) {
                addImport(c);
            }
            for (Class<?> c : m.getExceptionTypes()) {
                addImport(c);
            }
            methods.put(methodKey, m);
        }

        private String getMethodKey(Method m) {
            StringBuilder buff = new StringBuilder();
            buff.append(m.getReturnType()).append(' ');
            buff.append(m.getName());
            for (Class<?> p : m.getParameterTypes()) {
                buff.append(' ');
                buff.append(p.getName());
            }
            return buff.toString();
        }

        private void addImport(Class<?> c) {
            while (c.isArray()) {
                c = c.getComponentType();
            }
            if (!c.isPrimitive()) {
                if (!"java.lang".equals(ReflectionUtils.getPackageName(c))) {
                    String className;
                    if (c.getEnclosingClass() != null) {
                        className = getClassName(c.getEnclosingClass());
                    } else {
                        className = getClassName(c);
                    }
                    imports.add(ReflectionUtils.getPackageName(c) + "." + className);
                }
            }
        }

        private static String getClassName(Class<?> c) {
            return getClassName(c, false);
        }

        private static String getClassName(Class<?> c, boolean varArg) {
            if (varArg) {
                c = c.getComponentType();
            }
            String s = c.getSimpleName();
            while (true) {
                c = c.getEnclosingClass();
                if (c == null) {
                    break;
                }
                s = c.getSimpleName() + "." + s;
            }
            if (varArg) {
                return s + "...";
            }
            return s;
        }

        void write(PrintWriter writer) {
            if (packageName != null) {
                writer.println("package " + packageName + ";");
            }
            for (String imp : imports) {
                writer.println("import " + imp + ";");
            }
            writer.print("public class " + className);
            if (extendsClass != null) {
                writer.print(" extends " + getClassName(extendsClass));
            }
            writer.println(" {");
            writer.print("    private final InvocationHandler ");
            writer.print(invocationHandlerFieldName);
            writer.println(";");

            writer.println("    public " + className + "() {");
            writer.println("        this(new InvocationHandler() {");
            writer.println("            public Object invoke(Object proxy,");
            writer.println("                    Method method, Object[] args) throws Throwable {");
            writer.println("                return method.invoke(proxy, args);");
            writer.println("            }});");
            writer.println("    }");
            writer.println("    public " + className + "(InvocationHandler ih) {");
            if (constructor != null) {
                writer.print("        super(");
                int i = 0;
                for (Class<?> p : constructor.getParameterTypes()) {
                    if (i > 0) {
                        writer.print(", ");
                    }
                    if (p.isPrimitive()) {
                        if (p == boolean.class) {
                            writer.print("false");
                        } else if (p == byte.class) {
                            writer.print("(byte) 0");
                        } else if (p == char.class) {
                            writer.print("(char) 0");
                        } else if (p == short.class) {
                            writer.print("(short) 0");
                        } else if (p == int.class) {
                            writer.print("0");
                        } else if (p == long.class) {
                            writer.print("0L");
                        } else if (p == float.class) {
                            writer.print("0F");
                        } else if (p == double.class) {
                            writer.print("0D");
                        }
                    } else {
                        writer.print("null");
                    }
                    i++;
                }
                writer.println(");");
            }
            writer.print("        this.");
            writer.print(invocationHandlerFieldName);
            writer.println(" = ih;");
            writer.println("    }");
            writer.println("    @SuppressWarnings(\"unchecked\")");
            writer.println("    private static <T extends RuntimeException> T " +
                    "convertException(Throwable e) {");
            writer.println("        if (e instanceof Error) {");
            writer.println("            throw (Error) e;");
            writer.println("        }");
            writer.println("        return (T) e;");
            writer.println("    }");
            for (Method m : methods.values()) {
                Class<?> retClass = m.getReturnType();
                writer.print("    ");
                if (Modifier.isProtected(m.getModifiers())) {
                    // 'public' would also work
                    writer.print("protected ");
                } else {
                    writer.print("public ");
                }
                writer.print(getClassName(retClass) +
                    " " + m.getName() + "(");
                Class<?>[] pc = m.getParameterTypes();
                for (int i = 0; i < pc.length; i++) {
                    Class<?> p = pc[i];
                    if (i > 0) {
                        writer.print(", ");
                    }
                    boolean varArg = i == pc.length - 1 && m.isVarArgs();
                    writer.print(getClassName(p, varArg) + " p" + i);
                }
                writer.print(")");
                Class<?>[] ec = m.getExceptionTypes();
                writer.print(" throws RuntimeException");
                if (ec.length > 0) {
                    for (Class<?> e : ec) {
                        writer.print(", ");
                        writer.print(getClassName(e));
                    }
                }
                writer.println(" {");
                writer.println("        try {");
                writer.print("            if (");
                writer.print(invocationHandlerFieldName);
                writer.println(" == null) {");
                writer.print("                ");
                if (retClass != void.class) {
                    writer.print("return ");
                }
                writer.print("super.");
                writer.print(m.getName());
                writer.print("(");
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    if (i > 0) {
                        writer.print(", ");
                    }
                    writer.print("p" + i);
                }
                writer.println(");");
                writer.println("            } else {");
                if (retClass != void.class) {
                    writer.print("return (");
                    if (retClass == boolean.class) {
                        writer.print("Boolean");
                    } else if (retClass == byte.class) {
                        writer.print("Byte");
                    } else if (retClass == char.class) {
                        writer.print("Character");
                    } else if (retClass == short.class) {
                        writer.print("Short");
                    } else if (retClass == int.class) {
                        writer.print("Integer");
                    } else if (retClass == long.class) {
                        writer.print("Long");
                    } else if (retClass == float.class) {
                        writer.print("Float");
                    } else if (retClass == double.class) {
                        writer.print("Double");
                    } else {
                        writer.print(getClassName(retClass));
                    }
                    writer.print(") ");
                }
                writer.print(invocationHandlerFieldName);
                writer.print(".invoke(this, ");
                writer.println(getClassName(m.getDeclaringClass()) +
                        ".class.getDeclaredMethod(\"" + m.getName() +
                        "\",");
                writer.print("                new Class[] {");
                int i = 0;
                for (Class<?> p : m.getParameterTypes()) {
                    if (i > 0) {
                        writer.print(", ");
                    }
                    writer.print(getClassName(p) + ".class");
                    i++;
                }
                writer.println("}),");
                writer.print("                new Object[] {");
                for (i = 0; i < m.getParameterTypes().length; i++) {
                    if (i > 0) {
                        writer.print(", ");
                    }
                    writer.print("p" + i);
                }
                writer.println("});");
                writer.println("            }");
                writer.println("        } catch (Throwable e) {");
                writer.println("            throw convertException(e);");
                writer.println("        }");
                writer.println("    }");
            }
            writer.println("}");
            writer.flush();
        }

    }

}


