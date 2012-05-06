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
 * The skeleton class where the {@link #call()} must be implemented.
 * <p/>
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsThrowing
 * @since 0.1, 26.2.2012, 20:39
 */
public abstract class Callable<V> extends Block<V> implements java.util.concurrent.Callable<V> {
    public Callable() {
        super(true);
    }

    public Callable(V fallback) {
        super(true, fallback);
    }

    @Override final V call1() throws Exception {
        return call();
    }
}