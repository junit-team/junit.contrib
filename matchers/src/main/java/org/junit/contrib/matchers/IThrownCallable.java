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
 * This interface specifies post operations after examined block returned.
 * <p/>
 *
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsThrowing
 * @since 0.1, 19.2.2012, 21:45
 */
public interface IThrownCallable<V> {
    /**
     * Evaluates whether the block returned executed without throwing any exception.
     * @return {@code true} if block executed without throwing any exception
     */
    boolean isBlockReturnedNormally();

    /**
     * See {@link org.junit.contrib.matchers.Callable}.
     * @return a value returned by a callable block, or a fallback when callable block threw
     */
    V blockReturned();

    /**
     * See the parameter in {@linkplain org.junit.contrib.matchers.Callable constructor of Callable}.
     * @return a fallback of a callable block which returns
     */
    V fallback();
}
