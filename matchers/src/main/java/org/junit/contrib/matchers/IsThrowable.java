package org.junit.contrib.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public final class IsThrowable {
    public static <E extends Throwable> Matcher<E> localizedMessage(final Matcher<String> expectedLocalizedMessage) {
        return new BaseMatcher<E>() {
            @SuppressWarnings("unchecked")
            @Override public boolean matches(Object e) {
                return expectedLocalizedMessage.matches(((E) e).getLocalizedMessage());
            }

            @Override public void describeTo(Description description) {
                expectedLocalizedMessage.describeTo(description);
            }
        };
    }

    public static <E extends Throwable> Matcher<E> message(final Matcher<String> expectedMessage) {
        return new BaseMatcher<E>() {
            @SuppressWarnings("unchecked")
            @Override public boolean matches(Object e) {
                return expectedMessage.matches(((E) e).getMessage());
            }

            @Override public void describeTo(Description description) {
                expectedMessage.describeTo(description);
            }
        };
    }

    public static <E extends Throwable> Matcher<E> type(final Matcher<Class<? extends Throwable>> expectedType) {
        return new TypeSafeMatcher<E>() {
            @Override public boolean matchesSafely(E e) {
                return expectedType.matches(e.getClass());
            }

            @Override public void describeTo(Description description) {
                expectedType.describeTo(description);
            }
        };
    }

    public static <E extends Throwable> Matcher<E> cause(final Matcher<? extends Throwable> expectedLocalizedMessage) {
        return new BaseMatcher<E>() {
            @SuppressWarnings("unchecked")
            @Override public boolean matches(Object e) {
                return expectedLocalizedMessage.matches(((E) e).getCause());
            }

            @Override public void describeTo(Description description) {
                expectedLocalizedMessage.describeTo(description);
            }
        };
    }
}
