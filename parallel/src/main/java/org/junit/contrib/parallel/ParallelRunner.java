package org.junit.contrib.parallel;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * A JUnit 4 runner which executes the test methods of a test class concurrently.
 */
public class ParallelRunner extends BlockJUnit4ClassRunner {

    public ParallelRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        setScheduler(new ParallelScheduler());
    }
}
