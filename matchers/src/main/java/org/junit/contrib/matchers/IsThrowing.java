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
import org.junit.Assert;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.junit.contrib.matchers.IsAssignableTo.assignableTo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.core.AnyOf.anyOf;

/**
 * These are matchers used together with the {@link org.junit.Assert#assertThat(Object, org.hamcrest.Matcher) assertThat}
 * method, however they are not currently included in JUnitMatchers.
 * The purpose of them is to encapsulate a code into a block, see {@link IBlock wrapping block}, and impose exceptional
 * conditions on how the block returns (normally or abruptly). This is useful when a surrounding code in test case should
 * not throw at all, or throws other expected exceptions.
 * <p>
 * The names of matchers which are finished by 'Deeply', report Java stack trace, cause and localized message, when
 * the running block of code has returned unexpectedly.
 * <p>
 * This matcher comes from practical experiences when trying to solve the following test scenario. The test case is testing
 * code statements, where their calls would not split to other methods and call a sequence the statements matters a lot.
 * Thus suppose this formal test case:
 * <p><blockquote><pre>
 *      <a>@</a>Test(expected = SomeException.class)
 *      public void myTest() {
 *          (statement)
 *          (statement)
 *
 *          This block must not throw SomeException
 *          {
 *              (statement)
 *              (statement)
 *              (statement)
 *          }
 *
 *          (statement)
 *          (statement)
 *      }
 *  </pre></blockquote>
 * <p>
 * In this particular scenario the developer dos not want to encapsulate the middle part into an ugly and dangerous
 * try-catch block. Here the test behaviour wants to say that all statements are supposed to throw SomeException, except
 * the middle part. This means that we want to keep Hamcrest in use with assertThat() and keep the tests as much verbose
 * as possible. When you read the expression using <code>assertThat()</code>, you can understand what it does. This is
 * very verbose expression in the test code using assertThat with the Hamcrest and JUnit. The <code>IsThrowing</code>
 * has four variants of matchers, where the last two are able to put the stack trace from the failing block to a
 * description (output) stream:
 *  <p><blockquote><pre>
 *      throwing(Class<a><</a><a>? </a>extends Throwable<a>></a>...  ): Matcher<a><</a>IsThrowing.IBlock<a>></a>
 *      notThrowing(Class<a><</a><a>? </a>extends Throwable<a>></a>...  ): Matcher<a><</a>IsThrowing.IBlock<a>></a>
 *      throwingDeeply(Class<a><</a><a>? </a>extends Throwable<a>></a>...  ): Matcher<a><</a>IsThrowing.IBlock<a>></a>
 *      notThrowingDeeply(Class<a><</a><a>? </a>extends Throwable<a>></a>...  ): Matcher<a><</a>IsThrowing.IBlock<a>></a>
 *  </pre></blockquote>
 * <p>
 * The form of <code>is(not(throwing()))</code> is equivalent to the form <code>is(notThrowing())</code>. Similar with
 * the form of <code>is(not(throwingDeeply()))</code> which is again equivalent to <code>is(notThrowingDeeply())</code>.
 * Once you use the 'Deeply', you can see the stack trace of failed block. The <code>allOf</code> and
 * <code>anyOf</code>, and/or combination forms of multiple matchers would not be much readable, however still correct.
 * <p>
 * Finally the expression may continue with {@link #andHasMessage(org.hamcrest.Matcher)} or
 * {@link #andHasMessage(String, MatchFlag...)} which additionally filters the expected exceptions against certainly
 * localized message (via <code>Matcher<a><</a>String<a>></a></code> or the <em>regular expression</em>) of thrown
 * exception in the block. Thus, the entire expression may be in the form as
 * <code>assertThat(block, is(throwing().andHasMessage()))</code>.
 * <p>
 * <p>
 * The following table describes a behavior and possible examples of the <code>IsThrowing</code> matcher.
 * <p>
 *  <table border="1">
 *      <col width="50%"/>
 *      <col width="50%"/>
 *  <thead>
 *      <tr><th>Use Case of IsThrowing</th><th>Explanation</th></tr>
 *  <thead>
 *  <tbody>
 *      <tr>
 *          <td>assertThat(block, is(throwing()))</td>
 *          <td>the 'block' is expected to throw any exception</td>
 *      </tr>
 *      <tr>
 *          <td>assertThat(block, is(throwingDeeply()))</td>
 *          <td>the 'block' is expected to throw any exception with block's stack trace (not an errorin 'block')</td>
 *      </tr>
 *      <tr>
 *          <td>assertThat(block, is(notThrowing())) or assertThat(block, is(not(throwing())))</td>
 *          <td>the 'block' is expected not to throw exceptions at all</td>
 *      </tr>
 *      <tr>
 *          <td>assertThat(block, is(notThrowingDeeply())) or assertThat(block, is(not(throwingDeeply())))</td>
 *          <td>the 'block' is expected not to throw exceptions at all (including block's stack trace if failure in it)</td>
 *      </tr>
 *      <tr>
 *          <td>assertThat(block, is(throwing(ArrayIndexOutOfBoundsException.class, IndexOutOfBoundsException.class)))</td>
 *          <td>the 'block' is expected to throw all assignable exceptions to java.lang.ArrayIndexOutOfBoundsException and java.lang.IndexOutOfBoundsException</td>
 *      </tr>
 *      <tr>
 *          <td>assertThat(block, is(throwingDeeply(ArrayIndexOutOfBoundsException.class, IndexOutOfBoundsException.class)))</td>
 *          <td>the 'block' is expected to throw all assignable exceptions to java.lang.ArrayIndexOutOfBoundsException and java.lang.IndexOutOfBoundsException (including block's stack trace if other failure in it). If other exception is thrown, the deep stack trace appears an error in the 'block'.</td>
 *      </tr>
 *      <tr>
 *          <td>assertThat(block, is(notThrowing(ArrayIndexOutOfBoundsException.class, IndexOutOfBoundsException.class))) or assertThat(block, is(not(throwing(ArrayIndexOutOfBoundsException.class, IndexOutOfBoundsException.class))))</td>
 *          <td>the 'block' is expected to throw all exceptions except for those which are assignable to java.lang.ArrayIndexOutOfBoundsException or java.lang.IndexOutOfBoundsException</td>
 *      </tr>
 *      <tr>
 *          <td>assertThat(block, is(notThrowingDeeply(ArrayIndexOutOfBoundsException.class, IndexOutOfBoundsException.class))) or assertThat(block, is(not(throwingDeeply(ArrayIndexOutOfBoundsException.class, IndexOutOfBoundsException.class))))</td>
 *          <td>the 'block' is expected to throw all exceptions except for those which are assignable to java.lang.ArrayIndexOutOfBoundsException or java.lang.IndexOutOfBoundsException; Otherwise a stack trace appears from the 'block'</td>
 *      </tr>
 *  </tbody>
 * </table>
 * <p>
 * <em>Note: Assignable exceptions means all specified and their sub-types.</em>
 * <p>
 * <p>
 * <p>
 * Concrete use cases:
 * A lot of use cases are covered in the test of this matcher, see <code>TestIsThrowable</code>.
 * <p>
 * This is the most simple use case:
 *  <p><blockquote><pre>
 *      final Integer[] array = new Integer[2];
 *      IBlock block = new IBlock() {
 *          public void run() {
 *              int i = 0;
 *              array[i++] = i;
 *              array[i++] = i;
 *              array[i++] = i;
 *          }
 *      };
 *      assertThat(block, is(throwing(ArrayIndexOutOfBoundsException.class)));
 *      array[1] = array[0];
 * </pre></blockquote>
 * <p>
 * <p>
 * <p>
 * A description can be placed on the exceptional conditions using {@link org.hamcrest.core.DescribedAs}, in a form as
 * follows:
 *  <p><blockquote><pre>
 *      expected = {IndexOutOfBoundsException.class, ArrayStoreException.class};
 *      assertThat(block, describedAs("\n%0 <a><</a>why should 0<a>></a>,\n%1 <a><</a>why should 1<a>></a>", is(throwingDeeply(expected)), expected))
 *  </pre></blockquote>
 * <p>
 * <p>
 * This matcher can be also used in multiple times running block within one call of
 * {@link org.junit.Assert#assertThat(Object, org.hamcrest.Matcher)} if these matchers are encapsulated by
 * {@link org.junit.matchers.JUnitMatchers#both(org.hamcrest.Matcher)} and <code>and()</code>, <code>or()</code> matchers.
 *  <p><blockquote><pre>
 *      List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
 *      final ListIterator it = list.listIterator();
 *      IBlock block = new IBlock() {
 *          public void run() {
 *              it.next();
 *              it.next();
 *              it.set("a");
 *          }
 *      };
 *      assertThat(block, both(is(throwingDeeply(NoSuchElementException.class))).and(is(notThrowing())));
 *  </pre></blockquote>
 * <p>
 * <p>
 * Other alternative of <code>both()</code> matcher would be Hamcrest's <code>allOf()</code> matcher:
 * <p><blockquote><pre>
 *      List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
 *      final ListIterator it = list.listIterator();
 *      IBlock block = new IBlock() {
 *          public void run() {
 *              it.next();
 *              it.next();
 *              it.set("a");
 *          }
 *      };
 *      assertThat(block, allOf(is(notThrowing()), is(throwingDeeply(NoSuchElementException.class))));
 *  </pre></blockquote>
 * <p>
 * The order in which the block is launched by every matcher by <code>allOf()</code> is not same as if used in
 * <code>both()</code> matcher. The <code>both().or()</code> and <code>anyOf()</code> can be used as an alternative in
 * other cases as well, when the block is expected to be reused in a test case in an order within one call of
 * <code>assertThat()</code>.
 * <p>
 * <p>
 * Other two examples would describe situations when you want to assert against exceptions and expected messages. The
 * first example shows the use of regular expression:
 * <p><blockquote><pre>
 *      List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
 *      final ListIterator it = list.listIterator();
 *      IBlock block = new IBlock() {
 *          public void run() {
 *              throw new Exception("     a ny thin g     ");
 *          }
 *      };
 *      assertThat(block, is(throwingDeeply().andHasMessage("a ny|thing")));
 *  </pre></blockquote>
 * <p>
 * The second example uses <code>Matcher<String></code> combinations on a thrown message:
 *  <p><blockquote><pre>
 *      List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
 *      final ListIterator it = list.listIterator();
 *      IBlock block = new IBlock() {
 *          public void run() {
 *              throw new Exception("     a ny thin g     ");
 *          }
 *      };
 *      assertThat(block, is(throwingDeeply().andHasMessage(anyOf(containsString("any"), containsString("thin"), containsString("thing")))));
 *  </pre></blockquote>
 * <p>
 * <p>
 * <p>
 * Known limitations:
 * The <code>assertThat()</code> fails as soon in Matcher construction as identifying that no exceptions are required to
 * throw (i.e. using {@link #notThrowing(Class[])}, {@link #notThrowingDeeply(Class[])} with empty parameter list) and
 * an expected thrown message is specified (even if null) via {@link #andHasMessage(org.hamcrest.Matcher)}, and
 * {@link #andHasMessage(String, MatchFlag...)}.
 * <p>
 * @author Tibor17
 * @version 0.1
 * @since 0.1, Sep 15, 2011, 10:33:33 AM
 */
