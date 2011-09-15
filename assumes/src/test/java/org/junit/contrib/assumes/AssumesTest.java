package org.junit.contrib.assumes;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.Comparator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class AssumesTest {
    private static Comparator<Description> forward() {
        return new Comparator<Description>() {
            public int compare(Description o1, Description o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        };
    }

    private static Comparator<Description> backward() {
        return new Comparator<Description>() {
            public int compare(Description o1, Description o2) {
                return o2.getDisplayName().compareTo(o1.getDisplayName());
            }
        };
    }

    public static class NoAssumesImpliesNoAffectsOnOrder {
        private static String log;

        @Before
        public void setUp() {
            log = "";
        }

        @Test
        public void runsInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("abcd"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(0));
        }

        @Test
        public void runsInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("dcba"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(0));
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
                log += "a";
            }
            @Test
            public void b() {
                log += "b";
            }
            @Test
            public void c() {
                log += "c";
                assertThat(true, is(false));
            }
            @Test
            public void d() {
                log += "d";
            }
        }
    }

    public static class AssumesDoesNotAffectStandardRunner {
        private static String log;

        @Before
        public void setUp() {
            log = "";
        }

        @Test
        public void runsInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("abcd"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(0));
        }

        @Test
        public void runsInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("dcba"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(0));
        }

        public static class Artifact {
            @Test
            public void a() {
                log += "a";
            }
            @Test
            public void b() {
                log += "b";
            }
            @Test
            public void c() {
                log += "c";
                assertThat(true, is(false));
            }
            @Test
            public void d() {
                log += "d";
            }
        }
    }

    public static class AssumesRestrictsOrdering {
        private static String log;

        @Before
        public void setUp() {
            log = "";
        }

        @Test
        public void runsInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("acbd"));
            assertThat(result.getFailureCount(), is(0));
            assertThat(result.getIgnoreCount(), is(0));
        }

        @Test
        public void runsInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("dcba"));
            assertThat(result.getFailureCount(), is(0));
            assertThat(result.getIgnoreCount(), is(0));
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
                log += "a";
            }
            @Test
            @Assumes("c")
            public void b() {
                log += "b";
            }
            @Test
            public void c() {
                log += "c";
            }
            @Test
            public void d() {
                log += "d";
            }
        }
    }

    public static class LongCircularAssumptions {
        @Test
        public void doesNotInfiniteLoopInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            new JUnitCore().run(request);
        }

        @Test
        public void doesNotInfiniteLoopInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            new JUnitCore().run(request);
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
            }
            @Test
            @Assumes("c")
            public void b() {
            }
            @Test
            @Assumes("d")
            public void c() {
            }
            @Test
            @Assumes("b")
            public void d() {
            }
        }
    }

    public static class ShortCircularAssumptions {
        @Test
        public void doesNotInfiniteLoopInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            new JUnitCore().run(request);
        }

        @Test
        public void doesNotInfiniteLoopInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            new JUnitCore().run(request);
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
            }
            @Test
            @Assumes("c")
            public void b() {
            }
            @Test
            @Assumes("b")
            public void c() {
            }
            @Test
            public void d() {
            }
        }
    }

    public static class FailedAssumptionsAreSkipped {
        private static String log;

        @Before
        public void setUp() {
            log = "";
        }

        @Test
        public void runsInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("acd"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(1));
        }

        @Test
        public void runsInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("dca"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(1));
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
                log += "a";
            }
            @Test
            @Assumes("c")
            public void b() {
                log += "b";
            }
            @Test
            public void c() {
                log += "c";
                assertThat(true, is(false));
            }
            @Test
            public void d() {
                log += "d";
            }
        }
    }

    public static class ComplexSequenceSmokeTest {
        private static String log;

        @Before
        public void setUp() {
            log = "";
        }

        @Test
        public void runsInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("acbefghijkd"));
            assertThat(result.getFailureCount(), is(0));
            assertThat(result.getIgnoreCount(), is(0));
        }

        @Test
        public void runsInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("kjigecfbhda"));
            assertThat(result.getFailureCount(), is(0));
            assertThat(result.getIgnoreCount(), is(0));
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
                log += "a";
            }
            @Test
            @Assumes("c")
            public void b() {
                log += "b";
            }
            @Test
            public void c() {
                log += "c";
            }
            @Test
            @Assumes({"k","h"})
            public void d() {
                log += "d";
            }
            @Test
            public void e() {
                log += "e";
            }
            @Test
            @Assumes("c")
            public void f() {
                log += "f";
            }
            @Test
            public void g() {
                log += "g";
            }
            @Test
            @Assumes({"c","b"})
            public void h() {
                log += "h";
            }
            @Test
            public void i() {
                log += "i";
            }
            @Test
            public void j() {
                log += "j";
            }
            @Test
            public void k() {
                log += "k";
            }
        }
    }

    public static class ComplexLargeSmokeTest {
        private static String log;

        @Before
        public void setUp() {
            log = "";
        }

        @Test
        public void runsInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("acegijk"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(4));
        }

        @Test
        public void runsInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("kjigeca"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(4));
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
                log += "a";
            }
            @Test
            @Assumes("c")
            public void b() {
                log += "b";
            }
            @Test
            public void c() {
                log += "c";
                assertThat(true, is(false));
            }
            @Test
            @Assumes({"k","h"})
            public void d() {
                log += "d";
            }
            @Test
            public void e() {
                log += "e";
            }
            @Test
            @Assumes("c")
            public void f() {
                log += "f";
            }
            @Test
            public void g() {
                log += "g";
            }
            @Test
            @Assumes({"c","b"})
            public void h() {
                log += "h";
            }
            @Test
            public void i() {
                log += "i";
            }
            @Test
            public void j() {
                log += "j";
            }
            @Test
            public void k() {
                log += "k";
            }
        }
    }

    public static class ComplexSmallEffectSmokeTest {
        private static String log;

        @Before
        public void setUp() {
            log = "";
        }

        @Test
        public void runsInOrder() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(forward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("acbefgijk"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(2));
        }

        @Test
        public void runsInReverse() throws Exception {
            Request request = Request.classes(Artifact.class).sortWith(backward());
            Result result = new JUnitCore().run(request);
            assertThat(log, is("kjigecfba"));
            assertThat(result.getFailureCount(), is(1));
            assertThat(result.getIgnoreCount(), is(2));
        }

        @RunWith(Corollaries.class)
        public static class Artifact {
            @Test
            public void a() {
                log += "a";
            }
            @Test
            @Assumes("c")
            public void b() {
                log += "b";
                assertThat(true, is(false));
            }
            @Test
            public void c() {
                log += "c";
            }
            @Test
            @Assumes({"k","h"})
            public void d() {
                log += "d";
            }
            @Test
            public void e() {
                log += "e";
            }
            @Test
            @Assumes("c")
            public void f() {
                log += "f";
            }
            @Test
            public void g() {
                log += "g";
            }
            @Test
            @Assumes({"c","b"})
            public void h() {
                log += "h";
            }
            @Test
            public void i() {
                log += "i";
            }
            @Test
            public void j() {
                log += "j";
            }
            @Test
            public void k() {
                log += "k";
            }
        }
    }
}
