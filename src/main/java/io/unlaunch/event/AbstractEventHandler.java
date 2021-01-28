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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Batched implementation of the {@link EventHandler}.
 *
 * <p>Incoming events are added to a blocking queue instead of being sent to the server immediately. There is a single
 * consumer thread that pulls events from the queue when the maximum batch size is reached, or a special 'flush'
 * event is encountered and sends them to the server.</p>
 *
 * @author umer mansoor
 */
abstract class AbstractEventHandler implements EventHandler {
    private final BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    private final UnlaunchRestWrapper restClient;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ScheduledExecutorService flushExecutor;
    private final String name;
    private final boolean enabled;
    private final int maxBufferSize;
    private AtomicLong lastFlushInMillis = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(AbstractEventHandler.class);

    AbstractEventHandler(String name, boolean enabled, UnlaunchRestWrapper restClient, long flushIntervalInSeconds, int maxBufferSize) {
        this.restClient = restClient;
        this.name = name;
        this.enabled = enabled;
        this.maxBufferSize = maxBufferSize;
        flushExecutor = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder().setNameFormat(name + "-flush" + "-%d").build());
        flushExecutor.scheduleWithFixedDelay(new QueueConsumer(), flushIntervalInSeconds, flushIntervalInSeconds, TimeUnit.SECONDS);
    }


    @Override
    public boolean handle(Event event) {
        if (!enabled || event == null || closed.get()) {
            return false;
        }

        try {
            queue.put(event);

            if (queue.size() >= maxBufferSize) {
                logger.debug("maximum buffer sized reached. flushing.");
                flushExecutor.execute(new QueueConsumer());
            }
            return true;
        } catch (InterruptedException e) {
            logger.warn("Interrupted while adding event to the queue {}. ({})", event, name);
            return false;
        }
    }

    @Override
    public void flush() {
        // Run in the current thread
        new QueueConsumer().run();
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
            flushExecutor.awaitTermination(1, TimeUnit.SECONDS);

            if (flushExecutor.isShutdown()) {
                logger.info("EventHandler ({}) shutdown successfully.", name);
            } else {
                logger.info("EventHandler ({}) was not shutdown successfully: flush executor shutdown: {}",
                        name, flushExecutor.isShutdown());
            }
        } catch (InterruptedException e) {
            flushExecutor.shutdownNow();
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

        public QueueConsumer() {
        }

        @Override
        public void run() {
            if (queue.size() < 1) {
                return; // nothing to do
            }

            try {
                List<Event> events = new ArrayList<>();
                queue.drainTo(events);

                restClient.post(Entity.entity(events, MediaType.APPLICATION_JSON));
                logger.info("{} event(s) submitted. Elapsed time between last run {} seconds ago",
                        events.size(),
                        lastFlushInMillis.get() == 0 ? "never" : (System.currentTimeMillis() - lastFlushInMillis.get())/1000);

                lastFlushInMillis.set(System.currentTimeMillis());
            } catch (UnlaunchHttpException he) {
                logger.error("There was an error submitting events to Unlaunch servers", he.getMessage());
            } catch (Exception e) {
                logger.error("Unknown exception {}", e.getMessage());
            }
        }
    }
}
