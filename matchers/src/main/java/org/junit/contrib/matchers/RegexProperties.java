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

import java.util.Arrays;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

/**
 * The purpose of this class is to specify properties on static methods <tt>like</tt>
 * and <tt>match</tt> in {@link org.junit.contrib.matchers.IsRegex}.
 * As for instance, the input sequence "aAaBbcD" is handled and other case-insensitive
 * subsequence within the region [1, 6) is to be matched with the pattern "a*b".
 * This means that the subsequence "AaBbc" should be considered.<p>
 * <blockquote><pre>
 *      RegexProperties regexProperties = new RegexProperties(CASE_INSENSITIVE).setStartRegion(1).setEndRegion(6);
 *      assertThat("aAaBbcD", is(like("a*b", regexProperties)));
 * </pre></blockquote>
 * <p>
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsRegex
 * @since 0.1, 27.12.2011, 17:57
 */
public final class RegexProperties {
    private int startRegion, endRegion = -1;
    private MatchFlag[] flags = new MatchFlag[0];
    private boolean useAnchoringBounds = true, useTransparentBounds;

    /**
     * Default properties.
     */
    public RegexProperties() {}

    /**
     * Properties with arbitrary mach flags.
     * @param matchFlags mach flags array
     * @throws AssertionError
     * if the array is null or contains null element(s)
     * @see java.util.regex.Pattern#compile(String, int)
     */
    public RegexProperties(MatchFlag... matchFlags) {
        setMatchFlags(matchFlags);
    }

    /**
     * Specifies the start index on the input sequence been applicable to {@link IsRegex}.
     * @param startRegion start index on the input sequence
     * @return this
     * @throws AssertionError
     * if negative number <tt>startRegion</tt>
     * @see java.util.regex.Matcher#region(int, int)
     */
    public RegexProperties setStartRegion(int startRegion) {
        if (startRegion < 0)
            fail("startRegion is negative");
        this.startRegion = startRegion;
        return this;
    }

    /**
     * Specifies the end index on the input sequence been applicable to {@link IsRegex}.
     * @param endRegion end index on the input sequence
     * @return this
     * @throws AssertionError
     * if negative number <tt>endRegion</tt>
     * @see java.util.regex.Matcher#region(int, int)
     */
    public RegexProperties setEndRegion(int endRegion) {
        if (endRegion < 0)
            fail("endRegion is negative");
        this.endRegion = endRegion;
        return this;
    }

    /**
     * Properties with arbitrary mach flags.
     * @param matchFlags mach flags array
     * @return this
     * @throws AssertionError
     * if the array is null or contains null element(s)
     * @see java.util.regex.Pattern#compile(String, int)
     */
    public RegexProperties setMatchFlags(MatchFlag... matchFlags) {
        if (matchFlags == null)
            fail("null flags on given array");
        MatchFlag[] flags = new MatchFlag[matchFlags.length];
        int i = 0;
        for (MatchFlag flag : matchFlags) {
            if (flag == null)
                fail("null element at " + i
                        + " in given flags array "
                        + Arrays.toString(matchFlags));
            if (!hasSameInstance(0, i, flag, flags)) flags[i++] = flag;
        }
        this.flags = i == flags.length ? flags : Arrays.copyOf(flags, i);
        return this;
    }

    /**
     * Sets the anchoring of region bounds for this matcher.
     * @param enable a boolean indicating whether or not to use anchoring bounds
     * @return this
     * @see java.util.regex.Matcher#useAnchoringBounds(boolean)
     */
    public RegexProperties setAnchoringBounds(boolean enable) {
        useAnchoringBounds = enable;
        return this;
    }

    /**
     * Sets the transparency of region bounds for this matcher.
     * @param enable a boolean indicating whether to use opaque or transparent regions
     * @return this
     * @see java.util.regex.Matcher#useTransparentBounds(boolean)
     */
    public RegexProperties setTransparentBounds(boolean enable) {
        useTransparentBounds = enable;
        return this;
    }

    /**
     * A value provided by {@link #setStartRegion(int)}; or zero otherwise (by default).
     * @return {@link #setStartRegion(int)}; or zero otherwise (by default)
     * @see java.util.regex.Matcher#region(int, int)
     */
    public int getStartRegion() {
        return startRegion;
    }

    /**
     * A value provided by {@link #setEndRegion(int)}; or -1 otherwise (unlimited by default).
     * @return {@link #setEndRegion(int)}; or -1 otherwise (unlimited by default)
     * @see java.util.regex.Matcher#region(int, int)
     */
    public int getEndRegion() {
        return endRegion;
    }

    /**
     * Properties with arbitrary mach flags.
     * @return mach flags
     * @see java.util.regex.Pattern#compile(String, int)
     */
    public MatchFlag[] getFlags() {
        return flags.clone();
    }

    /**
     * Returns a boolean indicating whether or not to use anchoring bounds ({@code true} by default).
     * @return a boolean indicating whether or not to use anchoring bounds ({@code true} by default)
     * @see java.util.regex.Matcher#useAnchoringBounds(boolean)
     */
    public boolean hasAnchoringBounds() {
        return useAnchoringBounds;
    }

    /**
     * Returns a boolean indicating whether to use opaque or transparent regions ({@code false} by default).
     * @return a boolean indicating whether to use opaque or transparent regions ({@code false} by default)
     * @see java.util.regex.Matcher#useTransparentBounds(boolean)
     */
    public boolean hasTransparentBounds() {
        return useTransparentBounds;
    }

    /**
     * Returns {@code true} if, and only if, the array <tt>references</tt>
     * has the given reference within given range of the array indexes.
     * @param references content
     * @param from inclusive
     * @param to exclusive
     * @param reference a reference to find
     * @param <T> generic parameter of both the given reference and the array
     * @return {@code true} if found reference between from (inclusive) and to (exclusive)
     * @throws AssertionError if {@code from > to}
     * ; or if {@code from < 0 or to > references.length}
     * ; or if null <tt>references</tt>
     */
    private static <T> boolean hasSameInstance(final int from, final int to, final T reference, final T... references) {
        assertNotNull("references: null", references);

        if (from < 0 || to > references.length)
            fail("from: " + from
                    + ", to: " + to
                    + ", references.length: " + references.length);

        if (from > to)
            fail("from: " + from
                    + ", to: " + to);

        for (int i = from; i < to; ++i) {
            if (references[i] == reference)
                return true;
        }

        return false;
    }
}
