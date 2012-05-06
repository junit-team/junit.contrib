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

import org.hamcrest.BaseDescription;
import org.hamcrest.Matcher;

/**
 * The purpose of this class is an internal use in {@link IsThrowing}, when combining expected exception to throw by
 * a block of code together with a certain message matching the expectations of the matcher {@link IsThrowing}.
 * @author Tibor17
 * @version 0.1
 * @see IsThrowing
 * @since 0.1, Sep 28, 2011, 11:26:00 AM
 */
abstract class ThrownMessageMatcher {
    protected final String beforeNullActual, beforeActual, afterActual,
                            beforeExpected, afterExpected,
                            expectedMatcherPrefix;

    protected ThrownMessageMatcher(String beforeNullActual, String beforeActual, String afterActual,
                                   String beforeExpected, String afterExpected,
                                   String expectedMatcherPrefix) {
        this.beforeNullActual = beforeNullActual;
        this.beforeActual = beforeActual;
        this.afterActual = afterActual;
        this.beforeExpected = beforeExpected;
        this.afterExpected = afterExpected;
        this.expectedMatcherPrefix = expectedMatcherPrefix;
    }

    protected abstract boolean isMatching(Throwable thrown);
    protected abstract boolean hasNullMessage(Throwable thrown);
    protected abstract String getActualMessage(Throwable thrown);
    protected abstract Matcher<? extends CharSequence> getExpectedMatcher();
    protected abstract String getExpectedMessage();

    final String createErrorMessage(final Throwable thrown) {
        final StringBuilder msg = new StringBuilder(128);

        if (hasNullMessage(thrown)) msg.append(beforeNullActual);
        else msg.append(beforeActual).append(getActualMessage(thrown)).append(afterActual);
        msg.append(beforeExpected);

        final Matcher expectedMatcher = getExpectedMatcher();

        if (expectedMatcher != null) {
            final int insertAt = msg.length();
            final String shouldStartWith = expectedMatcherPrefix;
            final boolean[] startsThat = {false};

            expectedMatcher.describeTo(new BaseDescription() {
                private int i, upTo = shouldStartWith.length();
                @Override protected void append(char c) {
                    if (i < upTo && (i == 0 || startsThat[0]))
                        startsThat[0] = c == shouldStartWith.charAt(i);
                    ++i;
                    msg.append(c);
                }
                @Override public String toString() { return msg.toString(); }
            });

            if (!startsThat[0])
                msg.insert(insertAt, shouldStartWith);

        } else msg.append(getExpectedMessage());

        msg.append(afterExpected);

        return msg.toString();
    }
}