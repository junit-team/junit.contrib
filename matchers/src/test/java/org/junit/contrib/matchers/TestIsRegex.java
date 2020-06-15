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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.contrib.matchers.IsRegex.like;
import static org.junit.contrib.matchers.IsRegex.match;
import static org.junit.contrib.matchers.IsRegexResult.*;
import static org.junit.contrib.matchers.MatchFlag.*;

/**
 * The purpose of this test is to test a functionality of
 * {@link IsRegex} matcher and to explain the use.
 * <p/>
 * @author Tibor17
 * @version 0.1
 * @see IsRegex
 * @since 0.1, Oct 11, 2011, 7:24:21 PM
 */
public final class TestIsRegex {
    @Rule
    public final ExpectedException expectedExceptionRule = ExpectedException.none();

    @Test
    public void baseLike$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"hello|hi\"\n" +
                "     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("hello|hi")));
    }

    @Test
    public void baseLike() {
        assertThat("Hi There!", is(like("Hello|Hi")));
    }

    @Test
    public void likeCaseInsensitive$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"hello|hi\"\n" +
                "     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties())));
    }

    @Test
    public void likeCaseInsensitive() {
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE))));
    }

    @Test
    public void likeCaseInsensitiveStart$1$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"(?i)hello|hi\" " +
                "starts at <1>\n" +
                "     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE)).startsAt(1)));
    }

    @Test
    public void likeCaseInsensitiveStart$1() {
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE)).startsAt(0)));
    }

    @Test
    public void likeCaseInsensitiveEnd$1$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"(?i)hello|hi\" " +
                "ends at <1>\n     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE)).endsAt(1)));
    }

    @Test
    public void likeCaseInsensitiveEnd$1() {
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE)).endsAt(2)));
    }

    @Test
    public void likeCaseInsensitiveStartEnd$1$NegativeTest1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"(?i)Hello|Hi\" " +
                "starts at <1> and ends at <1>\n" +
                "     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("Hello|Hi", new RegexProperties(CASE_INSENSITIVE)).startsAt(1).endsAt(1)));
    }

    @Test
    public void likeCaseInsensitiveStartEnd$1$NegativeTest2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("not applicable when result matcher already specified");
        assertThat("Hi There!", is(like("Hello|Hi", new RegexProperties(CASE_INSENSITIVE)).startsAt(0).endsAt(2).startsAt(0)));
    }

    @Test
    public void likeCaseInsensitiveStartEnd$1() {
        assertThat("Hi There!", is(like("Hello|Hi", new RegexProperties(CASE_INSENSITIVE)).startsAt(0).endsAt(2)));
    }

    @Test
    public void likeLogicalOR() {
        assertThat("Hi There!\nHello ...\nWhat's up!", is(like("What's up|Hello|Hi")));
    }

    @Test
    public void likeLogicalOR$NegativeTest$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"What's up|Hello|Hi\" " +
                "starts at <11> and ends at (<28> or <16>)\n" +
                "     got: \"Hi There!\\nHello ...\\nWhat's up!\"");

        assertThat("Hi There!\nHello ...\nWhat's up!",
                is(like("What's up|Hello|Hi")
                        .startsAt(equalTo(11))
                        .endsAt(anyOf(equalTo(28), equalTo(16)))));
    }

    @Test
    public void likeLogicalOR$NegativeTest$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"What's up|Hello|Hi\" " +
                "ends at (<28> or <16>)\n" +
                "     got: \"Hi There!\\nHello ...\\nWhat's up!\"");

        assertThat("Hi There!\nHello ...\nWhat's up!",
                is(like("What's up|Hello|Hi")
                        .startsAt(equalTo(10))
                        .endsAt(anyOf(equalTo(28), equalTo(16)))));
    }

    @Test
    public void likeLogicalOR$NegativeTest$3() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"What's up|Hello|Hi\" " +
                "ends at (<28> or <16>)\n" +
                "     got: \"Hi There!\\nHello ...\\nWhat's up!\"");

        assertThat("Hi There!\nHello ...\nWhat's up!",
                is(like("What's up|Hello|Hi")
                        .endsAt(anyOf(equalTo(28), equalTo(16)))));
    }

    @Test
    public void likeLogicalOR$NegativeTest$4() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"What's up|Hello|Hi\" " +
                "starts at <11>\n" +
                "     got: \"Hi There!\\nHello ...\\nWhat's up!\"");

        assertThat("Hi There!\nHello ...\nWhat's up!",
                is(like("What's up|Hello|Hi")
                        .startsAt(equalTo(11))
                        .endsAt(anyOf(equalTo(28), equalTo(15)))));
    }

    @Test
    public void likeLogicalOR$NegativeTest$5() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"What's up|Hello|Hi\" " +
                "starts at <11>\n" +
                "     got: \"Hi There!\\nHello ...\\nWhat's up!\"");

        assertThat("Hi There!\nHello ...\nWhat's up!",
                is(like("What's up|Hello|Hi")
                        .startsAt(equalTo(11))));
    }

    @Test
    public void likeLogicalOR$1() {
        assertThat("Hi There!\nHello ...\nWhat's up!",
                is(like("What's up|Hello|Hi")
                        .startsAt(10).endsAt(15)));
    }

    @Test
    public void likeLogicalOR$2() {
        assertThat("Hi There!\nHello ...\nWhat's up!",
                is(like("What's up|Hello|Hi")
                        .startsAt(equalTo(10))
                        .endsAt(anyOf(equalTo(29), equalTo(15)))));
    }

    @Test
    public void likeLogicalORandGroup() {
        assertThat("hat is the hit", is(like("h(a|i)t")));
    }

    @Test
    public void likeLogicalORandGroup$1$1() {
        assertThat("hat is the hit", is(like("h(a|i)t").startsAt(0, 0).endsAt(0, 3)));
    }

    @Test
    public void likeLogicalORandGroup$1$2NegativeTest$1$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <0> to <3> group 1\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(1, equalTo(0), equalTo(3)))));
    }

    @Test
    public void likeLogicalORandGroup$1$2NegativeTest$1$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <0> to <3> group 1\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(1, 0, 3))));
    }

    @Test
    public void likeLogicalORandGroup$1$2NegativeTest$2$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <1> to <3> group 0\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, equalTo(1), equalTo(3)))));
    }

    @Test
    public void likeLogicalORandGroup$1$2NegativeTest$2$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <1> to <3> group 0\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, 1, 3))));
    }

    @Test
    public void likeLogicalORandGroup$1$2NegativeTest$3$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <0> to <2> group 0\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, equalTo(0), equalTo(2)))));
    }

    @Test
    public void likeLogicalORandGroup$1$2NegativeTest$3$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <0> to <2> group 0\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, 0, 2))));
    }

    @Test
    public void likeLogicalORandGroup$1$2$1() {
        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, equalTo(0), equalTo(3)))));
    }

    @Test
    public void likeLogicalORandGroup$1$2$2() {
        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, 0, 3))));
    }

    @Test
    public void likeLogicalORandGroup$2$1() {
        assertThat("hat is the hit", is(like("h(a|i)t").startsAt(0, 11).endsAt(0, 14)));
    }

    @Test
    public void likeLogicalORandGroup$2$2() {
        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, equalTo(11), equalTo(14)))));
    }

    @Test
    public void likeLogicalORandGroup$2$3() {
        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(0, 11, 14))));
    }

    @Test
    public void likeLogicalORandGroup$3$1() {
        assertThat("hat is the hit", is(like("h(a|i)t").startsAt(1, 1).endsAt(1, 2)));
    }

    @Test
    public void likeLogicalORandGroup$3$2() {
        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(1, equalTo(1), equalTo(2)))));
    }

    @Test
    public void likeLogicalORandGroup$3$3() {
        assertThat("hat is the hit", is(like("h(a|i)t", regionByGroup(1, 1, 2))));
    }

    @Test
    public void likeLogicalORandRegionInAnyGroup$1NegativTest$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <2> to <2>\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", region(equalTo(2), equalTo(2)))));
    }

    @Test
    public void likeLogicalORandRegionInAnyGroup$1NegativTest$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <1> to <3>\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", region(equalTo(1), equalTo(3)))));
    }

    @Test
    public void likeLogicalORandRegionInAnyGroup$1() {
        assertThat("hat is the hit", is(like("h(a|i)t", region(equalTo(1), equalTo(2)))));
    }

    @Test
    public void likeLogicalORandRegionInAnyGroup$2NegativTest$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <2> to <2>\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", region(2, 2))));
    }

    @Test
    public void likeLogicalORandRegionInAnyGroup$2NegativTest$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"h(a|i)t\" " +
                "on region from <1> to <3>\n" +
                "     got: \"hat is the hit\"");

        assertThat("hat is the hit", is(like("h(a|i)t", region(1, 3))));
    }

    @Test
    public void likeLogicalORandRegionInAnyGroup$2() {
        assertThat("hat is the hit", is(like("h(a|i)t", region(1, 2))));
    }

    @Test
    public void groupsCombination$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"ab(b)a\" " +
                "on group <2>\n" +
                "     got: \"abba\"");

        assertThat("abba", is(like("ab(b)a", group(equalTo(2)))));
    }

    @Test
    public void groupsCombination() {
        assertThat("abba", is(like("ab(b)a", group(anyOf(equalTo(1), equalTo(0))))));
    }

    @Test
    public void twoStartsWithLike() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("not applicable when result matcher already specified");
        assertThat("", is(like("").startsAt(0).endsAt(0).startsAt(0)));
    }

    @Test
    public void twoEndsWithLike() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("not applicable when result matcher already specified");
        assertThat("", is(like("").endsAt(0).startsAt(0).endsAt(0)));
    }

    @Test
    public void likeCaseInsensitiveStart$2$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"(?i)hello|hi\" " +
                "on start position <1>\n" +
                "     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE), start(1))));
    }

    @Test
    public void likeCaseInsensitiveStart$2() {
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE), start(0))));
    }

    @Test
    public void likeCaseInsensitiveEnd$2$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"(?i)hello|hi\" on " +
                "end position <1>\n" +
                "     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE), end(1))));
    }

    @Test
    public void likeCaseInsensitiveEnd$2() {
        assertThat("Hi There!", is(like("hello|hi", new RegexProperties(CASE_INSENSITIVE), end(2))));
    }

    @Test
    public void likeCaseInsensitiveStartEnd$2$NegativeTest1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is like \"(?i)Hello|Hi\" " +
                "on (start position <1> and end position <1>)\n" +
                "     got: \"Hi There!\"");
        assertThat("Hi There!", is(like("Hello|Hi", new RegexProperties(CASE_INSENSITIVE), allOf(start(1), end(1)))));
    }

    @Test
    public void likeCaseInsensitiveStartEnd$2() {
        assertThat("Hi There!", is(like("Hello|Hi", new RegexProperties(CASE_INSENSITIVE), allOf(start(0), end(2)))));
    }

    @Test
    public void likeLogicalORandGroup$4$1() {
        assertThat("hat is the hit", is(like("h(a|i)t",
                allOf(startByGroup(1, 1), endByGroup(1, 2)))));
    }

    @Test
    public void likeLogicalORandGroup$4$2() {
        assertThat("hat is the hit", is(like("h(a|i)t",
                allOf(startByGroup(0, 0), endByGroup(0, 3),
                        startByGroup(0, 11), endByGroup(0, 14),
                        startByGroup(1, 1), endByGroup(1, 2)))));
    }

    @Test
    public void baseMatch$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"hello\"\n" +
                "     got: \" Hello \"");
        assertThat(" Hello ", match("hello"));
    }

    @Test
    public void baseMatch() {
        assertThat("hello", match("hello"));
    }

    @Test
    public void matchCaseInsensitive$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"(?i)hello\"\n" +
                "     got: \" Hello \"");
        assertThat(" Hello ", match("hello", new RegexProperties(CASE_INSENSITIVE)));
    }

    @Test
    public void matchCaseInsensitive() {
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE)));
    }

    @Test
    public void matchCaseInsensitiveStart() {
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE))
                .startsAt(0));
    }

    @Test
    public void matchCaseInsensitiveEnd() {
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE))
                .endsAt(5));
    }

    @Test
    public void matchCaseInsensitiveStartEnd$NegativeTest$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"(?i)hello\" " +
                "ends at <4> in group 0\n" +
                "     got: \"Hello\"");
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE))
                .startsAt(0).endsAt(4));
    }

    @Test
    public void matchCaseInsensitiveStartEnd$NegativeTest$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"(?i)hello\" starts at <1>\n" +
                "     got: \"Hello\"");
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE))
                .startsAt(1).endsAt(5));
    }

    @Test
    public void matchCaseInsensitiveStartEnd$NegativeTest$3() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"(?i)hello\" " +
                "starts at <1> and  ends at <4> in group 0\n" +
                "     got: \"Hello\"");
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE))
                .startsAt(1).endsAt(4));
    }

    @Test
    public void matchCaseInsensitiveStartEnd() {
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE))
                .startsAt(0).endsAt(5));
    }

    @Test
    public void matchStartEndMatcher$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"(?ix) hello \" " +
                "on (region from <0> to <4> or region from <1> to <5>)\n" +
                "     got: \"Hello\"");
        assertThat("Hello", match(" hello ", new RegexProperties(CASE_INSENSITIVE, COMMENTS), anyOf(region(0, 4), region(1, 5))));
    }

    @Test
    public void matchStartEndMatcher() {
        assertThat("Hello", match(" hello ", new RegexProperties(CASE_INSENSITIVE, COMMENTS), anyOf(region(0, 5), region(1, 6))));
    }

    @Test
    public void matchRegionMatcher$1$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"(?i)hello\" " +
                "on (region from <0> to <4> or region from <1> to <5>)\n" +
                "     got: \"Hello\"");
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE), anyOf(region(0, 4), region(1, 5))));
    }

    @Test
    public void matchRegionMatcher$1() {
        assertThat("Hello", match("hello", new RegexProperties(CASE_INSENSITIVE, COMMENTS), anyOf(region(0, 5), region(1, 6))));
    }

    @Test
    public void matchRegionMatcher$2$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: match \"(?ix) hello \" " +
                "on (region from <0> to <4> or region from <1> to <5>)\n" +
                "     got: \"Hello\"");
        assertThat("Hello", match(" hello ", new RegexProperties(CASE_INSENSITIVE, COMMENTS), anyOf(region(0, 4), region(1, 5))));
    }

    @Test
    public void matchRegionMatcher$2() {
        assertThat("Hello", match(" hello ", new RegexProperties(CASE_INSENSITIVE, COMMENTS), anyOf(region(0, 5), region(1, 6))));
    }

    @Test
    public void twoStartsWithMatch() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("not applicable when result matcher already specified");
        assertThat("", is(match("").startsAt(0).endsAt(0).startsAt(0)));
    }

    @Test
    public void twoEndsWithMatch() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("not applicable when result matcher already specified");
        assertThat("", is(match("").endsAt(0).startsAt(0).endsAt(0)));
    }

    @Test
    public void example1() {
        RegexProperties regexProperties = new RegexProperties(CASE_INSENSITIVE).setStartRegion(1).setEndRegion(6);
        assertThat("aAaBbcD", is(like("a*b", regexProperties)));
    }

    @Test
    public void example2() {
        assertThat("Hi There!", is(like("hi|hello", new RegexProperties(CASE_INSENSITIVE)).startsAt(equalTo(0))));
    }

    @Test
    public void example3() {
        assertThat("Hi There!", is(like("Hi|Hello", region(0, 2))));
    }
}
