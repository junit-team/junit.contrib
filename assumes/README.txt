=====================================
Welcome to JUnit Contrib Assumes
=====================================

Consider the case where you are testing a List class...

we have

public class MyListTest {

 @Test
 public void newListIsEmpty() {
   assertThat(new MyList().isEmpty(), is(true);
 }

 @Test
 public void newListHasSizeZero() {
   assertThat(new MyList().size(), is(0));
 }

 @Test
 public void addPutsAnElementIntoAnEmptyList() {
   MyList l = new MyList();
   l.add(new Object());
   assertThat(l.isEmpty(), is(false));
 }

 @Test
 public void addIncreasesSizeOfPopulatedListByOne() {
   MyList l = new MyList();
   l.add(new Object());
   int s = l.size();
   l.add(new Object());
   assertThat(l.size(), is(s + 1));
 }

}

We now want to add some tests of the delete functionality... but the
reality is that until/unless some of the preceding tests are passing,
the tests for delete are meaningless. We could have a perfectly
functional MyList.delete() method but until such time as the above tests
are passing, there is no way to tell that the method does not work.

Now I could code my tests like such

 @Test
 public void deleteIsANoOpOnEmptyList() {
   MyList l = new MyList();
   assumeThat(l.isEmpty(), is(true));
   l.delete(new Object());
 }

But all that I am doing is repeating code from the preceding tests,
having changed all those tests' assertThat(...)s into assumeThat(...)s

That does not seem agile to me, copy & paste & search & replace... bad
code smell there

I would much rather be able to annotate the tests with an @Assumes
annotation that indicates that the test assumes that the specified
tests are passing, e.g.

 @Test
 @Assumes("newListIsEmpty")
 public void deleteIsANoOpOnEmptyList() {
   MyList l = new MyList();
   l.delete(new Object());
 }

 @Test
 @Assumes({"newListIsEmpty","addPutsAnElementIntoAnEmptyList")
 public void deleteRemovesAnElement() {
   MyList l = new MyList();
   Object o = new Object();
   l.add(o);
   l.delete(o);
   assertThat(l.isEmpty(), is(true));
 }

In fact in my initial example of tests, there are some additional
assumptions that I didn't make explicit

 @Test
 @Assumes("newListIsEmpty")
 public void addPutsAnElementIntoAnEmptyList() {
   ...
 }

and

 @Test
 @Assumes({"newListIsEmpty","addPutsAnElementIntoAnEmptyList")
 public void addIncreasesSizeOfPopulatedListByOne() {
   ...
 }

This module allows this functionality by providing the Corollaries runner
which is aware of Assumes declared by tests and will ensure that the tests
of known invalid assumptions are skipped.

