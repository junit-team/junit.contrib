package org.junit.contrib.matchers;

import org.hamcrest.BaseDescription;

/**
 * The purpose of this class is to handle description string
 * by use of {@link org.hamcrest.Matcher#describeTo(org.hamcrest.Description)}
 * as follows:
 *  <p><blockquote><pre>
 *      org.hamcrest.Matcher m = ...;
 *      InternalDescription d = new InternalDescription();
 *      m.describeTo(d);
 *      String failureDescription = d.toString();
 *      // use of failureDescription
 *  </pre></blockquote>
 * <p>
 */
final class InternalDescription extends BaseDescription {
    private final StringBuilder msg;

    public InternalDescription() {
        msg = new StringBuilder(128);
    }

    public InternalDescription(String description) {
        msg = new StringBuilder(description);
    }

    @Override protected void append(char c) {
        msg.append(c);
    }

    @Override public String toString() {
        return msg.toString();
    }
}