public class IsThrowing<B extends IsThrowing.IBlock> extends TypeSafeMatcher<B> {

    @SuppressWarnings("unchecked")
    private static final Class<? extends Throwable>[] ALL_THROWABLE = new Class[]{Throwable.class};

    public static interface IBlock {
        public void run() throws Throwable;
    }

    public final class IsLike<B extends IsThrowing.IBlock> {
        private final String regex;
        private IsLike(final String regex) { this.regex = regex; }
        @SuppressWarnings("unchecked")
        public <T extends java.util.regex.Matcher> IsThrowing<B> like(final Matcher<T> regexMatcher) {
            thrownMessageMatcher = new ThrownMessageMatcher("with a message (i.e. null)", "with a message (i.e. \"", "\")", " which has ",  ".", "") {
                @Override protected boolean isMatching(Throwable thrown) { return isMatchingMessageByRegex(thrown, regex, regexMatcher); }
                @Override protected boolean hasNullMessage(Throwable thrown) { return IsThrowing.hasNullMessage(thrown); }
                @Override protected String getActualMessage(Throwable thrown) { return getMessage(thrown); }
                @Override protected Matcher<?> getExpectedMatcher() { return regexMatcher; }
                @Override protected String getExpectedMessage() { return null; }
            };
            return (IsThrowing<B>) IsThrowing.this;
        }
    }

