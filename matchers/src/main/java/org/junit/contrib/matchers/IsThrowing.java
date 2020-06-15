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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.ArrayList;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.contrib.matchers.IsAssignableTo.assignableToThrowable;

/**
 * These are matchers used together with the {@link org.junit.Assert#assertThat(Object, org.hamcrest.Matcher) assertThat}
 * method, however they are not currently included in JUnitMatchers.
 * <p> The purpose of them is to encapsulate a code into a block, see wrapping blocks {@link Block} and {@link Callable}, and
 * impose exceptional conditions on how the block returns (normally or abruptly). <p>This is especially useful in
 * situations, when a surrounding code in test case should not throw at all, or throws other expected exceptions.
 * <p> The names of matchers which are finished by 'Deeply' report Java stack trace, cause and localized message, when
 * the running block of code has returned unexpectedly.
 * <p> This matcher comes from practical experiences when trying to solve the following test scenario. The test case is
 * testing code statements, where their calls would not split to other methods and call a sequence the statements
 * matters a lot. Thus suppose this formal test case:<p>
 * <blockquote>
 *  <pre>
 *      &#064;Test(expected = SomeException.class)
 *      public void myTest() {
 *          (statement)
 *          (statement)
 *
 *          This block should not throw any exception
 *          {
 *              (statement)
 *              (statement)
 *              (statement)
 *          }
 *
 *          (statement)
 *          (statement)
 *      }
 *  </pre>
 * </blockquote>
 * <p> In this particular scenario the developer dos not want to encapsulate the middle part into an ugly and dangerous
 * try-catch block. Here the test behaviour wants to say that all statements are supposed to throw exceptions, except for
 * the middle part. This means that we want to keep Hamcrest in use with assertThat() and keep the tests as much verbose
 * as possible. When you read the expression using <code>assertThat()</code>, you can understand what it does.
 * <p> The matchers in <code>IsThrowing</code> have eight variants, where the second half deals with stack trace from the
 * failing block to a description (output stream):<p>
 * <blockquote>
 *  <pre>
 *      throwing(Class&lt;? extends Throwable&gt;...  ): IThrownMessage&lt;Void, ? extends Block&gt;
 *      throwing(Matcher&lt;Class&lt;? extends Throwable&gt;): IThrownMessage&lt;V, ? extends Callable&gt;
 *      throwing(Matcher&lt;? extends Throwable&gt;): Matcher&lt;? extends Callable&gt;
 *      notThrowing(Class&lt;? extends Throwable&gt;...  ): IThrownMessage&lt;V, ? extends Callable&gt;
 *      throwingDeeply(Class&lt;? extends Throwable&gt;...  ): IThrownMessage&lt;Void, ? extends Block&gt;
 *      throwingDeeply(Matcher&lt;Class&lt;? extends Throwable&gt;): IThrownMessage&lt;V, ? extends Callable&gt;
 *      throwingDeeply(Matcher&lt;? extends Throwable&gt;): Matcher&lt;? extends Callable&gt;
 *      notThrowingDeeply(Class&lt;? extends Throwable&gt;...  ): IThrownMessage&lt;V, ? extends Callable&gt;
 *  </pre>
 * </blockquote>
 * <p> The form of <code>is(not(throwing()))</code> is equivalent to the form <code>is(notThrowing())</code>. Similar with
 * the form of <code>is(not(throwingDeeply()))</code> which is again equivalent to <code>is(notThrowingDeeply())</code>.
 * Once you use the 'Deeply', the stack trace of failed block is observed. The <code>allOf</code> and
 * <code>anyOf</code>, and/or combination forms of multiple matchers would not be much readable, however still correct.
 * <p> Finally the expression may continue with {@link #andMessage(org.hamcrest.Matcher)} or {@link #andMessage(IsRegex)}
 * which additionally filters the expected exceptions against certainly localized message
 * (via <code>Matcher&lt;String&gt;</code> or the <em>regular expression</em>) of thrown exception in the block.
 * Thus, the entire expression appears in a form of <code>assertThat(block, is(throwing().andMessage()))</code>.<p>
 * <p> The following table describes a behavior and possible examples of the <code>IsThrowing</code> matcher.
 * <p><table border="1">
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
 *          <td>the 'block' is expected to throw any exception with block's stack trace (not an error in 'block')</td>
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
 * <em>Note: Assignable exceptions means all specified and their sub-types.</em><p><p>
 * <p> Concrete use cases:<p>
 * A lot of use cases are covered in the tests, see <code>TestIsThrowing</code>.
 * <p> This is the most simple use case:
 * <p><blockquote><pre>
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
 * </pre></blockquote><p><p>
 * <p> A description can be placed on the exceptional conditions using {@link org.hamcrest.core.DescribedAs}, in a form
 * as follows:<p><blockquote><pre>
 *      expected = {IndexOutOfBoundsException.class, ArrayStoreException.class};
 *      assertThat(block, describedAs("\n%0 &lt;why should 0&gt;,\n%1 &lt;why should 1&gt;", is(throwingDeeply(expected)), expected))
 *  </pre></blockquote><p><p>
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
 *  </pre></blockquote><p>
 * <p> Other alternative of <code>both()</code> matcher would be Hamcrest's <code>allOf()</code> matcher:
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
 *      assertThat(block, is(throwingDeeply().andMessage("a ny|thing")));
 *  </pre></blockquote>
 * <p> The second example uses <code>Matcher<String></code> combinations on a thrown message:
 * <p><blockquote><pre>
 *      List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
 *      final ListIterator it = list.listIterator();
 *      IBlock block = new IBlock() {
 *          public void run() {
 *              throw new Exception("     a ny thin g     ");
 *          }
 *      };
 *      assertThat(block, is(throwingDeeply().andMessage(anyOf(containsString("any"), containsString("thin"), containsString("thing")))));
 *  </pre></blockquote><p>
 * <p>
 * <p> Known limitations:
 * The <code>assertThat()</code> fails as soon as identifying that no exceptions are required to throw
 * (i.e. using {@link #notThrowing(Class[])}, {@link #notThrowingDeeply(Class[])} with empty parameter list)
 * and an expected thrown message is specified (even if null) via {@link #andMessage(org.hamcrest.Matcher)}, and
 * {@link #andMessage(IsRegex)}.
 * <p>
 * @author Tibor17
 * @version 0.1
 * @since 0.1, Sep 15, 2011, 10:33:33 AM
 */
