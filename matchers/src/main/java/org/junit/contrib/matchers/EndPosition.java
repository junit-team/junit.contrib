package org.junit.contrib.matchers;

import org.hamcrest.Matcher;

/**
 * Determines whether the {@link java.util.regex.Matcher} has end position matching
 * the regex criteria by {@link #end(java.util.regex.Matcher)}. In prior the caller
 * must satisfy the call {@link java.util.regex.Matcher#find(int)}.
 * <p>
 * If a failure appears after calling {@link #end(java.util.regex.Matcher)}, then the
 * method {@link #hasFailure()} returns {@code true} and the failure description is
 * retrieved by the return from {@link #getFailureDescription()}.
 */
final class EndPosition extends Position {
    private int end = -1;
    private boolean matches, isEvaluated;

    @Override
    protected boolean isEvaluated() {
        return isEvaluated;
    }

    EndPosition(int group, Matcher<Integer> expectedIndex) {
        super(group, expectedIndex);
    }

    boolean end(final java.util.regex.Matcher regexMatcher) {
        isEvaluated = true;
        try {
            end = regexMatcher.end(group);
            matches = end != -1 && matchExpectedIndex(end);
        } catch (IllegalStateException e) {
            matches = false;
        }
        return matches;
    }

    boolean hasFailure() {
        return !matches && getFailureDescription() != null;
    }

    @Override public String toString() {
        return end == -1 ? "" : "(per group " + group + ") to exclusive end position " + end;
    }
}