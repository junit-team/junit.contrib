/*
 * The copyright holders of this work license this file to You under
 * the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.junit.contrib.matchers;

/**
 * A skeleton class for {@link Block} and {@link Callable}.
 * <p/>
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsThrowing
 * @since 0.1, 26.2.2012, 20:37
 */
public abstract class AbstractCallable<V> {
    private final boolean hasReturnValue;
    private V returned;

    final V fallback;
    boolean isSucceeded;
    Throwable throwable;

    AbstractCallable(boolean hasReturnValue) {
        this.hasReturnValue = hasReturnValue;
        fallback = null;
    }

    AbstractCallable(boolean hasReturnValue, V fallback) {
        this.hasReturnValue = hasReturnValue;
        this.fallback = fallback;
    }

    abstract V call1() throws Exception;

    final V evaluate() {
        isSucceeded = true;
        try {
            returned = call1();
            return returned;
        } catch (final Throwable throwable) {
            isSucceeded = false;
            this.throwable = throwable;
            return fallback;
        }
    }

    @Override final public String toString() {
        return isSucceeded
                ? ("block returned " + (hasReturnValue ? (returned == null ? "null" : returned.toString()) : "normally"))
                : ("block threw " + throwable.toString());
    }
}