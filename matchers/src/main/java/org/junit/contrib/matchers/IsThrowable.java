package org.junit.contrib.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import static org.junit.Assert.assertNotNull;

/**
 * This is an utility of matchers mainly used together with the {@linkplain IsThrowing IsThrowing matcher}.
 * The matchers are used to match the type of an exception, cause, message, and localized message. Thus the
 * the returned reference is a matcher which corresponds to a reference of an actual exception. So the following
 * methods are called for certain matchers:
 * <p>
 *  <ul>
 *      <li> {@linkplain Object#getClass() getClass()} for {@linkplain #type(org.hamcrest.Matcher) the type matcher}
 *      <li> {@linkplain Throwable#getCause() getCause()} for {@linkplain #cause(org.hamcrest.Matcher) the cause matcher}
 *      <li> {@linkplain Throwable#getMessage() getMessage()} for {@linkplain #message(org.hamcrest.Matcher) the message matcher}
 *      <li> {@linkplain Throwable#getLocalizedMessage() getLocalizedMessage()} for {@linkplain #localizedMessage(org.hamcrest.Matcher)} the localized message matcher
 *  </ul>
 * <p>
 * <p>
 * As an use case, this assert will succeed because the type of actual instance of
 * exception of {@link ArrayIndexOutOfBoundsException} is a subtype of the expected type
 * {@link IndexOutOfBoundsException} declared by the combination of matchers
 * <tt>type(assignableTo(IndexOutOfBoundsException.class))</tt>:
 * <p>
 *  <blockquote>
 *      <pre>
 *          assertThat(new ArrayIndexOutOfBoundsException(),
                    is(type(assignableTo(IndexOutOfBoundsException.class))));
 *      </pre>
 *  </blockquote>
 * </p>
 */
public final class IsThrowable {
    private IsThrowable() throws IllegalAccessException {
        throw new IllegalAccessException("constructor not reachable");
    }

    @org.hamcrest.Factory
    public static <A extends Throwable> Matcher<A> localizedMessage(final Matcher<String> expectedLocalizedMessage) {
        assertNotNull("expectedLocalizedMessage is null", expectedLocalizedMessage);
        return new TypeSafeMatcher<A>() {
            private boolean isMatchesCalled;

            @Override public boolean matchesSafely(A e) {
                isMatchesCalled = true;
                return expectedLocalizedMessage.matches(e.getLocalizedMessage());
            }

            @Override public void describeTo(Description description) {
                if (isMatchesCalled) description.appendText("localized message ")
                        .appendDescriptionOf(expectedLocalizedMessage);
                else description.appendText("<>");
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable, E extends CharSequence> Matcher<A> localizedMessage(final IsRegex<E> expectedLocalizedMessagePattern) {
        assertNotNull("expectedLocalizedMessagePattern is null", expectedLocalizedMessagePattern);
        return new TypeSafeMatcher<A>() {
            private boolean isMatchesCalled, isNullMessage;

            @Override public boolean matchesSafely(A a) {
                isMatchesCalled = true;
                final String msg = a.getMessage();
                isNullMessage = msg == null;
                return !isNullMessage && expectedLocalizedMessagePattern.matches(msg);
            }

            @Override public void describeTo(Description description) {
                if (!isMatchesCalled) description.appendText("<>");
                else if (isNullMessage) description.appendText("localized message <pattern>");
                else description.appendText("localized message ").appendDescriptionOf(expectedLocalizedMessagePattern);
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable> Matcher<A> message(final Matcher<String> expectedMessage) {
        assertNotNull("expectedMessage is null", expectedMessage);
        return new TypeSafeMatcher<A>() {
            private boolean isMatchesCalled;

            @Override public boolean matchesSafely(A a) {
                isMatchesCalled = true;
                return expectedMessage.matches(a.getMessage());
            }

            @Override public void describeTo(Description description) {
                if (isMatchesCalled) description.appendText("message ").appendDescriptionOf(expectedMessage);
                else description.appendText("<>");
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable, E extends CharSequence> Matcher<A> message(final IsRegex<E> expectedMessagePattern) {
        assertNotNull("expectedMessagePattern is null", expectedMessagePattern);
        return new TypeSafeMatcher<A>() {
            private boolean isMatchesCalled, isNullMessage;

            @Override public boolean matchesSafely(A a) {
                isMatchesCalled = true;
                final String msg = a.getMessage();
                isNullMessage = msg == null;
                return !isNullMessage && expectedMessagePattern.matches(msg);
            }

            @Override public void describeTo(Description description) {
                if (!isMatchesCalled) description.appendText("<>");
                else if (isNullMessage) description.appendText("message <pattern>");
                else description.appendText("message ").appendDescriptionOf(expectedMessagePattern);
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable, E extends Throwable> Matcher<A> type(final Matcher<Class<E>> expectedType) {
        assertNotNull("expectedType is null", expectedType);
        return new TypeSafeMatcher<A>() {
            private boolean isMatchesCalled;
            @Override public boolean matchesSafely(A a) {
                isMatchesCalled = true;
                return expectedType.matches(a.getClass());
            }

            @Override public void describeTo(Description description) {
                if (isMatchesCalled) description.appendText("type ").appendDescriptionOf(expectedType);
                else description.appendText("<>");
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable, E extends Throwable> Matcher<A> cause(final Matcher<E> expectedCause) {
        assertNotNull("expectedCause is null", expectedCause);
        return new TypeSafeMatcher<A>() {
            private boolean isMatchesCalled;
            @Override public boolean matchesSafely(A a) {
                isMatchesCalled = true;
                return expectedCause.matches(a.getCause());
            }

            @Override public void describeTo(Description description) {
                if (isMatchesCalled) description.appendText("cause ").appendDescriptionOf(expectedCause);
                else description.appendText("<>");
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable> Matcher<A> any() {
        return new BaseMatcher<A>() {
            private boolean isMatchesCalled;
            @Override
            public boolean matches(Object thrown) {
                isMatchesCalled = true;
                return thrown instanceof Throwable;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(isMatchesCalled ? "any Throwable" : "<>");
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable, E extends Throwable> Matcher<A> complementOf(final Matcher<E> unexpectedCause) {
        assertNotNull("unexpectedCause is null", unexpectedCause);
        return new TypeSafeMatcher<A>() {
            private boolean isMatchesCalled;
            @Override
            public boolean matchesSafely(A a) {
                isMatchesCalled = true;
                return !unexpectedCause.matches(a);
            }

            @Override
            public void describeTo(Description description) {
                if (isMatchesCalled) description.appendText("complement of ")
                        .appendDescriptionOf(unexpectedCause);
                else description.appendText("<>");
            }
        };
    }

    @org.hamcrest.Factory
    public static <A extends Throwable> Matcher<A> none() {
        return new BaseMatcher<A>() {
            private boolean isMatchesCalled;
            @Override
            public boolean matches(Object a) {
                isMatchesCalled = true;
                return !(a instanceof Throwable);
            }

            @Override
            public void describeTo(Description description) {
                if (isMatchesCalled) description.appendText("none");
                else description.appendText("<>");
            }
        };
    }
}
