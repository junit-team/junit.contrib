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

import org.junit.Test;
import org.junit.contrib.matchers.IsThrowing.IBlock;
import static org.junit.contrib.matchers.IsThrowing.*;
import static org.junit.contrib.matchers.IsThrowable.type;

// junit asserts
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

// junit & hamcrest matchers
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.DescribedAs.describedAs;

import java.io.IOException;
import java.util.*;

/**
 * The purpose of this test is to test a functionality of
 * {@link IsThrowing} matcher
 * and to explain an use.
 * <p/>
 * @author Test17
 * @version 0.1
 * @see IsThrowing
 * @since 0.1, Oct 3, 2011, 7:07:57 PM
 */
public final class TestIsThrowing {

    @Test
    public void throwing$1() {
        final Integer[] array = new Integer[2];
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
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
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(throwing(IndexOutOfBoundsException.class)));
        array[1] = array[0];
    }

    @Test(expected = java.lang.AssertionError.class)
    public void throwing$3() {
        final Integer[] array = new Integer[2];
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                int i = 0;
                array[i++] = i;
                array[i++] = i;
                array[i++] = i;
            }
        };
        assertThat(block, is(not(throwing())));
        array[1] = array[0];
    }

    @Test(expected = java.lang.AssertionError.class)
    public void throwing$4() {
        final Integer[] array = new Integer[2];
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
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
    public void throwing$5() {
        final Integer[] array = new Integer[2];
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
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
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };
        assertThat(block, is(notThrowingDeeply(IndexOutOfBoundsException.class)));
        array[0] = array[1];
    }

    @Test(expected = java.lang.AssertionError.class)
    public void throwing$7() {
        final Integer[] array = new Integer[2];
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                Object[] copy = array;
                copy[0] = "wrong type to assign";
            }
        };
        assertThat(block, is(not(throwing(IndexOutOfBoundsException.class, ArrayStoreException.class))));
        array[0] = array[1];
    }

    @Test(expected = java.lang.AssertionError.class)
    public void throwing$8() {
        final Integer[] array = new Integer[2];
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
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
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
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
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
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
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
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

    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void throwingMutualBlock$0() {
        List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
        final ListIterator it = list.listIterator();
        IBlock block = new IBlock() {
            @Override public void run() {
                it.next();
                it.next();
                it.set("a");
            }
        };
        assertThat(block, both(is(throwingDeeply(NoSuchElementException.class))).and(is(notThrowing())));
        list.set(0, new Object());
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void throwingMutualBlock$1() {
        List list = Collections.checkedList(Arrays.asList("b", "c"), String.class);
        final ListIterator it = list.listIterator();
        IBlock block = new IBlock() {
            @Override public void run() {
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
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new Exception("     a ny thin g     ");
            }
        };
        assertThat(block, is(throwingDeeply().andHasMessage("a ny|thing")));
    }

    @Test
    public void thrownOkMessageWrong$0() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new Exception("     an y thin g     ");
            }
        };
        assertThat(block, is(not(throwingDeeply().andHasMessage("any|thing"))));
    }

    @Test
    public void thrownOkNullMessageWrong$0() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new Exception((String) null);
            }
        };
        assertThat(block, is(not(throwingDeeply().andHasMessage("any|thing"))));
    }

    @Test
    public void thrownOkMessageOk$1() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new Exception("     a ny thin g     ");
            }
        };
        assertThat(block, is(throwingDeeply().andHasMessage(anyOf(containsString("any"), containsString("thin"), containsString("thing")))));
    }

    @Test
    public void thrownOkMessageWrong$1() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new Exception("     an y thin g     ");
            }
        };
        assertThat(block, is(not(throwingDeeply().andHasMessage(anyOf(containsString("any"), containsString("thing"))))));
    }

    @Test
    public void thrownOkNullMessageWrong$1() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new Exception((String) null);
            }
        };
        assertThat(block, is(not(throwingDeeply().andHasMessage(anyOf(containsString("any"), containsString("thing"))))));
    }

    @Test
    public void matchException$0() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new RuntimeException();
            }
        };
        assertThat(block, is(not(throwing(type(IsAssignableTo.<Throwable>assignableTo(IOException.class))))));
    }

    @Test
    public void matchException$1() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new NullPointerException();
            }
        };
        assertThat(block, is(throwing(type(IsAssignableTo.<Throwable>assignableTo(RuntimeException.class)))));
    }

    @Test
    public void matchException$2() {
        IBlock block = new IBlock() {
            @Override public void run() throws Throwable {
                throw new NullPointerException("null value");
            }
        };
        assertThat(block, is(throwing(
                both(IsThrowable.<Throwable>localizedMessage(equalTo("null value")))
                        .and(anyOf(type(IsAssignableTo.<Throwable>assignableTo(IllegalArgumentException.class)),
                                type(IsAssignableTo.<Throwable>assignableTo(NullPointerException.class)))))));
    }
}