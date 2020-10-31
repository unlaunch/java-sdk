package io.unlaunch.event;

import io.unlaunch.UnlaunchRestWrapper;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * This interface provides event processing for Unlaunch events including metrics, impressions and tracking events.
 *
 * @author umermansoor
 */
public interface EventHandler extends Closeable {

    boolean handle(Event event);

    void flush();

    void close();

    static GenericEventHandler createGenericEventHandler(String name, UnlaunchRestWrapper unlaunchRestWrapper,
                                                         long eventFlushIntervalInSeconds) {
        return new GenericEventHandler(name, unlaunchRestWrapper, eventFlushIntervalInSeconds, 100);
    }

    static GenericEventHandler createGenericEventHandler(String name, UnlaunchRestWrapper unlaunchRestWrapper,
                                                         long eventFlushIntervalInSeconds, int maxBufferSize) {
        return new GenericEventHandler(name, unlaunchRestWrapper, eventFlushIntervalInSeconds,
                maxBufferSize);
    }

    static CountAggregatorEventHandler createCountAggregatorEventHandler(EventHandler eventHandler, long runFrequency, TimeUnit unit) {
        return new CountAggregatorEventHandler(eventHandler, unit.toMillis(runFrequency));
    }
}
