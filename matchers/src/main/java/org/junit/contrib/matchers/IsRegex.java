package org.junit.contrib.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.regex.Pattern.compile;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Matches regular expression with an input sequence specified by the actual value {@link T}.
 * <p>
 * The matcher can be extended by specifying start and the end position found first by a
 * positive result of the matcher. As for instance
 * <p><blockquote><pre>
 *      assertThat("Hi There!", is(like("hi|hello", new RegexProperties(CASE_INSENSITIVE)).startsAt(equalTo(0))));
 *      assertThat("Hi There!", is(like("Hi|Hello", region(0, 2))));
 * </pre></blockquote>
 * <p>
 * Both operations {@code startsAt} and {@code endsAt} have two alternatives. The integer
 * matcher in last method parameter represents expected range of position. In other alternative
 * {@link #startsAt(int, org.hamcrest.Matcher)} and {@link #endsAt(int, org.hamcrest.Matcher)}
 * specifies a group of given regular expression. If this group is negative (i.e. not recognized within
 * regular expression's group counter), then the the methods fail by throwing {@link AssertionError}.
 * If the group, start, or end position is negative in
 * {@link #startsAt(int)}, {@link #startsAt(int, int)}, {@link #startsAt(int, org.hamcrest.Matcher)},
 * {@link #endsAt(int)}, {@link #endsAt(int, int)}, {@link #endsAt(int, org.hamcrest.Matcher)}.
 * If the methods {@link #startsAt(org.hamcrest.Matcher)}, {@link #startsAt(int, org.hamcrest.Matcher)},
 * {@link #endsAt(org.hamcrest.Matcher)}, {@link #endsAt(int, org.hamcrest.Matcher)} are used with
 * null in reference parameter, the method call fails by throwing {@link AssertionError}.
 * @param <T> an actual input sequence
 */
public final class IsRegex<T extends CharSequence>
        extends TypeSafeMatcher<T> implements IStartMatcher<T>, IEndMatcher<T> {
    private static enum MatchOperation {
        MATCH, FIND
    }

    private final Pattern pattern;
    private final Matcher<? extends Iterable<RegexResult>> successfulResultMatcher;
    private final RegexProperties regexProperties;
    private final MatchOperation matchOperation;

    private java.util.regex.Matcher matcher;
    private StartPosition start;
    private EndPosition end;
    private String majorFault;

    /**
     * Compiles the given regular expression into a pattern with the given flags.
     * </p>
     * @param regex
     * The expression to be compiled.
     * @param successfulResultMatcher
     * Hamcrest matcher applied after successful match of input subsequence with the given <tt>regex</tt>.
     * @param matchOperation
     * Specifies {@link java.util.regex.Matcher#matches()} or {@link java.util.regex.Matcher#find(int)} by
     * {@link MatchOperation#MATCH} or {@link MatchOperation#FIND}, respectively.
     * @param regexProperties
     * input sequence properties, and properties of this matcher of regular expression
     * @throws AssertionError (a unit failure)
     * If <tt>regex</tt>, <tt>matchOperation</tt> or <tt>regexProperties</tt> is null
     * ; or if the expression's syntax is invalid.
     */
    public IsRegex(final String regex,
                   final Matcher<? extends Iterable<RegexResult>> successfulResultMatcher,
                   final MatchOperation matchOperation,
                   final RegexProperties regexProperties) {

        assertNotNull("regular expression must not be null", regex);
        assertNotNull("match operation must not be null", matchOperation);
        assertNotNull("properties of regular expression must not be null", regexProperties);

        int patternFlags = 0;
        for (final MatchFlag patternFlag : regexProperties.getFlags())
            patternFlags |= patternFlag.getJavaFlag();

        Pattern pattern = null;
        try {
            pattern = compile(regex, patternFlags);
        } catch (PatternSyntaxException e) {
            fail(e.getMessage());
        } finally {
            this.pattern = pattern;
        }
        this.successfulResultMatcher = successfulResultMatcher;
        this.matchOperation = matchOperation;
        this.regexProperties = regexProperties;
    }

    /**
     *
     * @param input input sequence
     * @return {@code true} if matched
     * @throws AssertionError
     * if <tt>input</tt> is null;
     * or if start or end position in {@link RegexProperties} is greater than input sequence length;
     * or if group in <tt>startsAt</tt> or <tt>endsAt</tt> is greater than group counter.
     */
    @Override
    public boolean matchesSafely(final T input) {
        majorFault = null;

        if (input == null) {
            majorFault = "input sequence is null";
            fail(majorFault);
        }

        final int startPosition = regexProperties.getStartRegion(),
                    endPosition = regexProperties.getEndRegion(),
                    inputLength = input.length();

        if (startPosition > inputLength) {
            majorFault = "the start position ("
                            + startPosition
                            + ") is greater than the given input sequence length "
                            + inputLength;
            fail(majorFault);
        }

        if (endPosition > inputLength) {
            majorFault = "the end position ("
                            + endPosition
                            + ") is greater than the given input sequence length "
                            + inputLength;
            fail(majorFault);
        }

        matcher = startPosition == 0 && endPosition == -1 ?
                pattern.matcher(input)
                : pattern.matcher(input).region(startPosition, endPosition == -1 ? inputLength : endPosition);

        if (!regexProperties.hasAnchoringBounds()) matcher.useAnchoringBounds(false);
        if (regexProperties.hasTransparentBounds()) matcher.useTransparentBounds(true);

        final HashSet<RegexResult> regexResults = new HashSet<RegexResult>();

        if (successfulResultMatcher == null) {
            if (start != null && start.group > matcher.groupCount()) {
                majorFault = "group " + start.group + " must not be greater than " + matcher.groupCount();
                fail(majorFault);
            }

            if (end != null && end.group > matcher.groupCount()) {
                majorFault = "group " + end.group + " must not be greater than " + matcher.groupCount();
                fail(majorFault);
            }

            if (matchOperation == MatchOperation.MATCH)
                return matcher.matches()
                        && ((start == null || start.start(matcher))
                            & (end == null || end.end(matcher)));
            else {
                populateResults(regexResults);
                return !regexResults.isEmpty()
                        && ((start == null || matchesStart(start, regexResults))
                            & (end == null || matchesEnd(end, regexResults)));
            }
        } else {
            if (matchOperation == MatchOperation.FIND
                    || matcher.matches())
                populateResults(regexResults);

            return !regexResults.isEmpty()
                    && successfulResultMatcher.matches(regexResults);
        }
    }

    private boolean matchesStart(final StartPosition expected, final HashSet<RegexResult> regexResults) {
        for (final RegexResult regexResult : regexResults) {
            if (expected.group == regexResult.getGroup()
                    && expected.expectedIndex.matches(regexResult.getStart()))
                return true;
        }
        return false;
    }

    private boolean matchesEnd(final EndPosition expected, final HashSet<RegexResult> regexResults) {
        for (final RegexResult regexResult : regexResults) {
            if (expected.group == regexResult.getGroup()
                    && expected.expectedIndex.matches(regexResult.getEnd()))
                return true;
        }
        return false;
    }

    private void populateResults(final HashSet<RegexResult> regexResults) {
        for (int start, end; matcher.find(); ) {
            start = matcher.start();
            end = matcher.end();
            if (start != -1
                    && end != -1)
                regexResults.add(new RegexResult(0, start, end));
        }

        if (!matcher.find(0)) return;

        for (int start, end, group = 0, groupCount = matcher.groupCount(); group <= groupCount; ++group) {
            start = matcher.start(group);
            end = matcher.end(group);
            if (start != -1
                    && end != -1)
                regexResults.add(new RegexResult(group, start, end));
        }
    }

    private static String humanReadableFlags(MatchFlag[] flags) {
        if (flags == null || flags.length == 0) return "";
        TreeSet<Character> sortedFlags = new TreeSet<Character>();
        for (final MatchFlag flag : flags)
            if (flag != null && flag.getEmbeddedFlagExpression() != null)
                sortedFlags.add(flag.getEmbeddedFlagExpression().charAt(2));
        return humanReadableFlags(sortedFlags);
    }

    private static String humanReadableFlags(TreeSet<Character> flags) {
        if (flags.isEmpty()) return "";
        StringBuilder f = new StringBuilder(flags.size() + 3);
        f.append("(?");
        for (char flag : flags) f.append(flag);
        return f.append(")").toString();
    }

    @Override
    public void describeTo(final Description description) {
        if (majorFault != null) {
            description.appendText(majorFault);
            return;
        }

        description.appendText(matchOperation == MatchOperation.FIND ? "like \"" : "match \"")
                .appendText(humanReadableFlags(regexProperties.getFlags()))
                .appendText(pattern.pattern())
                .appendText("\"");

        if (successfulResultMatcher == null) {
            if (matchOperation == MatchOperation.MATCH) {
                final boolean isStartEvaluated
                        = start != null && start.isEvaluated();
                if (isStartEvaluated) {
                    /*description.appendText(" ")
                            .appendText(start.toString());*/
                    if (start.hasFailure())
                        description.appendText(" starts at ")
                                .appendText(start.getFailureDescription());
                }

                if (end != null && end.isEvaluated()) {
                    if (isStartEvaluated && start.hasFailure() &&
                            start.group != end.group)
                        description.appendText(" in group ")
                                .appendText(Integer.toString(start.group));
                    //description.appendText(end.toString());
                    if (end.hasFailure()) {
                        if (isStartEvaluated && start.hasFailure())
                            description.appendText(" and ");
                        description.appendText(" ends at ")
                                .appendText(end.getFailureDescription());
                        description.appendText(" in group ")
                                .appendText(Integer.toString(end.group));
                    }
                } else if (isStartEvaluated) description.appendText("in group ")
                        .appendText(Integer.toString(start.group));
            } else {
                final boolean isStartFailed = start != null
                        && start.expectedIndex.isExpectedIndexEvaluated()
                        && !start.expectedIndex.isExpectedIndexMatched();
                if (isStartFailed) {
                    description.appendText(" starts at ");
                    start.expectedIndex.describeTo(description);
                }

                if (end != null
                        && end.expectedIndex.isExpectedIndexEvaluated()
                        && !end.expectedIndex.isExpectedIndexMatched()) {
                    if (isStartFailed)
                        description.appendText(" and");
                    description.appendText(" ends at ");
                    end.expectedIndex.describeTo(description);
                }
            }
        } else {
            description.appendText(" on ")
                    .appendDescriptionOf(successfulResultMatcher);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEndMatcher<T> startsAt(int group, Matcher<Integer> index) {
        if (successfulResultMatcher != null || start != null)
            fail("not applicable when result matcher already specified");
        if (group < 0)
            fail("negative group: " + group);
        start = new StartPosition(group, index);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEndMatcher<T> startsAt(Matcher<Integer> index) {
        return startsAt(0, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEndMatcher<T> startsAt(int group, int index) {
        if (successfulResultMatcher != null || start != null)
            fail("not applicable when result matcher already specified");
        if (group < 0)
            fail("negative group: " + group);
        start = new StartPosition(group, equalTo(index));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEndMatcher<T> startsAt(int index) {
        return startsAt(0, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStartMatcher<T> endsAt(int group, Matcher<Integer> index) {
        if (successfulResultMatcher != null || end != null)
            fail("not applicable when result matcher already specified");
        if (group < 0)
            fail("negative group: " + group);
        end = new EndPosition(group, index);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStartMatcher<T> endsAt(Matcher<Integer> index) {
        return endsAt(0, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStartMatcher<T> endsAt(int group, int index) {
        if (successfulResultMatcher != null || end != null)
            fail("not applicable when result matcher already specified");
        if (group < 0)
            fail("negative group: " + group);
        end = new EndPosition(group, equalTo(index));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStartMatcher<T> endsAt(int index) {
        return endsAt(0, index);
    }

    /**
     * Attempts to match the entire region of the input sequence against the given <tt>pattern</tt>.
     * <p>
     * The pattern is specified by the first method parameter.
     * <p>
     * If the match succeeds, then more information can be obtained via the
     * <tt>startsAt</tt>, <tt>endsAt</tt>.
     * </p>
     * @param pattern a pattern to match
     * @param <T> an actual input sequence
     * @return this matcher
     * @throws AssertionError (a unit failure)
     * if <tt>pattern</tt> is null; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence> IsRegex<T> match(String pattern) {
        return new IsRegex<T>(pattern, null, MatchOperation.MATCH, new RegexProperties());
    }

    /**
     * Attempts to match the entire region of the input sequence against the given <tt>pattern</tt>
     * and matches successful results with given expectations in <tt>successfulResultMatcher</tt>.
     * <p>
     * The pattern is specified by the first method parameter, and the matched result is expected
     * to match with the another matcher <tt>Matcher<Iterable<RegexResult>></tt> specified by the
     * last method parameter.
     * </p>
     * @param pattern
     * a pattern to match
     * @param successfulResultMatcher
     * Hamcrest matcher applied after successful match of input sequence with the given <tt>pattern</tt>
     * @param <T> an actual input sequence
     * @param <R> a generic parameter on <tt>successfulResultMatcher</tt>
     * @return a Hamcrest's matcher reference
     * @throws AssertionError (a unit failure)
     * if <tt>pattern</tt> or <tt>successfulResultMatcher</tt> is null; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence, R extends Iterable<RegexResult>> Matcher<T> match(String pattern, Matcher<R> successfulResultMatcher) {
        assertNotNull("successfulResultMatcher is null", successfulResultMatcher);
        return new IsRegex<T>(pattern, successfulResultMatcher, MatchOperation.MATCH, new RegexProperties());
    }

    /**
     * Attempts to match the given region of the input sequence against the given <tt>pattern</tt>.
     * <p>
     * The pattern is specified by the first method parameter, and the matcher properties are given
     * in the last parameter <tt>regexProperties</tt>.
     * </p>
     * @param pattern a pattern to match
     * @param regexProperties
     * input sequence properties, and properties of this matcher of regular expression
     * @param <T> an actual input sequence
     * @return this matcher
     * @throws AssertionError (a unit failure)
     * if <tt>pattern</tt> or <tt>regexProperties</tt> is null; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence> IsRegex<T> match(String pattern, RegexProperties regexProperties) {
        return new IsRegex<T>(pattern, null, MatchOperation.MATCH, regexProperties);
    }

    /**
     * Attempts to match the given region of the input sequence against the given <tt>pattern</tt>
     * and matches successful results with given expectations in <tt>successfulResultMatcher</tt>.
     * <p>
     * The pattern is specified by the first method parameter. The properties are given in the
     * second parameter, and the matched result is expected to match with the another matcher
     * <tt>Matcher<Iterable<RegexResult>></tt> specified by the last method parameter.
     * </p>
     * @param pattern a pattern to match
     * @param regexProperties
     * input sequence properties, and properties of this matcher of regular expression
     * @param successfulResultMatcher
     * Hamcrest matcher applied after successful match of input sequence with the given <tt>pattern</tt>
     * @param <T> an actual input sequence
     * @param <R> a generic parameter on <tt>successfulResultMatcher</tt>
     * @return a Hamcrest's matcher reference
     * @throws AssertionError (a unit failure)
     * if <tt>pattern</tt> or <tt>regexProperties</tt> or <tt>successfulResultMatcher</tt> is null
     * ; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence, R extends Iterable<RegexResult>> Matcher<T> match(String pattern, RegexProperties regexProperties, Matcher<R> successfulResultMatcher) {
        assertNotNull("successfulResultMatcher is null", successfulResultMatcher);
        return new IsRegex<T>(pattern, successfulResultMatcher, MatchOperation.MATCH, regexProperties);
    }

    /**
     * Attempts to find all input subsequences against the given <tt>pattern</tt>.
     * <p>
     * The pattern is specified by the first method parameter.
     * <p>
     * If the match succeeds, then more information can be obtained via the
     * <tt>startsAt</tt>, <tt>endsAt</tt>.
     * </p>
     * @param pattern a pattern to match
     * @param <T> an actual input sequence
     * @return this matcher
     * @throws AssertionError (a unit failure)
     * If <tt>pattern</tt> is null; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence> IsRegex<T> like(String pattern) {
        return new IsRegex<T>(pattern, null, MatchOperation.FIND, new RegexProperties());
    }

    /**
     * Attempts to find all input subsequences against the given <tt>pattern</tt> which match
     * their successful results with given expectations in <tt>successfulResultMatcher</tt>.
     * <p>
     * The pattern is specified by the first method parameter, and the matched result is expected
     * to match with the another matcher <tt>Matcher<Iterable<RegexResult>></tt> specified by the
     * last method parameter.
     * </p>
     * @param pattern
     * a pattern to match
     * @param successfulResultMatcher
     * Hamcrest matcher applied after successful match of input sequence with the given <tt>pattern</tt>
     * @param <T> an actual input sequence
     * @param <R> a generic parameter on <tt>successfulResultMatcher</tt>
     * @return a Hamcrest's matcher reference
     * @throws AssertionError (a unit failure)
     * if <tt>pattern</tt> or <tt>successfulResultMatcher</tt> is null; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence, R extends Iterable<RegexResult>> Matcher<T> like(String pattern, Matcher<R> successfulResultMatcher) {
        assertNotNull("successfulResultMatcher is null", successfulResultMatcher);
        return new IsRegex<T>(pattern, successfulResultMatcher, MatchOperation.FIND, new RegexProperties());
    }

    /**
     * Attempts to find all input subsequences against the given <tt>pattern</tt>.
     * <p>
     * The pattern is specified by the first method parameter, and the matcher properties are given
     * in the last parameter <tt>regexProperties</tt>.
     * </p>
     * @param pattern a pattern to match
     * @param regexProperties
     * input sequence properties, and properties of this matcher of regular expression
     * @param <T> an actual input sequence
     * @return this matcher
     * @throws AssertionError (a unit failure)
     * if <tt>pattern</tt> or <tt>regexProperties</tt> is null; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence> IsRegex<T> like(String pattern, RegexProperties regexProperties) {
        return new IsRegex<T>(pattern, null, MatchOperation.FIND, regexProperties);
    }

    /**
     * Attempts to find all input subsequences against the given <tt>pattern</tt> which match
     * their successful results with given expectations in <tt>successfulResultMatcher</tt>.
     * <p>
     * The pattern is specified by the first method parameter. The properties are given in the
     * second parameter, and the matched result is expected to match with the another matcher
     * <tt>Matcher<Iterable<RegexResult>></tt> specified by the last method parameter.
     * </p>
     * @param pattern a pattern to match
     * @param regexProperties
     * input sequence properties, and properties of this matcher of regular expression
     * @param successfulResultMatcher
     * Hamcrest matcher applied after successful match of input sequence with the given <tt>pattern</tt>
     * @param <T> an actual input sequence
     * @param <R> a generic parameter on <tt>successfulResultMatcher</tt>
     * @return a Hamcrest's matcher reference
     * @throws AssertionError (a unit failure)
     * if <tt>pattern</tt> or <tt>regexProperties</tt> or <tt>successfulResultMatcher</tt> is null
     * ; or if the expression's syntax is invalid in the pattern.
     */
    @org.hamcrest.Factory
    public static <T extends CharSequence, R extends Iterable<RegexResult>> Matcher<T> like(String pattern, RegexProperties regexProperties, Matcher<R> successfulResultMatcher) {
        assertNotNull("successfulResultMatcher is null", successfulResultMatcher);
        return new IsRegex<T>(pattern, successfulResultMatcher, MatchOperation.FIND, regexProperties);
    }
}