    private final Matcher<Class<? extends Throwable>> exceptions;
    private final boolean isThrowing, showStack;

    private Throwable unexpected;
    private ThrownMessageMatcher thrownMessageMatcher;
    private String additionalErrMsg;

    protected IsThrowing() {
        exceptions = anyOf();
        isThrowing = showStack = false;
    }


    protected IsThrowing(Class<B> expectedType) {
        super(expectedType);
        exceptions = anyOf();
        isThrowing = showStack = false;
    }

    /**
     * Specifies expected behaviour when running the block of code.
     * <p/>
     * @param operationName an operation name, e.g., throwing, notThrowing, throwingDeeply, and notThrowingDeeply
     * @param isThrowing {@code true} if any of the given <tt>exceptions</tt> is expected to be thrown by calling the
     *                  {@link IsThrowing.IBlock#run()}. If no exceptions are specified and this parameter is
     *                  {@code true}, then any exception is expected to be thrown when running the block of code.
     *                  If {@code false}, then a complementary exception is expected to throw when running the block.
     *                  If {@code false} and no exceptions are specified at all, then no exception is expected to be
     *                  thrown (complement of <em>all</em> is <em>nothing</em>).
     * @param showStack If {@code true} and an unexpected exception (other than <tt>exceptions</tt>) is thrown in running
     *                  block, then Java Stack Trace is passed in the description on method's parameter of
     *                  {@link #describeTo(org.hamcrest.Description)}.
     * @param exceptions Expected exceptions which belong to the particular operation. An empty array of exceptions, or
     *                  the {@link Throwable Throwable.class} in array represents <em>all</em> exceptions.

     */
    protected IsThrowing(final String operationName, final boolean isThrowing, final boolean showStack,
                         final Class<? extends Throwable>... exceptions) {
        this(isThrowing, showStack, anyOfThrowable(operationName, exceptions.length == 0 ? ALL_THROWABLE : exceptions));
    }