public class IsThrowing<V, B extends AbstractCallable<V>>
        extends TypeSafeMatcher<B> implements IIsThrowingMessage<V, B>, IThrownCallable<V> {
    @SuppressWarnings("unchecked")
    private static final Class<? extends Throwable>[] ALL_THROWABLE = new Class[]{Throwable.class};

    public static enum Operation {
        THROWING("throwing"),
        NOT_THROWING("notThrowing"),
        THROWING_DEEPLY("throwingDeeply"),
        NOT_THROWING_DEEPLY("notThrowingDeeply");

        private final String operation;
        private Operation(String operation) {
            this.operation = operation;
        }

        public final @Override String toString() {
            return operation;
        }
    }

    public static enum MatcherType {
        THROWABLE_CLASSES, THROWABLE_INSTANCES
    }

    private final Matcher<Class<? extends Throwable>> throwableClassesMatcher;
    private final Matcher<? extends Throwable> throwableInstancesMatcher;
    private final boolean isThrowing, showStack, isThrownMessageMatcherEnabled;
    private final StringBuilder additionalErrMsg = new StringBuilder();
    private final MatcherType matcherType;

    private Throwable caughtException;
    private ThrownMessageMatcher thrownMessageMatcher;
    private V returnedValue, fallback;
    private boolean isBlockReturnedNormally, isMatchesCalled;

    private IsThrowing() {
        throwableClassesMatcher = anyOf();
        isThrowing = showStack = isThrownMessageMatcherEnabled = false;
        matcherType = null;
        throwableInstancesMatcher = null;
    }

    private IsThrowing(Class<B> expectedType) {
        super(expectedType);
        throwableClassesMatcher = anyOf();
        isThrowing = showStack = isThrownMessageMatcherEnabled = false;
        matcherType = null;
        throwableInstancesMatcher = null;
    }

    /**
     * Specifies expected behaviour when running the block of code.
     * <p/>
     * @param operation an operation, representing operation names e.g., throwing, notThrowing, throwingDeeply, and notThrowingDeeply
     * @param isThrowing {@code true} if any of the given <tt>exceptions</tt> is expected to be thrown by calling the
     * {@link Block#run()}.
     * <p> If no exceptions are specified and this parameter is {@code true}, then any exception is expected to be
     * thrown when running the block of code.
     * <p> If {@code false}, then a complementary exception is expected to throw when running the block.
     * <p> If {@code false} and no exceptions are specified at all, then no exception is expected to be thrown
     * (complement of <em>all</em> is <em>nothing</em>).
     * @param showStack If {@code true} and an unexpected exception (other than <tt>exceptions</tt>) is thrown in
     * running block, then Java Stack Trace is passed in the description on method's parameter of
     * {@link #describeTo(org.hamcrest.Description)}.
     * @param throwableClassesMatcher Expected exceptions which belong to the particular operation. An empty array of exceptions, or
     * the {@link Throwable Throwable.class} in array represents <em>all</em> exceptions.
     */
    public IsThrowing(final Operation operation, final boolean isThrowing, final boolean showStack,
                         final Class<? extends Throwable>... throwableClassesMatcher) {
        assertNotNull("an array of exceptions must not be null", throwableClassesMatcher);
        this.throwableClassesMatcher = anyOfThrowable(operation, throwableClassesMatcher.length == 0 ? ALL_THROWABLE : throwableClassesMatcher);
        throwableInstancesMatcher = null;
        this.showStack = showStack;
        this.isThrowing = isThrowing;
        isThrownMessageMatcherEnabled = true;
        if (throwableClassesMatcher.length != 0) additionalErrMsg.append(toString(throwableClassesMatcher));
        matcherType = IsThrowing.MatcherType.THROWABLE_CLASSES;
    }

    /**
     * Specifies expected behaviour when running the block of code.
     * <p/>
     * @param isThrowing {@code true} if used by static matcher.
     * @param showStack If {@code true} and an unexpected exception is thrown in running block, then appeared Java Stack
     * Trace appears in the description on method's parameter of {@link #describeTo(org.hamcrest.Description)}.
     * @param throwableClassesMatcher A Matcher of expected exceptions which belong to 'throwing' operation. The actual exception
     * thrown by the {@link B block} is caught internally and never null.
     */
    public IsThrowing(final boolean isThrowing, final boolean showStack, final Matcher<Class<? extends Throwable>> throwableClassesMatcher) {
        assertNotNull("exceptions-matcher must not be null", throwableClassesMatcher);
        this.throwableClassesMatcher = throwableClassesMatcher;
        throwableInstancesMatcher = null;
        this.showStack = showStack;
        this.isThrowing = isThrowing;
        isThrownMessageMatcherEnabled = true;
        matcherType = IsThrowing.MatcherType.THROWABLE_CLASSES;
    }

    /**
     * Specifies expected behaviour when running the block of code.
     * <p/>
     * @param showStack If {@code true} and an unexpected exception is thrown in running block, then appeared Java Stack
     * Trace appears in the description on method's parameter of {@link #describeTo(org.hamcrest.Description)}.
     * @param throwableInstancesMatcher A Matcher of expected exceptions which belong to 'throwing' operation. The actual exception
     * thrown by the {@link B block} is caught internally. Uncaught exception instance, as a null, should be still possible to
     * evaluate in fail-safe matcher of the given one <tt>throwableInstancesMatcher</tt>.
     */
    public IsThrowing(final boolean showStack, final Matcher<? extends Throwable> throwableInstancesMatcher) {
        assertNotNull("exceptions-matcher must not be null", throwableInstancesMatcher);
        throwableClassesMatcher = null;
        this.throwableInstancesMatcher = throwableInstancesMatcher;
        this.showStack = showStack;
        this.isThrowing = true;
        isThrownMessageMatcherEnabled = false;
        matcherType = IsThrowing.MatcherType.THROWABLE_INSTANCES;
    }

    private static Matcher<Class<? extends Throwable>> anyOfThrowable(final Operation operation, final Class<? extends Throwable>... exceptions) {
        assertNotNull("an array of exceptions must not be null", exceptions);
        return anyOf(new ArrayList<Matcher<? extends Class<? extends Throwable>>>(){{
            for (final Class<? extends Throwable> t : exceptions) {
                assertNotNull("an array of exceptions in the operation "
                                + getClass().getName() + "." + operation.toString() + "()"
                                + " must not have null elements", t);
                add(assignableToThrowable(t));
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
     * Evaluates whether the block returned executed without throwing any exception.
     * @return {@code true} if block executed without throwing any exception
     */
    @Override
    public final boolean isBlockReturnedNormally() {
        return isBlockReturnedNormally;
    }

    /**
     * See {@link Callable}.
     * @return a value returned by a callable block, or a fallback when callable block threw
     */
    @Override
    public final V blockReturned() {
        return returnedValue;
    }

    /**
     * See the parameter in {@linkplain Callable constructor of Callable}.
     * @return a fallback of a callable block which returns
     */
    @Override
    public final V fallback() {
        return fallback;
    }

    /**
     * The method parameter 'block' has expected type {@link B}, and its value is not null.
     * @param block a block of testing code
     * @return {@code true} if matching expectations
     */
    @Override
    public boolean matchesSafely(B block) {
        isMatchesCalled = true;

        fallback = block.fallback;
        returnedValue = block.evaluate();
        isBlockReturnedNormally = block.isSucceeded;
        if (!isBlockReturnedNormally)
            caughtException = block.throwable;

        boolean match;

        switch (matcherType) {
            case THROWABLE_CLASSES:
                match = !isThrowing;
                if (!isBlockReturnedNormally)
                    match ^= throwableClassesMatcher.matches(caughtException.getClass());

                if (thrownMessageMatcher != null && caughtException != null) {
                    if (match) match = thrownMessageMatcher.isMatching(caughtException);
                    additionalErrMsg.append(additionalErrMsg.length() == 0 ? "" : " ")
                        .append(thrownMessageMatcher.createErrorMessage(caughtException));
                }
                break;
            case THROWABLE_INSTANCES:
                match = throwableInstancesMatcher.matches(caughtException);
                break;
            default:
                fail("unknown throwable type " + matcherType);
                throw new IllegalStateException("shouldn't appear in an unreachable statement");
        }

        return match;
    }

    @Override
    public void describeTo(Description description) {
        if (!isMatchesCalled) {
            description.appendText("<>");
            return;
        }

        final boolean showUnexpectedStackTrace = showStack & caughtException != null;
        switch (matcherType) {
            case THROWABLE_CLASSES:
                if (matchesAllThrowableClasses()) {
                    if (isThrowing) description.appendText("to throw any exception");
                    else description.appendText("not to throw exceptions at all");
                } else {
                    if (isThrowing) description.appendText("to throw exception(s)");
                    else description.appendText("not to throw exception(s)");
                }

                if (additionalErrMsg.length() != 0)
                    description.appendText(" ").appendText(additionalErrMsg.toString());

                if (showUnexpectedStackTrace)
                    describeThrown(description.appendText("\r\n"), caughtException);
                break;
            case THROWABLE_INSTANCES:
                throwableInstancesMatcher.describeTo(description.appendText("throwing "));
                if (showUnexpectedStackTrace)
                    describeThrown(description.appendText("\r\n"), caughtException);
                break;
            default:
                fail("unknown throwable type " + matcherType);
                throw new IllegalStateException("shouldn't appear in an unreachable statement");
        }
    }

    private static Description describeThrown(final Description description, final Throwable thrown) {
        description.appendText(thrown.toString()).appendText("\r\n");
        for (final StackTraceElement s : thrown.getStackTrace())
            description.appendText(s.toString()).appendText("\r\n");
        return description;
    }

    private boolean matchesAllThrowableClasses() {
        try {
            return throwableClassesMatcher.matches(Throwable.class);
        } catch (VirtualMachineError e) {
            throw e;
        } catch (Throwable t) {
            fail(t.getLocalizedMessage());
            throw new IllegalStateException("unreachable statement");
        }
    }

    @Override
    public final Matcher<B> withMessage(final String expectedMessage) {
        assertTrue("\"withMessage\" enabled only by matcher *throwing*(Class<Throwable>...) " +
                    "and *throwing*(Matcher<Class<Throwable>>)", isThrownMessageMatcherEnabled);
        return andMessage(new BaseMatcher<String>() {
            @Override
            public boolean matches(Object actualMessage) {
                return expectedMessage == null && actualMessage == null
                        || expectedMessage != null && expectedMessage.equals(actualMessage);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expectedMessage == null ?
                        "null message" : ("a message equal to " + expectedMessage));
            }
        });
    }

    @Override
    public final Matcher<B> andMessage(final Matcher<String> matchWith) {
        assertNotNull("string-matcher must not be null", matchWith);
        assertTrue("\"andMessage\" enabled only by matcher *throwing*(Class<Throwable>...) " +
                    "and *throwing*(Matcher<Class<Throwable>>)", isThrownMessageMatcherEnabled);
        thrownMessageMatcher = new ThrownMessageMatcher("and message", "and message", "", " ", "", "is ") {
            @Override protected boolean isMatching(Throwable thrown) { return isMatchingMessageByHamcrest(thrown, matchWith); }
            @Override protected boolean hasNullMessage(Throwable thrown) { return IsThrowing.hasNullMessage(thrown); }
            @Override protected String getActualMessage(Throwable thrown) { return ""/*getMessage(thrown)*/; }
            @Override protected Matcher<? extends CharSequence> getExpectedMatcher() { return matchWith; }
            @Override protected String getExpectedMessage() { return null; }
        };
        return this;
    }

    @Override
    public final Matcher<B> andMessage(final IsRegex<CharSequence> regex) {
        assertNotNull("regular expression matcher must not be null", regex);
        assertTrue("\"andMessage\" enabled only by matcher *throwing*(Class<Throwable>...) " +
                    "and *throwing*(Matcher<Class<Throwable>>)", isThrownMessageMatcherEnabled);
        thrownMessageMatcher = new ThrownMessageMatcher("and message", "and message", "", " ", "", "is ") {
            @Override protected boolean isMatching(Throwable thrown) { return isMatchingMessageByRegex(thrown, regex); }
            @Override protected boolean hasNullMessage(Throwable thrown) { return IsThrowing.hasNullMessage(thrown); }
            @Override protected String getActualMessage(Throwable thrown) { return ""/*getMessage(thrown)*/; }
            @Override protected Matcher<? extends CharSequence> getExpectedMatcher() { return regex; }
            @Override protected String getExpectedMessage() { return null; }
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

    private static boolean isMatchingMessageByRegex(final Throwable thrown, final IsRegex<CharSequence> regex) {
        assertNotNull("(1) an instance of thrown exception must not be NULL", thrown);
        assertNotNull("regular expression must not be null", regex);
        final String msg = getMessage(thrown);
        assertNotNull("observed exception which produced unacceptable NULL message", msg);
        return regex.matchesSafely(msg);
    }

    private static <T> boolean isMatchingMessageByHamcrest(final Throwable thrown, final Matcher<T> matchWith) {
        assertNotNull("(3) an instance of thrown exception must not be NULL", thrown);
        assertNotNull("hamcrest matcher must not be null", matchWith);
        return matchWith.matches(getMessage(thrown));
    }

    @org.hamcrest.Factory
    public static <A extends AbstractCallable<Void>> IIsThrowingMessage<Void, A> throwing(final Class<? extends Throwable>... expectedExceptions) {
        return new IsThrowing<Void, A>(Operation.THROWING, true, false, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static <V, A extends AbstractCallable<V>> IsThrowing<V, A> throwing(final Matcher<Class<? extends Throwable>> expectedExceptions) {
        return new IsThrowing<V, A>(true, false, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static <V, A extends AbstractCallable<V>> IsThrowing<V, A> throwingDeeply(final Matcher<Class<? extends Throwable>> expectedExceptions) {
        return new IsThrowing<V, A>(true, true, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static <V> Matcher<AbstractCallable<V>> throwing(final Matcher<? extends Throwable> expectedExceptions) {
        return new IsThrowing<V, AbstractCallable<V>>(false, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static <V> Matcher<AbstractCallable<V>> throwingDeeply(final Matcher<? extends Throwable> expectedExceptions) {
        return new IsThrowing<V, AbstractCallable<V>>(true, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static <A extends AbstractCallable<Void>> IsThrowing<Void, A> notThrowing(final Class<? extends Throwable>... exceptionsComplement) {
        return new IsThrowing<Void, A>(Operation.NOT_THROWING, false, false, exceptionsComplement);
    }

    @org.hamcrest.Factory
    public static <A extends AbstractCallable<Void>> IIsThrowingMessage<Void, A> throwingDeeply(final Class<? extends Throwable>... expectedExceptions) {
        return new IsThrowing<Void, A>(Operation.THROWING_DEEPLY, true, true, expectedExceptions);
    }

    @org.hamcrest.Factory
    public static <A extends AbstractCallable<Void>> IsThrowing<Void, A> notThrowingDeeply(final Class<? extends Throwable>... exceptionsComplement) {//vsetko
        return new IsThrowing<Void, A>(Operation.NOT_THROWING_DEEPLY, false, true, exceptionsComplement);
    }
}
