JUnit Theories Runner
----

This is a port of the JUnit theories runner into junit.contrib.

In addition to being current with the theories implementation in JUnit 4.11 and
depending on its core, this implementation contains a resolution for
[JUnit GitHub issue 64](http://github.com/junit-team/junit/issues/64), making it possible for
[junit-quickcheck](http://github.com/pholser/junit-quickcheck) to generate values for theory
parameters involving generics in a safe manner.

Ultimately it is hoped that this rendition becomes the sanctioned theories runner for JUnit,
so that the one in the core can be removed, meaning that this runner can evolve without new
releases of JUnit.

**PLEASE NOTE**: The classes that comprise this rendition of the JUnit theories runner are packaged
as `org.junit.contrib.theories.*`, rather than `org.junit.experimental.theories.*`. Be mindful of
which one you're using.

## Downloading

Releases are synced to the central Maven repository. Declare a dependency element in your POM like so:

    ...
    <dependencies>
      ...
      <dependency>
        <groupId>org.junit.contrib</groupId>
        <artifactId>junit-theories</artifactId>
        <version>4.11</version>
      </dependency>
      ...
    </dependencies>
    ...

### What is a theory?

Most JUnit tests are example-based: given a specific set of inputs, the test subject behaves in a
particular way or responds with a specific answer. For example, here are some tests we might write
when test-driving a prime factors kata:

    public class PrimeFactors {
        public static List<Integer> of(int n) {
            // ...
        }
    }

    public class UnparameterizedPrimeFactorsTest {
        @Test public void one() {
            assertEquals(Collections.emptyList(), PrimeFactors.of(1));
        }

        @Test public void two() {
            assertEquals(Arrays.asList(2), PrimeFactors.of(2));
        }

        @Test public void three() {
            assertEquals(Arrays.asList(3), PrimeFactors.of(3));
        }

        @Test public void four() {
            assertEquals(Arrays.asList(2, 2), PrimeFactors.of(4));
        }

        @Test public void five() {
            assertEquals(Arrays.asList(5), PrimeFactors.of(5));
        }

        // etc...
    }

We can eliminate duplicated test logic sometimes by using parameterized tests:

    @RunWith(Parameterized.class)
    public class PrimeFactorsTest {
        private final int target;
        private final List<Integer> expectedFactors;

        public PrimeFactorsTest(int target, List<Integer> expectedFactors) {
            this.target = target;
            this.expectedFactors = expectedFactors;
        }

        @Parameters public static Collection<?> data() {
            return asList(new Object[][] {
                { 1, Collections.emptyList() },
                { 2, Arrays.asList(2) },
                { 3, Arrays.asList(3) },
                { 4, Arrays.asList(2, 2) },
                { 5, Arrays.asList(5) },
                // etc...
            });
        }

        @Test public void comparison() {
            assertEquals(Integer.toString(target), expectedFactors, PrimeFactors.of(target));
        }
    }

Neither of these tests expresses important characteristics we want the the answers given by
`PrimeFactors.of()` to exhibit: No matter what positive integer you give the method...

* The factors must be prime
* The factors must multiply together to give the original integer
* Factorizations of two distinct integers must themselves be distinct (the Fundamental Theorem of Arithmetic)

Whenever we want to express characteristics of a test subject that hold for entire classes of
inputs, and we can express the characteristics in terms of inputs and outputs, we can codify these
desired characteristics in _theories_. Here are the characteristics of `PrimeFactors.of()`
expressed as theories:

    @RunWith(Theories.class)
    public class PrimeFactorsTheories {
        @DataPoint public static final int ONE = 1;
        @DataPoint public static final int TWO = 2;
        @DataPoint public static final int THREE = 3;
        @DataPoints public static int[] moreExamples = { 4, 5, 6 };

        @DataPoint public static int anotherExample() {
            return 5;
        }

        @DataPoints public static int[] stillMoreExamples() {
            return new int[] { 6, 7, 8, 9 };
        }

        @Theory public void factorsPassPrimalityTest(int n) {
            assumeThat(n, greaterThan(0));

            for (int each : PrimeFactors.of(n))
                assertTrue(BigInteger.valueOf(each).isProbablePrime(1000));
        }

        @Theory public void factorsMultiplyToOriginal(int n) {
            assumeThat(n, greaterThan(0));

            int product = 1;
            for (int each : PrimeFactors.of(n))
                product *= each;

            assertEquals(n, product);
        }

        @Theory public void factorizationsAreUnique(int m, int n) {
            assumeThat(m, greaterThan(0));
            assumeThat(n, greaterThan(0));
            assumeThat(m, not(equalTo(n)));

            assertThat(PrimeFactors.of(m), not(equalTo(PrimeFactors.of(n))));
        }
    }

### How to formulate theories

* A test class containing theories is run with the `Theories` runner.

* A method that represents a theory is annotated with `@Theory` instead of the usual `@Test`.

* A theory method accepts parameters, which represent arbitrary inputs to the theory.

* Theory methods can state _assumptions_ about their inputs using the methods of `Assume`.
For example, the theories above assume that we're dealing with positive integers. The remainder
of a theory is not run if one of its assumptions is violated.

* Theory methods state success criteria in the form of _assertions_, just like regular JUnit tests
do.

* By default, inputs are supplied to theories from fields and methods on the class annotated as
either `@DataPoint` or `@DataPoints`. A theory method is invoked once for every combination of data
points that match on type.

#### Alternate means of providing theory data

The data points method is somewhat flawed, because we are still baking concrete example data into
the theory class. It would be nice to be able to decouple the theory from data that is used to
verify the theory -- after all, a theory should hold for potentially infinite classes of data.

Thankfully, we can use `ParameterSupplier`s for just this purpose:

    public class PositiveIntegerParameterSupplier extends ParameterSupplier {
        private final Random random = new SecureRandom();

        @Override
        public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
            List<PotentialAssignment> values = new ArrayList<PotentialAssignment>();

            for (int i = 0; i < 100; ++i) {
                int next = random.nextInt(Integer.MAX_VALUE);
                if (next == 0)
                    next = Integer.MAX_VALUE;
                values.add(PotentialAssignment.forValue(Integer.toString(next), next));
            }

            return values;
        }
    }

    @Target(PARAMETER)
    @Retention(RUNTIME)
    @ParametersSuppliedBy(PositiveIntegerParameterSupplier.class)
    public @interface AnyPositive {
    }

    @RunWith(Theories.class)
    public class PrimeFactorsTheories {
        @Theory public void factorsPassPrimalityTest(@AnyPositive int n) {
            for (int each : PrimeFactors.of(n))
                assertTrue(BigInteger.valueOf(each).isProbablePrime(1000));
        }

        @Theory public void factorsMultiplyToOriginal(@AnyPositive int n) {
            int product = 1;
            for (int each : PrimeFactors.of(n))
                product *= each;

            assertEquals(n, product);
        }

        @Theory public void factorizationsAreUnique(@AnyPositive int m, @AnyPositive int n) {
            assumeThat(m, not(equalTo(n)));

            assertThat(PrimeFactors.of(m), not(equalTo(PrimeFactors.of(n))));
        }
    }

To customize how data are fed to a given theory parameter:

* Create a class that extends `ParameterSupplier`

* Create an annotation that is itself annotated with
`@ParametersSuppliedBy(YourParameterSupplier.class)`

* Mark the desired theory parameter with your annotation

In the example above, we create a `PositiveIntegerParameterSupplier` that gives 100 positive
integers at random when invoked. Then, we create an annotation `@AnyPositive` and apply it to the
theory parameters. This allows us to get rid of our baked-in data points and test the theory
against lots of random values. Also, because our parameter supplier is coded to supply only
positive integers, we can remove the positivity assumptions from the theories.
