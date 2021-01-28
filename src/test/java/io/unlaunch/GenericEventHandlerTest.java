package io.unlaunch;

import io.unlaunch.event.Event;
import io.unlaunch.event.EventHandler;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;

/**
 * author umermansoor
 */
public class GenericEventHandlerTest {

    @Test
    public void testEventIsNull() {
        UnlaunchRestWrapper unlaunchRestWrapper =
                UnlaunchRestWrapper.create("blah", "blah", "blah", 1000, 1000);
        EventHandler eventHandler = EventHandler.createGenericEventHandler("evt", true, unlaunchRestWrapper,1000);
        boolean res = eventHandler.handle(null);
        Assert.assertFalse(res);
        eventHandler.close();
    }

    @Test
    public void testEventsRejectedAfterClosed() {
        UnlaunchRestWrapper unlaunchRestWrapper =
                UnlaunchRestWrapper.create("blah", "blah", "blah", 1000, 1000);
        EventHandler eventHandler =EventHandler.createGenericEventHandler("evt", true, unlaunchRestWrapper, 1000);
        eventHandler.close();
        boolean res = eventHandler.handle(new Event("blah", ""));
        Assert.assertFalse(res);
    }

    @Test
    public void testEventsAreNotSentUntilMaxSizeIsReached() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);

        EventHandler eventHandler = EventHandler.createGenericEventHandler("evt", true, unlaunchRestWrapper,
                Long.MAX_VALUE,
                Integer.MAX_VALUE);

            eventHandler.handle(new Event("blah", ""));

            Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);

            Mockito.verifyZeroInteractions(unlaunchRestWrapper);
            eventHandler.close();
    }

    @Test
    public void testEventsAreSentOutImmediatelyOnFlush()  {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);

        EventHandler eventHandler = EventHandler.createGenericEventHandler("evt", true, unlaunchRestWrapper,
                Long.MAX_VALUE,
                Integer.MAX_VALUE);

            eventHandler.handle(new Event("blah", ""));
            eventHandler.flush();

            Awaitility.await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

            Mockito.verify(unlaunchRestWrapper, Mockito.times(1)).post((Entity<?>) Mockito.any());
            eventHandler.close();
    }

    @Test
    public void testEventsAreSentWhenMaxBufferSizeIsReached() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);

        final int bufferSize = 3;
        EventHandler eventHandler = EventHandler.createGenericEventHandler("evt", true, unlaunchRestWrapper,
                Long.MAX_VALUE,
                bufferSize);

        int count = 0;

        while (count++ < bufferSize) {
            eventHandler.handle(new Event("blah", ""));
        }

        Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        Mockito.verify(unlaunchRestWrapper, Mockito.times(1)).post((Entity<?>) Mockito.any());
        eventHandler.close();
    }

    @Test
    public void testEventsAreSentWhenFlushIntevalIsReached() {
        UnlaunchRestWrapper eventsApiRestClient = Mockito.mock(UnlaunchRestWrapper.class);

        final int flushIntervalInSeconds = 1;
        EventHandler eventHandler = EventHandler.createGenericEventHandler(
                "evt",
                true,
                eventsApiRestClient,
                flushIntervalInSeconds,
                Integer.MAX_VALUE);

        eventHandler.handle(new Event("blah", ""));

        Awaitility.await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

        Mockito.verify(eventsApiRestClient, Mockito.atLeastOnce()).post((Entity<?>) Mockito.any());
        eventHandler.close();
    }
}