    /**
     * Specifies expected behaviour when running the block of code. Internal use only.
     * <p/>
     * @param isThrowing {@code true} if used by static matcher.
     * @param showStack If {@code true} and an unexpected exception is thrown in running block, then appeared Java Stack Trace
     *                  appears in the description on method's parameter of {@link #describeTo(org.hamcrest.Description)}.
     * @param exceptions A Matcher of expected exceptions which belong to 'throwing' operation. The actual exception thrown
     *                  by the {@link B block} is caught internally and never null.
     */
    private IsThrowing(final boolean isThrowing, final boolean showStack, final Matcher<Class<? extends Throwable>> exceptions) {
        assertNotNull("an array of exceptions must not be null", exceptions);
        this.exceptions = exceptions;
        this.showStack = showStack;
        this.isThrowing = isThrowing;
    }

    private static Matcher<Class<? extends Throwable>> anyOfThrowable(final String operationName, final Class<? extends Throwable>... exceptions) {
        assertNotNull("an array of exceptions must not be null", exceptions);
        return anyOf(new ArrayList<Matcher<? extends Class<? extends Throwable>>>(){{
            for (final Class<? extends Throwable> t : exceptions) {
                assertNotNull("an array of exceptions in the operation "
                                + getClass().getName() + "." + operationName + "()"
                                + " must not have null elements", t);
                add(assignableTo(t));
            }}});
    }

    private static String toString(final Class<? extends Throwable>... exceptions) {
        int i = exceptions.length;
        final StringBuilder description = new StringBuilder("[");
        for (final Class<? extends Throwable> t : exceptions) {
            description.append(t.getName());
            if (--i != 0) description.append(", ");
        }
        return description.append("]").toString();
    }

    /**
     * The method parameter 'block' has expected type {@link B}, and its value is not null.
     * @param block a block of testing code
     * @return {@code true} if matching expectations
     */
    @Override
    public boolean matchesSafely(B block) {
        boolean match = !isThrowing;
        Throwable thrown = null;
        try {
            block.run();
        } catch (final Throwable throwable) {
            thrown = throwable;
            match ^= exceptions.matches(throwable.getClass());
            unexpected = throwable;
        }

        if (thrownMessageMatcher != null) {
            if (match && thrown != null)







                match = thrownMessageMatcher.isMatching(thrown);

            additionalErrMsg = additionalErrMsg == null ?
                    thrownMessageMatcher.createErrorMessage(thrown)
                    : new StringBuilder(additionalErrMsg)
                        .append('\r').append('\n')
                        .append(thrownMessageMatcher.createErrorMessage(thrown)).toString();
        }

        return match;
    }

