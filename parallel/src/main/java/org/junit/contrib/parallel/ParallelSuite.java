package org.junit.contrib.parallel;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;

/**
 * An extension of the Junit 4 {@link Suite} runner
 * which executes test classes concurrently.Example
 */
public class ParallelSuite extends Suite {

    public ParallelSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        setScheduler(new ParallelScheduler());
    }
}
