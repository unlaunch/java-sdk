package io.unlaunch.store;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.unlaunch.UnlaunchRestWrapper;
import io.unlaunch.exceptions.UnlaunchRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides a singleton instance of the {@link UnlaunchHttpDataStore} such that it updates itself periodically.
 *
 * @author umermansoor
 */
public final class RefreshableDataStoreProvider implements Closeable {
    private final UnlaunchRestWrapper restWrapper;
    private final long delay;

    private final AtomicReference<UnlaunchHttpDataStore> refreshableUnlaunchFetcherRef = new AtomicReference<>();
    private final CountDownLatch initialDownloadDoneLatch;
    private final AtomicBoolean downloadSuccessful;
    private final ScheduledExecutorService scheduledExecutorService;

    private static final Logger logger = LoggerFactory.getLogger(RefreshableDataStoreProvider.class);

    public RefreshableDataStoreProvider(
            UnlaunchRestWrapper restWrapper,
            CountDownLatch initialDownloadDoneLatch,
            AtomicBoolean downloadSuccessful,
            long dataStoreRefreshDelayInSeconds) {
        this.restWrapper = restWrapper;
        this.delay = dataStoreRefreshDelayInSeconds;
        this.initialDownloadDoneLatch = initialDownloadDoneLatch;
        this.downloadSuccessful = downloadSuccessful;

        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        threadFactoryBuilder.setNameFormat("fetcher-%d");
        this.scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor(threadFactoryBuilder.build());
    }

    public UnlaunchDataStore getNoOpDataStore() {
        return new UnlaunchNoOpDataStore();
    }

    public synchronized UnlaunchDataStore getDataStore() {
        if (refreshableUnlaunchFetcherRef.get() != null) {
            return refreshableUnlaunchFetcherRef.get();
        }

        UnlaunchHttpDataStore dataStore = new UnlaunchHttpDataStore(restWrapper, initialDownloadDoneLatch, downloadSuccessful);

        try {
            scheduledExecutorService.scheduleWithFixedDelay(dataStore, 0L, delay, TimeUnit.SECONDS);

            this.refreshableUnlaunchFetcherRef.set(dataStore);
            return dataStore;

        } catch (RejectedExecutionException e) {
            logger.error("Unable to initialize  in-memory cache (Datastore). This should not have happened. Executor " +
                            "shutdown: {}?", scheduledExecutorService.isShutdown());
            throw new UnlaunchRuntimeException("couldn't start executor", e);
        }
    }

    @Override
    public void close() {
        if (!scheduledExecutorService.isShutdown()) {
            try {
                scheduledExecutorService.shutdown();
                if (!scheduledExecutorService.awaitTermination(2L, TimeUnit.SECONDS)) {
                    logger.warn("Fetcher executor didn't shutdown in expected time.");
                    List droppedTasks = scheduledExecutorService.shutdownNow();
                    logger.warn("Fetcher was forcefully shut down. {} tasks were not executed: ", droppedTasks.size());
                } else {
                    logger.debug("Fetcher executor shutdown complete.");
                }

                if (refreshableUnlaunchFetcherRef.get() != null) {
                    refreshableUnlaunchFetcherRef.get().close(); // simple cleanup
                    refreshableUnlaunchFetcherRef.set(null);
                }
            } catch (InterruptedException e) {
                logger.warn("Fetcher close method was interrupted");
                Thread.currentThread().interrupt(); // reset the interrupt.
            }
        }
    }
}
