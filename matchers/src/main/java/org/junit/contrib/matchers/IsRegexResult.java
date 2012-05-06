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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Arrays;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.junit.contrib.matchers.ResultType.*;

/**
 * The purpose of this matcher is an use in {@link IsRegex regex matcher}.
 * Note: Any likely combination of <tt>start</tt> and <tt>group</tt> within combinatory
 * matchers (e.g. Hamcrest's <tt>allOf<//tt>) is not same as the matcher <tt>startByGroup</tt>.
 * <p/>
 * Same statement implies to <tt>end</tt> and <tt>group</tt> towards <tt>endByGroup</tt>.
 * <p/>
 * Note: Any likely combination of <tt>start</tt> and <tt>end</tt> within combinatory
 * matchers (e.g. Hamcrest's <tt>allOf<//tt>) is not same as the matcher <tt>region</tt> or <tt>regionByGroup</tt>.
 * <p/>
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsRegex
 * @since 0.1, 25.12.2011, 22:20
 */
public final class IsRegexResult<T extends Iterable<RegexResult>> extends TypeSafeMatcher<T> {
    static final int ANY_GROUP = -1;

    private final int group;
    private final ResultType resultType;
    private final Matcher<Integer>[] expectedResult;

    private String generalFault;

    private IsRegexResult(int group, ResultType resultType, Matcher<Integer>... expectedResult) {
        this.group = group;
        this.resultType = resultType;
        this.expectedResult = expectedResult;
    }

    private IsRegexResult(ResultType resultType, Matcher<Integer>... expectedResult) {
        this(ANY_GROUP, resultType, expectedResult);
    }

    @Override
    public boolean matchesSafely(T t) {
        switch(resultType) {
            case GROUP:
                for (final RegexResult regexResult : t)
                    if (expectedResult[0].matches(regexResult.getGroup()))
                        return true;
                break;
            case START:
                for (final RegexResult regexResult : t)
                    if ((group == ANY_GROUP || group == regexResult.getGroup())
                            && expectedResult[0].matches(regexResult.getStart()))
                        return true;
                break;
            case END:
                for (final RegexResult regexResult : t)
                    if ((group == ANY_GROUP || group == regexResult.getGroup())
                            && expectedResult[0].matches(regexResult.getEnd()))
                        return true;
                break;
            case REGION:
                for (final RegexResult regexResult : t)
                    if ((group == ANY_GROUP || group == regexResult.getGroup())
                            && expectedResult[0].matches(regexResult.getStart())
                            && expectedResult[1].matches(regexResult.getEnd()))
                        return true;
                break;
            default:
                generalFault = "expected " + resultType + " of " + Arrays.toString(ResultType.values());
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        if (generalFault == null) {
            switch (resultType) {
                case GROUP:
                    description.appendText(resultType.name().toLowerCase())
                            .appendText(" ")
                            .appendDescriptionOf(expectedResult[0]);
                    break;
                case START:
                    description.appendText(resultType.name().toLowerCase())
                            .appendText(" position ")
                            .appendDescriptionOf(expectedResult[0]);
                    if (group != ANY_GROUP)
                        description.appendText(" group ")
                                .appendText(Integer.toString(group));
                    break;
                case END:
                    description.appendText(resultType.name().toLowerCase())
                            .appendText(" position ")
                            .appendDescriptionOf(expectedResult[0]);
                    if (group != ANY_GROUP)
                        description.appendText(" group ")
                                .appendText(Integer.toString(group));
                    break;
                case REGION:
                    description.appendText(resultType.name().toLowerCase())
                            .appendText(" from ")
                            .appendDescriptionOf(expectedResult[0]);
                    description.appendText(" to ")
                            .appendDescriptionOf(expectedResult[1]);
                    if (group != ANY_GROUP)
                        description.appendText(" group ")
                                .appendText(Integer.toString(group));
                    break;
                default:
                    fail(" an internal error");
            }
        } else fail(generalFault);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> group(Matcher<Integer> group) {
        assertNotNull(group);
        return new IsRegexResult<T>(GROUP, group);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> group(int group) {
        return group(equalTo(group));
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> start(Matcher<Integer> start) {
        assertNotNull(start);
        return new IsRegexResult<T>(START, start);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> start(int start) {
        return start(equalTo(start));
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> startByGroup(int group, Matcher<Integer> start) {
        assertTrue("group " + group + " must be non-negative", group >= 0);
        assertNotNull(start);
        return new IsRegexResult<T>(group, START, start);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> startByGroup(int group, int start) {
        return startByGroup(group, equalTo(start));
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> end(Matcher<Integer> end) {
        assertNotNull(end);
        return new IsRegexResult<T>(END, end);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> end(int end) {
        return end(equalTo(end));
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> endByGroup(int group, Matcher<Integer> end) {
        assertTrue("group " + group + " must be non-negative", group >= 0);
        assertNotNull(end);
        return new IsRegexResult<T>(group, END, end);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> endByGroup(int group, int end) {
        return endByGroup(group, equalTo(end));
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> region(Matcher<Integer> start, Matcher<Integer> end) {
        assertNotNull(start);
        assertNotNull(end);
        return new IsRegexResult<T>(REGION, start, end);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> region(int start, int end) {
        return region(equalTo(start), equalTo(end));
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> regionByGroup(int group, Matcher<Integer> start, Matcher<Integer> end) {
        assertTrue("group " + group + " must be non-negative", group >= 0);
        assertNotNull(start);
        assertNotNull(end);
        return new IsRegexResult<T>(group, REGION, start, end);
    }

    @org.hamcrest.Factory
    public static <T extends Iterable<RegexResult>> Matcher<T> regionByGroup(int group, int start, int end) {
        return regionByGroup(group, equalTo(start), equalTo(end));
    }
}
