package org.junit.contrib.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

class Position {
    protected final Matcher<Integer> expectedIndex;
    protected final int group;

    private String failureDescription;

    Position(int group, Matcher<Integer> expectedIndex) {
        this.group = group;
        this.expectedIndex = expectedIndex;
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