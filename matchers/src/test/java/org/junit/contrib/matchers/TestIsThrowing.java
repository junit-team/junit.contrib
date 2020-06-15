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
import java.util.*;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.DescribedAs.describedAs;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.contrib.matchers.IsAssignableTo.assignableTo;
import static org.junit.contrib.matchers.IsRegex.like;
import static org.junit.contrib.matchers.IsRegex.match;
import static org.junit.contrib.matchers.IsThrowable.localizedMessage;
import static org.junit.contrib.matchers.IsThrowable.type;
import static org.junit.contrib.matchers.IsThrowing.*;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.containsString;

// junit & hamcrest matchers

/**
 * The purpose of this test is to test a functionality of
 * {@link IsThrowing} matcher and to explain the use.
 * <p/>
 * @author Tibor17
 * @version 0.1
 * @see IsThrowing
 * @since 0.1, Oct 3, 2011, 7:07:57 PM
 */
public final class TestIsThrowing {
    @Rule
    public final ExpectedException expectedExceptionRule = ExpectedException.none();

    @Test
    public void throwing$1() {
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(throwing(ArrayIndexOutOfBoundsException.class)));
        array[1] = array[0];
    }

    @Test
    public void throwing$2() {
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(throwing(IndexOutOfBoundsException.class)));
        array[1] = array[0];
    }

    @Test
    public void throwing$3() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is not to throw any exception");
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(not(throwing())));
        array[1] = array[0];
    }

    @Test
    public void throwing$3$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage(both(containsString("Expected: is not to throw any exception\r\n" +
                "java.lang.ArrayIndexOutOfBoundsException: 2\r\n" +
                "org.junit.contrib.matchers.TestIsThrowing$"))
                .and(containsString("got: <block threw java.lang.ArrayIndexOutOfBoundsException: 2>")));
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(not(throwingDeeply())));
        array[1] = array[0];
    }

    @Test
    public void throwing$3$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage(both(containsString("Expected: is not to throw exceptions at all\r\n" +
                "java.lang.ArrayIndexOutOfBoundsException: 2\r\n" +
                "org.junit.contrib.matchers.TestIsThrowing$"))
                .and(containsString("got: <block threw java.lang.ArrayIndexOutOfBoundsException: 2>")));
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(notThrowingDeeply()));
        array[1] = array[0];
    }

    @Test
    public void throwing$4() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is not to throw exceptions at all");
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(notThrowing()));
        array[1] = array[0];
    }

    @Test
    public void throwing$5$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage(both(containsString("Expected: is to throw exception(s) [java.lang.IndexOutOfBoundsException]\r\n"))
                .and(containsString("org.junit.contrib.matchers.TestIsThrowing$"))
                .and(containsString("     got: <block threw java.lang.ArrayStoreException: java.lang.String>")));
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };
        assertThat(block, is(throwingDeeply(IndexOutOfBoundsException.class)));
        array[0] = array[1];
    }

    @Test
    public void throwing$5() {
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };
        assertThat(block, is(not(throwingDeeply(IndexOutOfBoundsException.class))));
        array[0] = array[1];
    }

    @Test
    public void throwing$6() {
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };
        assertThat(block, is(notThrowingDeeply(IndexOutOfBoundsException.class)));
        array[0] = array[1];
    }

    @Test
    public void throwing$7() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is not to throw exception(s) " +
                "[java.lang.IndexOutOfBoundsException, java.lang.ArrayStoreException]\n" +
                "     got: <block threw java.lang.ArrayStoreException: java.lang.String>");

        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };

        assertThat(block, is(not(throwing(IndexOutOfBoundsException.class, ArrayStoreException.class))));

        array[0] = array[1];
    }

    @Test
    public void throwing$8() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage(both(containsString("Expected: is not to throw exception(s) " +
                "[java.lang.IndexOutOfBoundsException, java.lang.ArrayStoreException]\r\n"))
                .and(containsString("     got: <block threw java.lang.ArrayStoreException: java.lang.String>")));

        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };

        assertThat(block, is(not(throwingDeeply(IndexOutOfBoundsException.class, ArrayStoreException.class))));

        array[0] = array[1];
    }

    @Test
    public void throwing$9() {
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };
        assertThat(block, is(throwing(IndexOutOfBoundsException.class, ArrayStoreException.class)));
        array[0] = array[1];
    }

    @Test
    public void throwing$10() {
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };
        assertThat(block, is(throwingDeeply(IndexOutOfBoundsException.class, ArrayStoreException.class)));
        array[0] = array[1];
    }

    @Test
    public void throwingDescribedAs() {
        final Integer[] array = new Integer[2];
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                Object[] copy = array;
                copy[new Random().nextInt(4)] = "wrong type to assign";
            }
        };
        @SuppressWarnings("unchecked")
        Class<Throwable>[] expected = new Class[] {IndexOutOfBoundsException.class, ArrayStoreException.class};
        assertThat(block, describedAs("\n%0 might be thrown due to the original array length is " + array.length + "," +
                "\nor %1 must be thrown because only " + array.getClass().getComponentType().getName() + " is accepted\n",
                is(throwingDeeply(expected)),
                expected));
        array[0] = array[1];
    }

    @Test
    @SuppressWarnings("unchecked")
    public void throwingMutualBlock$0() {
        expectedExceptionRule.expect(ClassCastException.class);
        List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
        final ListIterator it = list.listIterator();
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                it.next();
                it.next();
                it.set("a");
            }
        };
        assertThat(block, both(is(throwingDeeply(NoSuchElementException.class))).and(is(notThrowing())));
        list.set(0, new Object());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void throwingMutualBlock$1() {
        expectedExceptionRule.expect(ClassCastException.class);
        List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
        final ListIterator it = list.listIterator();
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                it.next();
                it.next();
                it.set("a");
            }
        };
        assertThat(block, allOf(is(notThrowing()), is(throwingDeeply(NoSuchElementException.class))));
        list.set(0, new Object());
    }

    @Test
    public void thrownOkMessageOk$0() {
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() throws Exception {
                throw new Exception("     a ny thin g     ");
            }
        };
        assertThat(block, is(throwingDeeply().andMessage(like("a ny|thing"))));
    }

    @Test
    public void thrownOkMessageWrong$0() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage(both(containsString("Expected: " +
                "is to throw any exception and message is like \"any|thing\"\r\n"))
                .and(containsString("org.junit.contrib.matchers.TestIsThrowing$"))
                .and(containsString("     got: <block threw java.lang.Exception:      an y thin g     >")));

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() throws Exception {
                throw new Exception("     an y thin g     ");
            }
        };

        assertThat(block, is(throwingDeeply().andMessage(like("any|thing"))));
    }

    @Test
    public void thrownOkNullMessageWrong$0() {
        final AbstractCallable<Void> block = new Block<Void>() {
            @Override
            protected void run() {
                @SuppressWarnings({"unchecked"})
                final AbstractCallable<Void> block = new Block() {
                    @Override protected void run() throws Exception {
                        throw new Exception((String) null);
                    }
                };
                assertThat(block, is(throwingDeeply().andMessage(like("any|thing"))));

            }
        };
        assertThat(block, throwing(java.lang.AssertionError.class)
                .withMessage("observed exception which produced unacceptable NULL message"));

    }

    @Test
    public void thrownOkMessageOk$1() {
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() throws Exception {
                throw new Exception("     a ny thin g     ");
            }
        };
        assertThat(block, is(throwingDeeply().andMessage(anyOf(containsString("any"), containsString("thin"), containsString("thing")))));
    }

    @Test
    public void thrownOkMessageWrong$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage(both(containsString("Expected: " +
                "is to throw any exception and message is " +
                "(a string containing \"any\" or a string containing \"thing\")\r\n"))
                .and(containsString("org.junit.contrib.matchers.TestIsThrowing$"))
                .and(containsString("     got: <block threw java.lang.Exception:      an y thin g     >")));

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() throws Exception {
                throw new Exception("     an y thin g     ");
            }
        };

        assertThat(block, is(throwingDeeply().andMessage(anyOf(containsString("any"), containsString("thing")))));
    }

    @Test
    public void thrownOkNullMessageWrong$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is to throw any exception " +
                "and message " +
                "is (a string containing \"any\" or a string containing \"thing\")");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() throws Exception {
                throw new Exception((String) null);
            }
        };

        assertThat(block, is(throwingDeeply().andMessage(anyOf(containsString("any"), containsString("thing")))));
    }

    @Test
    public void matchException$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is throwing type assignable to java.io.IOException\n" +
                "     got: <block threw java.lang.RuntimeException>");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new RuntimeException();
            }
        };
        assertThat(block, is(throwing(type(assignableTo(IOException.class)))));
    }

    @Test
    public void matchException() {
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException();
            }
        };
        assertThat(block, is(throwing(type(assignableTo(RuntimeException.class)))));
    }

    @Test
    public void matchExceptionWithMessage$NegativeTest$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is throwing (localized message \"non-null value\" and (<> or <>))\n" +
                "     got: <block threw java.lang.NullPointerException: null value>");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("null value");
            }
        };

        assertThat(block, is(throwing(
                allOf(localizedMessage(equalTo("non-null value")),
                        anyOf(type(assignableTo(IllegalArgumentException.class)), type(assignableTo(NullPointerException.class)))))));
    }

    @Test
    public void matchExceptionWithMessage$NegativeTest$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is throwing ((type assignable to java.lang.IllegalArgumentException " +
                "or type assignable to java.lang.NullPointerException) " +
                "and localized message \"non-null value\")\n" +
                "     got: <block threw java.lang.NullPointerException: null value>");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("null value");
            }
        };

        assertThat(block, is(throwing(
                both(localizedMessage(equalTo("non-null value")))
                        .and(anyOf(type(assignableTo(IllegalArgumentException.class)), type(assignableTo(NullPointerException.class)))))));
    }

    @Test
    public void matchExceptionWithMessage$NegativeTest$3() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is throwing ((type assignable to java.lang.IllegalArgumentException " +
                "or type assignable to java.lang.NullPointerException) and localized message like \"non-null value\")\n" +
                "     got: <block threw java.lang.NullPointerException: null value>");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("null value");
            }
        };

        assertThat(block, is(throwing(
                both(localizedMessage(like("non-null value")))
                        .and(anyOf(type(assignableTo(IllegalArgumentException.class)), type(assignableTo(NullPointerException.class)))))));
    }

    @Test
    public void matchExceptionWithMessage$NegativeTest$4() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is throwing ((type assignable to java.lang.IllegalArgumentException " +
                "or type assignable to java.lang.NullPointerException) " +
                "and localized message match \"non-null value\")\n" +
                "     got: <block threw java.lang.NullPointerException: null value>");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("null value");
            }
        };

        assertThat(block, is(throwing(
                both(localizedMessage(match("non-null value")))
                        .and(anyOf(type(assignableTo(IllegalArgumentException.class)), type(assignableTo(NullPointerException.class)))))));
    }

    @Test
    public void matchExceptionWithMessage$1() {
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("null value");
            }
        };

        assertThat(block, is(throwing(
                allOf(localizedMessage(is("null value")),
                        anyOf(type(assignableTo(IllegalArgumentException.class)), type(assignableTo(NullPointerException.class)))))));
    }

    @Test
    public void matchExceptionWithMessage$2() {
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Object> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("null value");
            }
        };
        assertThat(block, is(throwing(
                both(localizedMessage(is("null value")))
                        .and(anyOf(type(assignableTo(IllegalArgumentException.class)), type(assignableTo(NullPointerException.class)))))));
    }

    @Test
    public void matchExceptionWithMessageRegex$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is to throw exception(s) [java.lang.RuntimeException] " +
                "and message is match \"null(.*?)parameter\"\n" +
                "     got: <block threw java.lang.NullPointerException: must not be null value in method parameter>");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("must not be null value in method parameter");
            }
        };

        assertThat(block, is(throwing(RuntimeException.class)
                            .andMessage(IsRegex.<CharSequence>match("null(.*?)parameter"))));
    }

    @Test
    public void matchExceptionWithMessageRegex() {
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("must not be null value in method parameter");
            }
        };
        assertThat(block, is(throwing(RuntimeException.class)
                            .andMessage(IsRegex.<CharSequence>match("(.*?)null(.*?)parameter"))));
    }

    @Test
    public void likeExceptionWithMessageRegex$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("Expected: is to throw exception(s) [java.lang.RuntimeException] " +
                "and message is like \"null(.*?aaa*)parameter\"\n" +
                "     got: <block threw java.lang.NullPointerException: must not be null value in method parameter>");

        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("must not be null value in method parameter");
            }
        };
        assertThat(block, is(throwing(RuntimeException.class).andMessage(like("null(.*?aaa*)parameter"))));
    }

    @Test
    public void likeExceptionWithMessageRegex() {
        @SuppressWarnings({"unchecked"})
        final AbstractCallable<Void> block = new Block() {
            @Override protected void run() {
                throw new NullPointerException("must not be null value in method parameter");
            }
        };
        assertThat(block, is(throwing(RuntimeException.class)
                            .andMessage(like("null(.*?)parameter"))));
    }
}