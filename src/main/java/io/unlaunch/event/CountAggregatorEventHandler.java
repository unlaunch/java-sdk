package io.unlaunch.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.unlaunch.utils.UnlaunchConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Specialized event handler to aggregate variation count stats and send one aggregated event periodically instead of
 * blasting the backend with too many events.
 *
 * @author umer
 */

final class CountAggregatorEventHandler implements EventHandler, Closeable {

    // All operations which modify the map must be synchronized.
    private final Map<String, AtomicInteger> variationsCountMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final EventHandler eventHandler;
    private static final Logger logger = LoggerFactory.getLogger(CountAggregatorEventHandler.class);

    CountAggregatorEventHandler(EventHandler eventHandler, long runFrequencyInMillis) {
        Preconditions.checkNotNull(eventHandler);
        Preconditions.checkArgument(runFrequencyInMillis > 0);
        executorService.scheduleAtFixedRate(this::run, runFrequencyInMillis, runFrequencyInMillis, TimeUnit.MILLISECONDS);
        this.eventHandler = eventHandler;
        logger.info("Variation count metrics will be aggregated every {} millseconds", runFrequencyInMillis);
    }


    private void incrementFlagVariation(String flagId, String variationId) {
        Strings.isNullOrEmpty(flagId);
        Strings.isNullOrEmpty(variationId);
        logger.debug("Incrementing variation {} for flag {}", variationId, flagId);

        String key = flagId + ":" + variationId;
        synchronized (variationsCountMap) {
            if (!variationsCountMap.containsKey(key)) {
                variationsCountMap.put(key, new AtomicInteger(0));
            }
        }
        variationsCountMap.get(key).incrementAndGet();
    }

    /**
     * Command which the Executor runs as a method reference. Upon close(), it is run in the context of the current
     * thread.
     */
    private void run() {
        if (variationsCountMap.size() != 0) {
            Map<String, AtomicInteger> copyOfVariationsCountMap = new HashMap<>();

            synchronized(variationsCountMap) {
                copyOfVariationsCountMap.putAll(variationsCountMap);
                variationsCountMap.clear();
            }

            logger.debug("{} flag counts will be sent to the server.", copyOfVariationsCountMap.size());

            copyOfVariationsCountMap.forEach( (key, value) -> {
                String [] temp = key.split(":");
                String flagKey = temp[0];
                String mapKey = temp[1];

                Event e = new Event(UnlaunchConstants.FLAG_INVOCATIONS_COUNT_EVENT_TYPE, flagKey);
                e.addProperty(mapKey, value.intValue());

                try {
                    eventHandler.handle(e);
                    eventHandler.flush(); // Flush because we already waited to aggregate events
                } catch (RuntimeException ex) {
                    logger.error("An error occured sending event counts to the service {}", ex.getMessage());
                }
            });
        } else {
            logger.debug("nothing to update");
        }
    }

    /**
     * This method aggregates {@link Event#getSecondaryKey()} using the {@link Event#getKey()} as the key.
     *
     * @param event
     * @return
     */
    @Override
    public boolean handle(Event event) {
        incrementFlagVariation(event.getKey(), event.getSecondaryKey());
        return true;
    }

    @Override
    public void flush() {
        // do nothing
    }

    @Override
    public void close() {
        try {
            executorService.shutdownNow();
            executorService.awaitTermination(1, TimeUnit.SECONDS);

            // pushing all remaining events to server one last time.
            run();

            if (executorService.isShutdown()) {
                logger.info("Executor service closed successfully.");
            } else {
                logger.info("Executor service was not closed successfully.");
            }
        } catch (Exception e) {
            logger.error("Error in shutting down executor in CountAggregatorEventHandler");
        }
    }
}
