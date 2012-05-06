package org.junit.contrib.matchers;

import org.hamcrest.Matcher;

/**
 * Determines whether the {@link java.util.regex.Matcher} has start position matching
 * the regex criteria by {@link #start(java.util.regex.Matcher)}. In prior the caller
 * must satisfy the call {@link java.util.regex.Matcher#find(int)}.
 * <p>
 * If a failure appears after calling {@link #start(java.util.regex.Matcher)}, then the
 * method {@link #hasFailure()} returns {@code true} and the failure description is
 * retrieved by the return from {@link #getFailureDescription()}.
 */
final class StartPosition extends Position {
    private int start = -1;
    private boolean matches, isEvaluated;

    @Override
    protected boolean isEvaluated() {
        return isEvaluated;
    }

    StartPosition(int group, Matcher<Integer> expectedIndex) {
        super(group, expectedIndex);
    }

    boolean start(final java.util.regex.Matcher regexMatcher) {
        isEvaluated = true;
        try {
            start = regexMatcher.start(group);
            matches = start != -1 && matchExpectedIndex(start);
        } catch (IllegalStateException e) {
            matches = false;
        }
        return matches;
    }

    boolean hasFailure() {
        return !matches && getFailureDescription() != null;
    }

    @Override public String toString() {
        return start == -1 ? "" : "(per group " + group + ") from start position " + start;
    }
}