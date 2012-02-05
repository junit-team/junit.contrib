package org.junit.contrib.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Arrays;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.junit.Assert.assertNotNull;

/**
 * Matches regular expression via {@link #matches(String, MatchFlag...)} with an input
 * sequence specified by the actual value {@link T}.
 * <p>
 * The matcher can be extended by specifying start and the end position found first by a
 * positive result of the matcher {@link #matches(String, MatchFlag...)}. As for instance
 * <p><blockquote><pre>
 *      assertThat("Hi There!", matches("hi|hello", CASE_INSENSITIVE).startsAt(equalTo(0)));
 * </pre></blockquote>
 * <p>
 * Both operations {@code startsAt} and {@code endsAt} have two alternatives. The integer
 * matcher in last method parameter represents expected range of position. In other alternative
 * {@link #startsAt(int, org.hamcrest.Matcher)} and {@link #endsAt(int, org.hamcrest.Matcher)}
 * specifies a group of given regular expression. If this group is not recognized within
 * regular expression's group counter, then the {@link IndexOutOfBoundsException} is thrown.
 * @param <T> an actual input sequence
 */
public class IsRegex<T extends CharSequence> extends TypeSafeMatcher<T> {
    private final Pattern pattern;
    private final MatchFlag[] matchFlags;

    private T input;
    private java.util.regex.Matcher matcher;
    private StartPosition start;
    private EndPosition end;

    /**
     * Compiles the given regular expression into a pattern with the given flags.</p>
     * @param regex
     *          The expression to be compiled.
     * @param matchFlags
     *          Match flags, an enum array.
     * @throws java.util.regex.PatternSyntaxException
     *          If the expression's syntax is invalid.
     */
    public IsRegex(final String regex, final MatchFlag... matchFlags) {
        assertNotNull("regular expression must not be null", regex);
        assertNotNull("match flags array must not be null in given " + Arrays.toString(matchFlags), matchFlags);

        for (MatchFlag flag : matchFlags)
            assertNotNull("match flag must not be null", flag);

        int patternFlags = 0;
        for (final MatchFlag patternFlag : matchFlags)
            patternFlags |= patternFlag.getJavaFlag();

        this.pattern = compile(regex, patternFlags);
        this.matchFlags = matchFlags.clone();
    }

    @Override
    public boolean matchesSafely(final T input) {
        this.input = input;
        matcher = pattern.matcher(input);
        return matcher.find() && ((start == null || start.start(matcher)) && (end == null || end.end(matcher)));
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("to match the given regular expression \"")
                .appendText(pattern.pattern());

        if (matchFlags.length != 0)
            description.appendText("based on flags")
                    .appendText(Arrays.toString(matchFlags));

        description.appendText("\" with the input \"")
                .appendText(input.toString())
                .appendText("\"");

        if (start != null) {
            description.appendText(start.toString());
            if (start.hasFailure())
                description.appendText("expected")
                        .appendText(start.getFailureDescription());
        }

        if (end != null) {
            if (start != null)
                description.appendText("and");
            description.appendText(end.toString());
            if (end.hasFailure())
                description.appendText("expected")
                        .appendText(end.getFailureDescription());
        }
    }

    /**
     * The integer matcher represents expected range of position. The first method parameter
     * specifies a group of given regular expression to search matching start position. If given
     * <tt>group</tt> is not recognized within regular expression's group counter, then the
     * {@link IndexOutOfBoundsException} is thrown.
     * @param group a group in regular expression
     * @param index expected range of start position (inclusive)
     * @throws IndexOutOfBoundsException
     * if given <tt>group</tt> is not recognized within regular expression's group counter
     * @return this matcher
     */
    public IsRegex<T> startsAt(int group, Matcher<Integer> index) {
        final int groupCount = matcher.groupCount();
        if (group < 0) throw new IndexOutOfBoundsException("negative group: " + group);
        if (group > groupCount) throw new IndexOutOfBoundsException("group exceeded total count: " + groupCount);
        start = new StartPosition(group, index);
        return this;
    }

    /**
     * The integer matcher represents expected range of position.
     * @param index expected range of start position (inclusive)
     * @return this matcher
     */
    public IsRegex<T> startsAt(Matcher<Integer> index) {
        return startsAt(0, index);
    }

    /**
     * The integer matcher represents expected range of position. The first method parameter
     * specifies a group of given regular expression to search matching end position. If given
     * <tt>group</tt> is not recognized within regular expression's group counter, then the
     * {@link IndexOutOfBoundsException} is thrown.
     * @param group a group in regular expression
     * @param index expected range of end position (exclusive)
     * @throws IndexOutOfBoundsException
     * if given <tt>group</tt> is not recognized within regular expression's group counter
     * @return this matcher
     */
    public IsRegex<T> endsAt(int group, Matcher<Integer> index) {
        final int groupCount = matcher.groupCount();
        if (group < 0) throw new IndexOutOfBoundsException("negative group: " + group);
        if (group > groupCount) throw new IndexOutOfBoundsException("group exceeded total count: " + groupCount);
        end = new EndPosition(group, index);
        return this;
    }

    /**
     * The integer matcher represents expected range of position.
     * @param index expected range of end position (exclusive)
     * @return this matcher
     */
    public IsRegex<T> endsAt(Matcher<Integer> index) {
        return endsAt(0, index);
    }

    /**
     * A matcher of regular expression.
     * <p>
     * The expected regular expression is specified by the first method parameter, and options of it
     * can be additionally specified.
     * @param regex expected regular expression
     * @param matchFlags option of the regular expression
     * @param <T> an actual input sequence
     * @return matcher reference
     * @throws java.util.regex.PatternSyntaxException if the expression's syntax is invalid
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence> IsRegex<T> matches(String regex, MatchFlag... matchFlags) {
        return new IsRegex<T>(regex, matchFlags);
    }
}