    @Override
    public void describeTo(Description description) {
        final boolean showUnexpectedStackTrace = showStack & unexpected != null;
        if (matchesAllThrowable()) {
            if (isThrowing) description.appendText("to throw any exception");
            else description.appendText("not to throw exceptions at all");

        } else {
            if (isThrowing) description.appendText("to throw given exception(s)");
            else description.appendText("not to throw given exception(s)");





        }

        if (additionalErrMsg != null)
            description.appendText("\r\n").appendText(additionalErrMsg);

        if (showUnexpectedStackTrace) {
            description.appendText("\r\nInstead the JUnit block threw ");


            description.appendText(unexpected.toString()).appendText("\r\n");
            for (final StackTraceElement s : unexpected.getStackTrace())
                description.appendText(s.toString()).appendText("\r\n");
        }
    }

    private boolean matchesAllThrowable() {
        try {
            return exceptions.matches(Throwable.class);
        } catch (VirtualMachineError e) {
            throw e;
        } catch (Throwable t) {
            fail(t.getLocalizedMessage());
            throw new IllegalStateException("unreachable statement");
        }
    }

    public final IsLike<B> withMessage(final String regex) {
        assertTrue("illegal to expect no exceptions to throw with a certain thrown message", matchesAllThrowable());
        return new IsLike<B>(regex);
    }

    public final Matcher<B> andHasMessage(final Matcher<String> matchWith) {
        assertTrue("illegal to expect no exceptions to throw with a certain thrown message", matchesAllThrowable());
        thrownMessageMatcher = new ThrownMessageMatcher("with a message (i.e. null)", "with a message (i.e. \"", "\")", " which ", ".", "is ") {
            @Override protected boolean isMatching(Throwable thrown) { return isMatchingMessageByHamcrest(thrown, matchWith); }
            @Override protected boolean hasNullMessage(Throwable thrown) { return IsThrowing.hasNullMessage(thrown); }
            @Override protected String getActualMessage(Throwable thrown) { return getMessage(thrown); }
            @Override protected Matcher<?> getExpectedMatcher() { return matchWith; }
            @Override protected String getExpectedMessage() { return null; }
        };
        return this;
    }

    public final Matcher<B> andHasMessage(final String regex, final MatchFlag... flags) {
        assertTrue("illegal to expect no exceptions to throw with certainly a given message regex: " + regex, matchesAllThrowable());
        thrownMessageMatcher = new ThrownMessageMatcher("which a message (i.e. null)", "which a message (i.e. \"", "\")", " should match given regex \"", "\".", "") {
            @Override protected boolean isMatching(Throwable thrown) { return hasNullMessage(thrown) ? regex == null : regex != null && isMatchingMessageByRegex(thrown, regex, flags); }
            @Override protected boolean hasNullMessage(Throwable thrown) { return IsThrowing.hasNullMessage(thrown); }
            @Override protected String getActualMessage(Throwable thrown) { return getMessage(thrown); }
            @Override protected Matcher<?> getExpectedMatcher() { return null; }
            @Override protected String getExpectedMessage() { return regex; }
        };
        return this;
    }

    private static String getMessage(final Throwable thrown) {
        assertNotNull("(2) an instance of thrown exception must not be NULL", thrown);
        return thrown.getLocalizedMessage();
    }

    private static boolean hasNullMessage(final Throwable thrown) {
        assertNotNull("(0) an instance of thrown exception must not be NULL", thrown);
        return thrown.getLocalizedMessage() == null;
    }

