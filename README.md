A port of the JUnit theories runner into junit.contrib.

In addition to being current with the theories implementation in JUnit 4.11
and depending on JUnit 4.11's core, this implementation contains a resolution
for [JUnit GitHub issue 64](http://github.com/KentBeck/junit/issues/64),
making it possible for
[junit-quickcheck](http://github.com/pholser/junit-quickcheck) to generate
values for theory parameters involving generics in a safe manner.

