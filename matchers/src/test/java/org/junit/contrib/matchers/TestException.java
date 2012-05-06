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

import org.junit.Ignore;

/**
 * This is test exception used rather than nested class which naming is totally compiler specific.
 * <p/>
 *
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsRegex
 * @since 0.1, 24.3.2012, 22:53
 */
@Ignore
class TestException extends Exception {
    private final String localizedMessage;

    TestException(String message, String localizedMessage) {
        super(message);
        this.localizedMessage = localizedMessage;
    }

    TestException(String message) {
        super(message);
        localizedMessage = message;
    }

    TestException(String message, Throwable cause) {
        super(message, cause);
        localizedMessage = message;
    }

    TestException(Throwable cause) {
        super(cause);
        localizedMessage = getMessage();
    }

    public
    @Override String getLocalizedMessage() { return localizedMessage; }
}
