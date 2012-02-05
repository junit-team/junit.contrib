package org.junit.contrib.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import static org.junit.Assert.assertNotNull;

/**
 * Matches the actual reference type with the given type.
 * <p>
 * If the actual type is same with or a subtype, in other words assignable, to the given expected type in the
 * methods {@link #assignableTo(Class)}, {@link #assignableToAny(Class)}, {@link #assignableToThrowable(Class)}.
 * <p>
 * The method {@link #assignableTo(Class)} uses same generic type on the returned value and method parameter.
 * <p>
 * The method {@link #assignableToAny(Class)} allows to use wildcards <tt>Class<?></tt>.
 * <p>
 * The last method {@link #assignableToThrowable(Class)}
 * is suitable in use of exceptions, like it is in the {@linkplain IsThrowing IsThrowing matcher}.
 * <p>
 * @param <A> an actual type
 * @param <E> an expected type
 */
public class IsAssignableTo<A extends Class<?>, E extends Class<?>> extends TypeSafeMatcher<A> {
    private final E superOrSameType;

    private A subOrSameType;

    public IsAssignableTo(final E superOrSameType) {
        assertNotNull("expected 'superOrSameType' must not be null", superOrSameType);
        this.superOrSameType = superOrSameType;
    }

    @Override
    public boolean matchesSafely(final A a) {
        subOrSameType = a;
        return superOrSameType.isAssignableFrom(subOrSameType);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("assignable to ")
                .appendText(superOrSameType.getName());
    }

    @org.hamcrest.Factory
    public static <T> Matcher<Class<T>> assignableTo(final Class<T> superOrSameType) {
        return new IsAssignableTo<Class<T>, Class<T>>(superOrSameType);
    }

    @org.hamcrest.Factory
    public static Matcher<Class<?>> assignableToAny(final Class<?> superOrSameType) {
        return new IsAssignableTo<Class<?>, Class<?>>(superOrSameType);
    }

    @org.hamcrest.Factory
    public static Matcher<Class<? extends Throwable>> assignableToThrowable(final Class<? extends Throwable> superOrSameType) {
        return new IsAssignableTo<Class<? extends Throwable>, Class<? extends Throwable>>(superOrSameType);
    }
}
