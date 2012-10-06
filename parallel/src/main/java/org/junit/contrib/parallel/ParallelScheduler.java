package org.junit.contrib.parallel;

import org.junit.runners.model.RunnerScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;

import static java.util.concurrent.ForkJoinTask.inForkJoinPool;

public class ParallelScheduler implements RunnerScheduler {

    private static final ForkJoinPool FORK_JOIN_POOL = setUpForkJoinPool();

    private static ForkJoinPool setUpForkJoinPool() {
        int numThreads;
        try {
            Properties systemProperties = System.getProperties();
            String configuredNumThreads = systemProperties.getProperty("maxParallelTestThreads");
            numThreads = Math.max(2, Integer.parseInt(configuredNumThreads));
        } catch (Exception ignored) {
            Runtime runtime = Runtime.getRuntime();
            numThreads = Math.max(2, runtime.availableProcessors());
        }
        ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("JUnit-" + thread.getName());
                return thread;
            }
        };
        return new ForkJoinPool(numThreads, threadFactory, null, false);
    }

    private final List<ForkJoinTask<?>> asyncTasks = new ArrayList<>();
    private Runnable lastScheduledChild;

    public void schedule(Runnable childStatement) {
        if (lastScheduledChild != null) {
            // Execute previously scheduled child asynchronously ...
            if (inForkJoinPool()) {
                asyncTasks.add(ForkJoinTask.adapt(lastScheduledChild).fork());
            } else {
                asyncTasks.add(FORK_JOIN_POOL.submit(lastScheduledChild));
            }
        }
        // Remember scheduled child ...
        lastScheduledChild = childStatement;
    }

    public void finished() {
        MultiException me = new MultiException();
        if (lastScheduledChild != null) {
            // Execute the last scheduled child in the current thread ...
            try { lastScheduledChild.run(); } catch (Throwable t) { me.add(t); }
            // Make sure all asynchronously executed children are done ...
            for (ForkJoinTask<?> task : asyncTasks) {
                try { task.join(); } catch (Throwable t) { me.add(t); }
            }
            me.throwRuntimeExceptionIfNotEmpty();
        }
    }
}
