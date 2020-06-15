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

import static org.junit.contrib.matchers.IsRegexResult.ANY_GROUP;

/**
 * The purpose of this immutable object is to wrap information (group, start, end)
 * of successful match after using a regular expression on an input (sub)sequence.
 * <p/>
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsRegexResult
 * @since 0.1, 26.12.2011, 11:21
 */
final public class RegexResult {
    /**
     * Results.
     */
    private final int group, start, end;

    /**
     * Encapsulates a region of match returned by methods {@link java.util.regex.Matcher#start(int)} and
     * {@link java.util.regex.Matcher#end(int)} per <tt>group</tt> which is <tt>-1</tt> or within the
     * range 0 and {@link java.util.regex.Matcher#groupCount()}.
     * <p>
     * If the group is <tt>-1</tt>, then any group of group's range is possibly used for comparison in
     * {@link IsRegexResult} Hamcrest matcher.
     * @param group group for start or end operation (must be non-negative or <tt>-1</tt>)
     * @param start
     * start index (must be non-negative) of the subsequence captured by the given group on a match operation
     * @param end
     * end index (must be non-negative) of the subsequence captured by the given group on a match operation
     * @throws IllegalArgumentException
     * if <tt>group</tt> is less than <tt>-1</tt>;
     * or if <tt>stat</tt> or <tt>end</tt> is negative
     */
    RegexResult(int group, int start, int end) {
        if (group < ANY_GROUP) throw new IllegalArgumentException("group is " + group);
        if (start < 0) throw new IllegalArgumentException("start is " + start);
        if (end < 0) throw new IllegalArgumentException("end is " + end);
        this.group = group;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the value passed in by the constructor.
     * @return group. Must be non-negative number when passed in by {@link #RegexResult(int, int, int)} constructor}
     * ; otherwise <tt>-1</tt> which appears any group when passed in by
     * {@link #RegexResult(int, int, int)}.
     */
    public int getGroup() {
        return group;
    }

    /**
     * Returns the value passed in by the constructor.
     * @return (a non-negative number of) start operation
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the value passed in by the constructor.
     * @return (a non-negative number of) end operation
     */
    public int getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        return start ^ end ^ group;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof RegexResult)) return false;
        final RegexResult rr = (RegexResult) o;
        return rr.start == start && rr.end == end && rr.group == group;
    }
}
