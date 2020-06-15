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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertThat;
import static org.junit.contrib.matchers.IsAssignableTo.assignableTo;
import static org.junit.contrib.matchers.IsThrowable.*;

// junit & hamcrest matchers

/**
 * The purpose of this test is to test a functionality of
 * {@link IsThrowable} matcher and to explain the use.
 * <p/>
 * @author Tibor17
 * @version 0.1
 * @see IsThrowable
 * @since 0.1, Dec 11, 2011, 7:07:32 PM
 */
public final class TestIsThrowable {
    @Rule
    public final ExpectedException expectedExceptionRule = ExpectedException.none();

    @Test
    public void isType$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is type <class java.lang.NullPointerException>\n" +
                "     got: <java.lang.ArrayIndexOutOfBoundsException>");

        assertThat(new ArrayIndexOutOfBoundsException(), is(type(equalTo(NullPointerException.class))));
    }

    @Test
    public void isType() {
        assertThat(new ArrayIndexOutOfBoundsException(), is(type(equalTo(ArrayIndexOutOfBoundsException.class))));
    }

    @Test
    public void isAssignableType$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is type assignable to java.lang.ArrayIndexOutOfBoundsException\n" +
                "     got: <java.lang.IndexOutOfBoundsException>");

        assertThat(new IndexOutOfBoundsException(), is(type(assignableTo(ArrayIndexOutOfBoundsException.class))));
    }

    @Test
    public void isAssignableType() {
        assertThat(new ArrayIndexOutOfBoundsException(), is(type(assignableTo(IndexOutOfBoundsException.class))));
    }

    @Test
    public void isCause$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: cause <java.io.NotSerializableException>\n" +
                "     got: <java.io.IOException: java.io.NotSerializableException>");

        final ObjectStreamException actualCause = new NotSerializableException();
        final ObjectStreamException fake = new NotSerializableException();
        assertThat(new IOException(actualCause), cause(equalTo(fake)));
    }

    @Test
    public void isCause() {
        final NotSerializableException actualCause = new NotSerializableException();
        final ObjectStreamException expectedCause = actualCause;
        assertThat(new IOException(actualCause), cause(is(expectedCause)));
    }

    @Test
    public void isMessage$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: message is \"any message\"\n" +
                "     got: <org.junit.contrib.matchers.TestException: a test localized message>");

        Exception actual = new TestException("a test message", "a test localized message");

        assertThat(actual, message(is("any message")));
    }

    @Test
    public void isMessage() {
        Exception actual = new TestException("a test message", "a test localized message");
        assertThat(actual, message(is("a test message")));
    }

    @Test
    public void isLocalizedMessage$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: localized message is \"any localized message\"\n" +
                "     got: <org.junit.contrib.matchers.TestException: a test localized message>");

        Exception actual = new TestException("a test message", "a test localized message");

        assertThat(actual, localizedMessage(is("any localized message")));
    }

    @Test
    public void isLocalizedMessage() {
        Exception actual = new TestException("a test message", "a test localized message");
        assertThat(actual, localizedMessage(is("a test localized message")));
    }

    @Test
    public void none$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("none");
        Exception actual = new Exception();
        assertThat(actual, is(none()));
    }

    @Test
    public void noNe() {
        assertThat(null, is(none()));
    }

    @Test
    public void complement$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is complement of (type assignable to java.lang.Error or <>)\n" +
                "     got: <java.lang.IllegalAccessError>");

        assertThat(new IllegalAccessError(),
                is(complementOf(anyOf(type(assignableTo(Error.class)),
                                        type(assignableTo(RuntimeException.class))))));
    }

    @Test
    public void complement() {
        assertThat(new IOException(),
                is(complementOf(anyOf(type(assignableTo(Error.class)),
                                        type(assignableTo(RuntimeException.class))))));
    }
}
