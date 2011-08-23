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
package org.junit.contrib.assertthrows.verify;

import java.lang.reflect.Method;

/**
 * This handler is called after a method returned.
 *
 * @author Thomas Mueller
 */
public interface ResultVerifier {

    /**
     * Verify the result or exception of a method call.
     *
     * @param returnValue the returned value, or null if the method didn't
     *            return a value or if the returned value is unknown
     * @param t the exception / error or null if the method returned normally
     * @param m the method or null if unknown
     * @param args the arguments or null if unknown
     * @return true if the method should be called again
     * @throws AssertionError if the verification failed
     */
    boolean verify(Object returnValue, Throwable t, Method m, Object... args);

}
