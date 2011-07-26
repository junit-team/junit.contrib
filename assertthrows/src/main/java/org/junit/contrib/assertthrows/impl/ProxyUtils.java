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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility methods for proxy classes and invocation handlers.
 *
 * @author Thomas Mueller
 */
public class ProxyUtils {

    private ProxyUtils() {
        // utility class
    }

    /**
     * Get the default value for the given class. For non-primitive classes,
     * this is null, and for primitive classes this is the zero or false.
     *
     * @param clazz the class
     * @return false, zero, or null
     */
    public static Object getDefaultValue(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return null;
        }
        if (clazz == boolean.class) {
            return false;
        } else if (clazz == byte.class) {
            return (byte) 0;
        } else if (clazz == char.class) {
            return (char) 0;
        } else if (clazz == short.class) {
            return (short) 0;
        } else if (clazz == int.class) {
            return 0;
        } else if (clazz == long.class) {
            return 0L;
        } else if (clazz == float.class) {
            return 0F;
        } else if (clazz == double.class) {
            return 0D;
        }
        return null;
    }

    /**
     * Convert primitive class names to java.lang.* class names.
     *
     * @param clazz the class (for example: int)
     * @return the non-primitive class (for example: java.lang.Integer)
     */
    public static Class<?> getNonPrimitiveClass(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return clazz;
        } else if (clazz == boolean.class) {
            return Boolean.class;
        } else if (clazz == byte.class) {
            return Byte.class;
        } else if (clazz == char.class) {
            return Character.class;
        } else if (clazz == double.class) {
            return Double.class;
        } else if (clazz == float.class) {
            return Float.class;
        } else if (clazz == int.class) {
            return Integer.class;
        } else if (clazz == long.class) {
            return Long.class;
        } else if (clazz == short.class) {
            return Short.class;
        } else if (clazz == void.class) {
            return Void.class;
        }
        return clazz;
    }

    /**
     * Get the package name from a class. This works even if getPackage()
     * returns null.
     *
     * @param c the class
     * @return the package name (an empty string for primitive classes)
     */
    public static String getPackageName(Class<?> c) {
        Package p = c.getPackage();
        if (p != null) {
            return p.getName();
        }
        if (c.isPrimitive()) {
            return "";
        }
        String s = c.getName();
        int lastDot = s.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return s.substring(0, lastDot);
    }

    /**
     * Calls a static method via reflection. This will try to use the method
     * where the most parameter classes match exactly (this algorithm is simpler
     * than the one in the Java specification, but works well for most cases).
     *
     * @param classAndMethod a string with the entire class and method name, eg.
     *            "java.lang.System.gc"
     * @param params the method parameters
     * @return the return value from this call
     * @throws Exception if the method is unknown, or calling it throws an exception
     */
    public static Object callStaticMethod(
            String classAndMethod, Object... params) throws Exception {
        int lastDot = classAndMethod.lastIndexOf('.');
        String className = classAndMethod.substring(0, lastDot);
        String methodName = classAndMethod.substring(lastDot + 1);
        return classMethodInternal(methodName, Class.forName(className), null, params);
    }

    /**
     * Calls an instance method via reflection. This will try to use the method
     * where the most parameter classes match exactly (this algorithm is simpler
     * than the one in the Java specification, but works well for most cases).
     *
     * @param instance the instance on which the call is done
     * @param methodName a string with the method name
     * @param params the method parameters
     * @return the return value from this call
     * @throws Exception if the method is unknown, or calling it throws an exception
     */
    public static Object callMethod(
            Object instance,
            String methodName,
            Object... params) throws Exception {
        return classMethodInternal(methodName, instance.getClass(), instance, params);
    }

    private static Object classMethodInternal(
            String methodName, Class<?> clazz,
            Object instance,
            Object... params) throws Exception {
        Method best = null;
        int bestMatch = 0;
        boolean isStatic = instance == null;
        for (Method m : clazz.getMethods()) {
            if (Modifier.isStatic(m.getModifiers()) == isStatic &&
                    m.getName().equals(methodName)) {
                int p = match(m.getParameterTypes(), params);
                if (p > bestMatch) {
                    bestMatch = p;
                    best = m;
                }
            }
        }
        if (best == null) {
            throw new NoSuchMethodException(methodName);
        }
        return best.invoke(instance, params);
    }

    private static int match(Class<?>[] params, Object[] values) {
        int len = params.length;
        if (len == values.length) {
            int points = 1;
            for (int i = 0; i < len; i++) {
                Class<?> pc = getNonPrimitiveClass(params[i]);
                Object v = values[i];
                Class<?> vc = v == null ? null : v.getClass();
                if (pc == vc) {
                    points++;
                } else if (vc == null) {
                    // can't verify
                } else if (!pc.isAssignableFrom(vc)) {
                    return 0;
                }
            }
            return points;
        }
        return 0;
    }

}
