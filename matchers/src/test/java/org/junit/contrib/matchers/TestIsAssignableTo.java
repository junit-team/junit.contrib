package org.junit.contrib.matchers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.NotSerializableException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.contrib.matchers.IsAssignableTo.*;

/**
 * The purpose of this test is to test a functionality of
 * {@link IsAssignableTo} matcher and to explain the use.
 * <p/>
 * @author Tibor17
 * @version 0.1
 * @see IsAssignableTo
 * @since 0.1, Dec 11, 2011, 7:13:24 PM
 */
public final class TestIsAssignableTo {
    @Rule
    public final ExpectedException expectedExceptionRule = ExpectedException.none();

    final class Sub extends SuperClass implements SuperInterface {}
    class SuperClass extends SuperBaseClass {}
    interface SuperInterface extends SuperBaseInterface {}
    class SuperBaseClass {}
    interface SuperBaseInterface {}
    final class OtherClass {}
    interface OtherInterface {}

    @Test
    public void isAssignableToSame() {
        assertThat(Sub.class, is(assignableTo(Sub.class)));
    }

    @Test @SuppressWarnings("unchecked")
    public void isAssignableToExpectedSuperInterface$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is assignable to org.junit.contrib.matchers.TestIsAssignableTo$Sub");

        Class actual = SuperInterface.class;
        Class expected = Sub.class;
        assertThat(actual, is(assignableTo(expected)));
    }

    @Test @SuppressWarnings("unchecked")
    public void isAssignableToExpectedSuperInterface() {
        Class actual = Sub.class;
        Class expected = SuperInterface.class;
        assertThat(actual, is(assignableTo(expected)));
    }

    @Test @SuppressWarnings("unchecked")
    public void isAssignableToExpectedSuperBaseInterface$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is assignable to org.junit.contrib.matchers.TestIsAssignableTo$Sub");

        Class actual = SuperBaseInterface.class;
        Class expected = Sub.class;
        assertThat(actual, is(assignableTo(expected)));
    }

    @Test @SuppressWarnings("unchecked")
    public void isAssignableToExpectedSuperBaseInterface() {
        Class actual = Sub.class;
        Class expected = SuperBaseInterface.class;
        assertThat(actual, is(assignableTo(expected)));
    }
    //

    @Test
    public void isAssignableToAnyOtherInterface$NegativeTest$1() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is assignable to org.junit.contrib.matchers.TestIsAssignableTo$OtherInterface");

        assertThat(OtherClass.class, is(assignableToAny(OtherInterface.class)));
    }

    @Test
    public void isAssignableToAnyOtherClass$NegativeTest$2() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is assignable to org.junit.contrib.matchers.TestIsAssignableTo$OtherClass");

        assertThat(OtherInterface.class, is(assignableToAny(OtherClass.class)));
    }

    @Test
    public void isAssignableToAnyOtherClass$NegativeTest$3() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is assignable to org.junit.contrib.matchers.TestIsAssignableTo$OtherClass");

        assertThat(Sub.class, is(assignableToAny(OtherClass.class)));
    }

    @Test
    public void isAssignableToAnyOtherInterface$NegativeTest$4() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is assignable to org.junit.contrib.matchers.TestIsAssignableTo$OtherInterface");

        assertThat(Sub.class, is(assignableToAny(OtherInterface.class)));
    }

    @Test
    public void isAssignableToAnyExpectedSuperClass() {
        assertThat(Sub.class, is(assignableToAny(SuperClass.class)));
    }

    @Test
    public void isAssignableToAnyExpectedSuperInterface() {
        assertThat(Sub.class, is(assignableToAny(SuperInterface.class)));
    }

    @Test
    public void isAssignableToAnyExpectedSuperBaseClass() {
        assertThat(Sub.class, is(assignableToAny(SuperBaseClass.class)));
    }

    @Test
    public void isAssignableToAnyExpectedSuperBaseInterface() {
        assertThat(Sub.class, is(assignableToAny(SuperBaseInterface.class)));
    }
    //

    @Test
    public void isAssignableToThrowable$NegativeTest() {
        expectedExceptionRule.expect(AssertionError.class);
        expectedExceptionRule.expectMessage("is assignable to java.io.NotSerializableException");

        assertThat(IOException.class, is(assignableToThrowable(NotSerializableException.class)));
    }

    @Test
    public void isAssignableToThrowable$1() {
        assertThat(NotSerializableException.class, is(assignableToThrowable(IOException.class)));
    }

    @Test
    public void isAssignableToThrowable$2() {
        assertThat(IOException.class, is(assignableToThrowable(IOException.class)));
    }
}
