package io.unlaunch.event;

import io.unlaunch.UnlaunchRestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Batched implementation of the {@link EventHandler}.
 *
 * Incoming events are added to a blocking queue instead of being sent to the server immediately. There is a single
 * consumer thread that pulls events from the queue when the maximum batch size is reached, or a special 'flush'
 * event is encountered and sends them to the server.
 *
 * @author umermansoor
 */
final class GenericEventHandler extends AbstractEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(GenericEventHandler.class);

    GenericEventHandler(String name, UnlaunchRestWrapper restClientForEventsApi, long eventFlushIntervalInSeconds,
                        int maxBufferSize) {
        super(name, restClientForEventsApi, eventFlushIntervalInSeconds,
                maxBufferSize);
    }

}
