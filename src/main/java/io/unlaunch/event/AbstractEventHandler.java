package io.unlaunch.event;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.unlaunch.UnlaunchRestWrapper;
import io.unlaunch.exceptions.UnlaunchHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Batched implementation of the {@link EventHandler}.
 *
 *<p>Incoming events are added to a blocking queue instead of being sent to the server immediately. There is a single
 * consumer thread that pulls events from the queue when the maximum batch size is reached, or a special 'flush'
 * event is encountered and sends them to the server.</p>
 *
 * @author umer mansoor
 */
abstract class AbstractEventHandler implements EventHandler {
    private final BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    private final ExecutorService consumerExecutor;
    private final UnlaunchRestWrapper restClient;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ScheduledExecutorService flushExecutor;
    private final String name;
    private static final Event FLUSH_PILL_EVENT = new Event("FLUSH_PILL", "FLUSH_PILL");
    private static final Logger logger = LoggerFactory.getLogger(AbstractEventHandler.class);

    AbstractEventHandler(String name, UnlaunchRestWrapper restClient, long flushIntervalInSeconds, int maxBufferSize) {
        this.restClient = restClient;
        this.name = name;

        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setNameFormat("ul-evthndlr-" + name + "-%d");

        consumerExecutor = Executors.newSingleThreadExecutor(threadFactoryBuilder.build());
        consumerExecutor.submit(new QueueConsumer(maxBufferSize));

        // Inject a flush event periodically (eventFlushIntervalInSeconds) to make the consumer send events immediately
        // This is done to avoid polling in the consumer thread and let it be blocked on BlockingQueue.take()
        // indefinitely.
        flushExecutor = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder().setNameFormat("ul-evthndlr-" + name + "-flush").build());
        flushExecutor.scheduleWithFixedDelay(() -> {
            queue.offer(FLUSH_PILL_EVENT);
        }, flushIntervalInSeconds, flushIntervalInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean handle(Event event) {
        if (event == null || closed.get()) {
            return false;
        }

        try {
            queue.put(event);
            return true;
        } catch (InterruptedException e) {
            logger.warn("Interrupted while adding event to the queue {}. ({})", event, name);
            return false;
        }

    }


    @Override
    public void flush() {
        try {
            queue.put(FLUSH_PILL_EVENT);
        } catch (InterruptedException ie) {
            logger.warn("event handler flush was interrupted ({})", name);
        }
    }

    /**
     * Release all resources
     */
    @Override
    public void close() {
        try {
            closed.set(true);
            flush();

            flushExecutor.shutdownNow();
            consumerExecutor.shutdownNow();
            consumerExecutor.awaitTermination(2, TimeUnit.SECONDS);
            flushExecutor.awaitTermination(1, TimeUnit.SECONDS);

            if (consumerExecutor.isShutdown() && flushExecutor.isShutdown()) {
                logger.info("EventHandler ({}) shutdown successfully.", name);
            } else {
                logger.info("EventHandler ({}) was not shutdown successfully: consumer executor shutdown: {}, flush " +
                        "executor shutdown: {}", name, consumerExecutor.isShutdown(), flushExecutor.isShutdown());
            }
        } catch (InterruptedException e) {
            flushExecutor.shutdownNow();
            consumerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("Error in shutting down executor in EventHandler ({})", name);
        }
    }


    /**
     * An infinite loop consumer of the queue.
     *
     * @author umermansoor
     */
    class QueueConsumer implements Runnable {
        private final int maxBufferSize;
        private volatile long lastSentTimeInMillis = System.currentTimeMillis();
        private final List<Event> events = new ArrayList<>();

        public QueueConsumer(int maxBufferSize) {
            this.maxBufferSize = maxBufferSize;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Event event = queue.take();

                    if (!event.equals(FLUSH_PILL_EVENT)) {
                        events.add(event);
                    }

                    if (events.size() > this.maxBufferSize || (event.equals(FLUSH_PILL_EVENT) && events.size() > 0)) {
                        restClient.post(Entity.entity(events, MediaType.APPLICATION_JSON));
                        lastSentTimeInMillis = System.currentTimeMillis();
                        logger.info("{} event(s) sent to server", events.size());
                        events.clear();
                    }
                } catch (UnlaunchHttpException he) {
                    logger.error("There was an error submitting events to Unlaunch server", he.getMessage());
                } catch (InterruptedException ie) {
                    logger.warn("Thread interrupted. Exiting.");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Unknown exception {}", e.getMessage());
                }
            }

        }


    }
}
