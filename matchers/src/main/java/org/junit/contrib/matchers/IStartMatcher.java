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

import org.hamcrest.Matcher;

/**
 * Specifies the start position of regular expression matches.
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsRegex
 * @since 0.1, 25.12.2011, 1:35
 */
public interface IStartMatcher<T extends CharSequence> extends Matcher<T> {
    /**
     * Matches the start index of the subsequence captured by the given group during the previous
     * <tt>match</tt> or <tt>like</tt> operation.
     * <p>
     * The integer matcher represents expected range of position. The first method parameter
     * specifies a group of given regular expression to search matching start position. If given
     * <tt>group</tt> is negative, then the {@link AssertionError} is thrown.
     * @param group a group in regular expression
     * @param index expected range of start position (inclusive)
     * @throws AssertionError
     * if given <tt>group</tt> is negative,
     * <tt>index</tt> is null
     * ; or if not applicable when result matcher already specified
     * @return opposite matcher
     * @see java.util.regex.Matcher#start(int)
     */
    IEndMatcher<T> startsAt(int group, Matcher<Integer> index);

    /**
     * Matches the start index of the subsequence captured during the previous
     * <tt>match</tt> or <tt>like</tt> operation.
     * <p>
     * The integer matcher represents expected range of start position with zero group.
     * @param index expected range of start position (inclusive)
     * @return opposite matcher
     * @throws AssertionError
     * <tt>index</tt> is null
     * ; or if not applicable when result matcher already specified
     */
    IEndMatcher<T> startsAt(Matcher<Integer> index);

    /**
     * Matches the start index of the subsequence, captured by the given group during the previous
     * <tt>match</tt> or <tt>like</tt> operation, equal to the given index.
     * <p>
     * The first method parameter specifies a group of given regular expression to search matching
     * start position. If given <tt>group</tt> is negative, then the {@link AssertionError} is thrown.
     * @param group a group in regular expression
     * @param index expected range of start position (inclusive)
     * @throws AssertionError
     * if given <tt>group</tt> is negative
     * ; or if not applicable when result matcher already specified
     * @return opposite matcher
     * @see java.util.regex.Matcher#start(int)
     */
    IEndMatcher<T> startsAt(int group, int index);

    /**
     * Matches the start index of the subsequence, captured during the previous
     * <tt>match</tt> or <tt>like</tt> operation, equal to the given index.
     * <p>
     * The integer matcher represents expected range of start position with zero group.
     * @param index expected range of start position (inclusive)
     * @return opposite matcher
     * @throws AssertionError
     * if not applicable when result matcher already specified
     */
    IEndMatcher<T> startsAt(int index);
}
