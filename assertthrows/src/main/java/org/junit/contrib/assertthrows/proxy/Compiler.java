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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class allows to convert source code to a class. It uses one class loader
 * per class, and internally uses <code>javax.tools.JavaCompiler</code> if
 * available, and <code>com.sun.tools.javac.Main</code> otherwise.
 *
 * @author Thomas Mueller
 */
public class Compiler {

    private static final Class<?> JAVAC_SUN;

    // private static final JavaCompiler JAVA_COMPILER;
    private static final Object JAVA_COMPILER;

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
        Object comp;
        try {
            // comp = ToolProvider.getSystemJavaCompiler();
            comp = ReflectionUtils.callStaticMethod(
                    "javax.tools.ToolProvider.getSystemJavaCompiler");
        } catch (Exception e) {
            comp = null;
        }
        JAVA_COMPILER = comp;
        Class<?> javac;
        try {
            javac = Class.forName("com.sun.tools.javac.Main");
        } catch (Exception e) {
            javac = null;
        }
        JAVAC_SUN = javac;
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
     * @throws ClassNotFoundException if the class is not found or can't be compiled
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
                        throw new ClassNotFoundException(
                                "Could not compile class " + className + ": " + e.getMessage(), e);
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
        classFile.delete();
        try {
            FileWriter f = new FileWriter(javaFile.getAbsolutePath(), false);
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(f));
                out.println(source);
                out.close();
            } finally {
                f.close();
            }
            try {
                if (JAVA_COMPILER != null) {
                    javaxToolsJavac(javaFile);
                } else if (JAVAC_SUN != null) {
                    javacSun(javaFile);
                } else {
                    throw new IOException("Could not load a java compiler");
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                IOException io = new IOException("Error compling " +
                        javaFile.getAbsolutePath() + ": " + e.getMessage());
                io.initCause(e);
                throw io;
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

    private void javaxToolsJavac(File javaFile) throws Exception {

        StringWriter writer = new StringWriter();

        Iterable<String> options = Arrays.asList(
                "-sourcepath", compileDir,
                "-d", compileDir,
                "-encoding", "UTF-8");

        // JavaCompiler compiler = (JavaCompiler) JAVA_COMPILER;
        Object compiler = JAVA_COMPILER;

        // StandardJavaFileManager fileManager = compiler.
        //         getStandardFileManager(null, null, Charset.forName("UTF-8"));
        Object fileManager = ReflectionUtils.callMethod(compiler, "getStandardFileManager",
                null, null, Charset.forName("UTF-8"));

        // Iterable<? extends JavaFileObject> compilationUnits =
        //     fileManager.getJavaFileObjects(javaFile);
        Object compilationUnits = ReflectionUtils.callMethod(fileManager, "getJavaFileObjects",
                (Object) new File[] { javaFile });

        // Task task = compiler.getTask(
        //         writer, fileManager, null, options, null, compilationUnits);
        Object task = ReflectionUtils.callMethod(compiler, "getTask",
                writer, fileManager, null, options, null, compilationUnits);

        // task.call();
        ReflectionUtils.callMethod(task, "call");

        // fileManager.close();
        ReflectionUtils.callMethod(fileManager, "close");

        String err = writer.toString();
        throwSyntaxError(err);
    }

    private void javacSun(File javaFile) throws Exception {
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
            String err = new String(buff.toByteArray(), "UTF-8");
            throwSyntaxError(err);
        } finally {
            System.setErr(old);
        }
    }

    private void throwSyntaxError(String err) throws IOException {
        if (err.startsWith("Note:")) {
            // unchecked or unsafe operations - just a warning
        } else if (err.length() > 0) {
            throw new IOException(err);
        }
    }

}
