package org.junit.contrib.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertNotNull;

/**
 * Skeleton class for {@link StartPosition} and {@link EndPosition}.
 * Determines a position index
 * (as a result of {@link java.util.regex.Matcher#start(int)} or {@link java.util.regex.Matcher#end(int)})
 * been matched with the given hamcrest's matcher <tt>expectedIndex</tt> as expected one.
 * <p>
 * This is performed by {@link #matchExpectedIndex(int)}. The mismatched result enables the caller
 * to retrieve failure description via {@link #getFailureDescription()} after method the
 * {@link #matchExpectedIndex(int)} returned {@code false}.
 * <p>
 * The method {@link #isEvaluated()} can be called after {@link #matchExpectedIndex(int)}. Thus the method
 * {@link #isEvaluated()} always returns {@code true} if, and only if, {@link #matchExpectedIndex(int)} was
 * called before with whatever value it returned.
 */
abstract class Position {
    protected final ExpectedMatcher<Integer> expectedIndex;
    protected final int group;
    private String failureDescription;

    protected abstract boolean isEvaluated();

    static final class ExpectedMatcher<T> extends BaseMatcher<T> {
        private final Matcher<T> expectedIndex;
        private boolean isExpectedIndexMatched, isExpectedIndexEvaluated;

        private ExpectedMatcher(Matcher<T> expectedIndex) {
            assertNotNull("given position matcher must not be null", expectedIndex);
            this.expectedIndex = expectedIndex;
        }

        @Override public boolean matches(Object o) {
            isExpectedIndexEvaluated = true;
            return isExpectedIndexMatched = expectedIndex.matches(o);
        }

        boolean isExpectedIndexEvaluated() {
            return isExpectedIndexEvaluated;
        }

        boolean isExpectedIndexMatched() {
            return isExpectedIndexMatched;
        }

        @Override
        public void describeTo(Description description) {
            expectedIndex.describeTo(description);
        }
    }

    Position(int group, Matcher<Integer> expectedIndex) {
        this.group = group;
        this.expectedIndex = new ExpectedMatcher<Integer>(expectedIndex);
    }

    protected final boolean matchExpectedIndex(int actualIndex) {
        final boolean matches = expectedIndex.matches(actualIndex);
        if (!matches) failureDescription = failureAfterMatch();
        return matches;
    }

    protected final String getFailureDescription() {
        return failureDescription;
    }

    private String failureAfterMatch() {
        final Description description = new InternalDescription();
        expectedIndex.describeTo(description);
        return description.toString();
    }
}