    private static boolean isMatchingMessageByRegex(final Throwable thrown, final String regex, MatchFlag... flags) {
        assertNotNull("(1) an instance of thrown exception must not be NULL", thrown);
        assertNotNull("(0) regular expression must not be null", regex);
        final String msg = getMessage(thrown);
        assertNotNull("(0) observed exception produced unacceptable NULL message", msg);
        assertNotNull("corresponding enum flags must not be null", flags);

        int patternFlags = 0;
        for (final MatchFlag patternFlag : flags) {
            assertNotNull("null Pattern's flag in " + Arrays.toString(flags), patternFlag);
            patternFlags |= patternFlag.getJavaFlag();
        }

        try {
            return Pattern.compile(regex, patternFlags).matcher(msg).find();
        } catch (PatternSyntaxException e) {
            fail("(0) invalid regular expression syntax: " + regex);
            throw new InternalError("error in " + Assert.class.getName()
                                                    + ".fail(String, Object) must not return normally");
        }
    }

    private static <T extends java.util.regex.Matcher> boolean isMatchingMessageByRegex(final Throwable thrown, final String regex, final Matcher<T> regexMatcher) {
        assertNotNull("(4) an instance of thrown exception must not be NULL", thrown);
        assertNotNull("(1) regular expression must not be null", regex);
        final String msg = getMessage(thrown);
        assertNotNull("(0) observed exception produced unacceptable NULL message", msg);
        try {
            return regexMatcher.matches(Pattern.compile(regex).matcher(msg));
        } catch (PatternSyntaxException e) {
            fail("(1) invalid regular expression syntax: " + regex);
            throw new InternalError("error in " + Assert.class.getName()
                                                    + ".fail(String, Object) must not return normally");
        }
    }

    private static <T> boolean isMatchingMessageByHamcrest(final Throwable thrown, final Matcher<T> matchWith) {
        assertNotNull("(3) an instance of thrown exception must not be NULL", thrown);
        assertNotNull("hamcrest matcher must not be null", matchWith);
        return matchWith.matches(getMessage(thrown));
    }

    @org.hamcrest.Factory
    public static <T extends IBlock> IsThrowing<T> throwing(final Class<? extends Throwable>... expectedExceptions) {
        IsThrowing<T> matcher = new IsThrowing<T>("throwing", true, false, expectedExceptions);
        if (expectedExceptions.length != 0) matcher.additionalErrMsg = toString(expectedExceptions);
        return matcher;
    }

    @org.hamcrest.Factory
    public static <T extends IBlock> IsThrowing<T> throwing(final Matcher<Class<? extends Throwable>> expectedExceptions) {
        return new IsThrowing<T>(true, false, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static IsThrowing<IBlock> throwingDeeply(final Matcher<Class<? extends Throwable>> expectedExceptions) {
        return new IsThrowing<IBlock>(true, true, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static <T extends IBlock> Matcher<T> throwing(final Matcher<? extends Throwable> expectedExceptions) {
        return new TypeSafeMatcher<T>() {
            @Override public boolean matchesSafely(T block) {
                try {
                    block.run();
                    return false;
                } catch (final Throwable throwable) {
                    return expectedExceptions.matches(throwable);
                }
            }

            @Override public void describeTo(Description description) {
                expectedExceptions.describeTo(description.appendText("(exceptions and messages by a matcher)\r\n"));
            }
        };
    }

    @org.hamcrest.Factory
    public static <T extends IBlock> IsThrowing<T> notThrowing(final Class<? extends Throwable>... exceptionsComplement) {
        IsThrowing<T> matcher = new IsThrowing<T>("notThrowing", false, false, exceptionsComplement);
        if (exceptionsComplement.length != 0) matcher.additionalErrMsg = toString(exceptionsComplement);
        return matcher;
    }

    @org.hamcrest.Factory
    public static <T extends IBlock> IsThrowing<T> throwingDeeply(final Class<? extends Throwable>... expectedExceptions) {
        IsThrowing<T> matcher = new IsThrowing<T>("throwingDeeply", true, true, expectedExceptions);
        if (expectedExceptions.length != 0) matcher.additionalErrMsg = toString(expectedExceptions);
        return matcher;
    }

    @org.hamcrest.Factory
    public static <T extends IBlock> IsThrowing<T> notThrowingDeeply(final Class<? extends Throwable>... exceptionsComplement) {
        IsThrowing<T> matcher = new IsThrowing<T>("notThrowingDeeply", false, true, exceptionsComplement);
        if (exceptionsComplement.length != 0) matcher.additionalErrMsg = toString(exceptionsComplement);
        return matcher;
    }
}
