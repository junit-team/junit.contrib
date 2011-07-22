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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * This class allows to convert source code to a class. It uses one class loader
 * per class, and internally uses <code>com.sun.tools.javac.Main</code>.
 *
 * @author Thomas Mueller
 */
public class SourceCompiler {

    private static final Class<?> JAVAC_SUN;

    /**
     * The class name to source code map.
     */
    HashMap<String, String> sources = new HashMap<String, String>();

    /**
     * The class name to byte code map.
     */
    HashMap<String, Class<?>> compiled = new HashMap<String, Class<?>>();

    private String compileDir = System.getProperty("java.io.tmpdir", ".");

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("com.sun.tools.javac.Main");
        } catch (Exception e) {
            clazz = null;
        }
        JAVAC_SUN = clazz;
    }

    /**
     * Set the source code for the specified class.
     * This will reset all compiled classes.
     *
     * @param className the class name
     * @param source the source code
     */
    public void setSource(String className, String source) {
        sources.put(className, source);
        compiled.clear();
    }

    /**
     * Get the class object for the given name.
     *
     * @param packageAndClassName the class name
     * @return the class
     */
    public Class<?> getClass(String packageAndClassName) throws ClassNotFoundException {

        Class<?> compiledClass = compiled.get(packageAndClassName);
        if (compiledClass != null) {
            return compiledClass;
        }

        ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
            public Class<?> findClass(String name) throws ClassNotFoundException {
                Class<?> classInstance = compiled.get(name);
                if (classInstance == null) {
                    String source = sources.get(name);
                    String packageName = null;
                    int idx = name.lastIndexOf('.');
                    String className;
                    if (idx >= 0) {
                        packageName = name.substring(0, idx);
                        className = name.substring(idx + 1);
                    } else {
                        className = name;
                    }
                    byte[] data;
                    try {
                        data = javacCompile(packageName, className, source);
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Could not compile class " + className + ": " + e.getMessage(), e);
                    }
                    if (data == null) {
                        classInstance = findSystemClass(name);
                    } else {
                        classInstance = defineClass(name, data, 0, data.length);
                        compiled.put(name, classInstance);
                    }
                }
                return classInstance;
            }
        };
        return classLoader.loadClass(packageAndClassName);
    }

    /**
     * Get the first public static method of the given class.
     *
     * @param className the class name
     * @return the method name
     */
    public Method getMethod(String className) throws ClassNotFoundException {
        Class<?> clazz = getClass(className);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            int modifiers = m.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                if (Modifier.isStatic(modifiers)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Compile the given class. This method tries to use the class
     * "com.sun.tools.javac.Main" if available. If not, it tries to run "javac"
     * in a separate process.
     *
     * @param packageName the package name
     * @param className the class name
     * @param source the source code
     * @return the class file
     */
    byte[] javacCompile(String packageName, String className, String source) throws IOException {
        File dir = new File(compileDir);
        if (packageName != null) {
            dir = new File(dir, packageName.replace('.', '/'));
            dir.mkdirs();
        }
        File javaFile = new File(dir, className + ".java");
        File classFile = new File(dir, className + ".class");
        try {
            FileWriter f = new FileWriter(javaFile.getAbsolutePath(), false);
            PrintWriter out = new PrintWriter(new BufferedWriter(f));
            classFile.delete();
            if (source.startsWith("package ")) {
                out.println(source);
            } else {
                int endImport = source.indexOf("@CODE");
                String importCode = "import java.util.*;\n" +
                    "import java.math.*;\n" +
                    "import java.sql.*;\n";
                if (endImport >= 0) {
                    importCode = source.substring(0, endImport);
                    source = source.substring("@CODE".length() + endImport);
                }
                if (packageName != null) {
                    out.println("package " + packageName + ";");
                }
                out.println(importCode);
                out.println("public class "+ className +" {\n" +
                        "    public static " +
                        source + "\n" +
                        "}\n");
            }
            out.close();
            if (JAVAC_SUN != null) {
                javacSun(javaFile);
            }
            byte[] data = new byte[(int) classFile.length()];
            DataInputStream in = new DataInputStream(new FileInputStream(classFile));
            in.readFully(data);
            in.close();
            return data;
        } finally {
            javaFile.delete();
            classFile.delete();
        }
    }

    private void throwSyntaxError(ByteArrayOutputStream out) throws IOException {
        String err = new String(out.toByteArray(), "UTF-8");
        if (err.startsWith("Note:")) {
            // unchecked or unsafe operations - just a warning
        } else if (err.length() > 0) {
            throw new IOException(err);
        }
    }

    private void javacSun(File javaFile) throws IOException {
        PrintStream old = System.err;
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        PrintStream temp = new PrintStream(buff);
        try {
            System.setErr(temp);
            Method compile;
            compile = JAVAC_SUN.getMethod("compile", String[].class);
            Object javac = JAVAC_SUN.newInstance();
            compile.invoke(javac, (Object) new String[] {
                    "-sourcepath", compileDir,
                    "-d", compileDir,
                    "-encoding", "UTF-8",
                    javaFile.getAbsolutePath() });
            throwSyntaxError(buff);
        } catch (Exception e) {
            throw new IOException("Error compling " + javaFile.getAbsolutePath() + ": " + e.getMessage(), e);
        } finally {
            System.setErr(old);
        }
    }

}
