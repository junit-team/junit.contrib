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
package org.junit.contrib.assertthrows;

import java.lang.reflect.Method;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.contrib.assertthrows.proxy.ProxyFactory;
import org.junit.contrib.assertthrows.verify.ExceptionVerifier;
import org.junit.contrib.assertthrows.verify.ResultVerifier;

/**
 * Test a custom extension of the ResultVerifier. It specially tests that the
 * method is called again as long as the verify method returns true.
 *
 * @author Thomas Mueller
 */
public class ExtensionTest {

    protected Random random = new Random(0);

    @Test
    public void testAnonymousClass() {
        new AssertEventuallySucceeds() { public void test() {
            assertTrue(random.nextDouble() > 0.9);
        }};

        new AssertThrows() { public void test() {
            new AssertEventuallySucceeds() { public void test() {
                assertTrue(random.nextInt(10) == 10);
            }};
        }};
        Throwable e = AssertThrows.getLastThrown();
        assertEquals("Verification failed after 100 tries", e.getMessage());
    }

    @Test
    public void testProxy() {
        ProxyFactory.useClassProxyFactory(Random.class);
        assertEventuallyEquals(10, random).nextInt(20);
        assertEventuallySucceeds(this).failWithProbability(0.9);

        new AssertThrows() { public void test() {
            assertEventuallyEquals(20, random).nextInt(10);
        }};
        Throwable e = AssertThrows.getLastThrown();
        assertEquals("Verification failed after 100 tries", e.getMessage());
        new AssertThrows() { public void test() {
            assertEventuallySucceeds(ExtensionTest.this).failWithProbability(1.0);
        }};
        e = AssertThrows.getLastThrown();
        assertEquals("Verification failed after 100 tries", e.getMessage());
    }

    <T> T assertEventuallyEquals(final Object expected, T obj) {
        return ExceptionVerifier.createVerifyingProxy(new ResultVerifier() {

            private int count;

            public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
                if (count++ > 100) {
                    throw new AssertionError("Verification failed after 100 tries");
                }
                boolean equal;
                if (expected == null) {
                    equal = returnValue == null;
                } else {
                    equal = expected.equals(returnValue);
                }
                // try again if not equal
                return !equal;
            }

        }, obj);
    }

    <T> T assertEventuallySucceeds(T obj) {
        return ExceptionVerifier.createVerifyingProxy(
                new SucceedsEventuallyVerifier(), obj);
    }

    public void failWithProbability(double probability) {
        if (random.nextDouble() < probability) {
            throw new IllegalStateException();
        }
    }

}

/**
 * Verify that a given test is successful after at most 100 tries.
 */
abstract class AssertEventuallySucceeds extends AssertThrows {

    AssertEventuallySucceeds() {
        super(new SucceedsEventuallyVerifier());
    }

}

/**
 * Verifies that a no exception was thrown after at most 100 tries.
 */
class SucceedsEventuallyVerifier implements ResultVerifier {

    static final int MAX_TRIES = 100;

    private int count;

    public boolean verify(Object returnValue, Throwable t, Method m, Object... args) {
        if (t == null) {
            return false;
        }
        if (count++ < 100) {
            return true;
        }
        AssertionError ae = new AssertionError(
                "Verification failed after " + MAX_TRIES + " tries");
        ae.initCause(t);
        throw ae;
    }

}
