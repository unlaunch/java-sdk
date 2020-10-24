package io.unlaunch.event;

import io.unlaunch.event.Event;
import io.unlaunch.utils.UnlaunchConstants;
import io.unlaunch.event.EventHandler;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author umermansoor
 */
public class CountAggregatorEventHandlerTest {

    @Test
    public void testVariationCountUpdate() {
        EventHandler eventHandler = Mockito.mock(EventHandler.class);
        EventHandler flagInvocationMetricHandler = EventHandler.createCountAggregatorEventHandler(eventHandler,
                500, TimeUnit.MILLISECONDS);
        Event event = new Event("ignore", "blah", "blah");
        flagInvocationMetricHandler.handle(event);
        Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);
        Mockito.verify(eventHandler, Mockito.times(1)).handle(any());
        eventHandler.close();
    }

    @Test
    public void testVariationThatEventsAreAggregated() {
        EventHandler eventHandler = Mockito.mock(EventHandler.class);
        EventHandler flagInvocationMetricHandler = EventHandler.createCountAggregatorEventHandler(eventHandler,
                500, TimeUnit.MILLISECONDS);

        String flagId = "blah";
        String variationId = "blah";
        Event event = new Event("ignore", flagId , variationId);
        flagInvocationMetricHandler.handle(event);
        flagInvocationMetricHandler.handle(event);

        Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        // Both events (same flag) resulted in one event handler call
        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);
        Mockito.verify(eventHandler, Mockito.times(1)).handle(argument.capture());

        Event sentEvent = argument.getValue();

        Assert.assertEquals(UnlaunchConstants.FLAG_INVOCATIONS_COUNT_EVENT_TYPE, sentEvent.getType());
        Assert.assertEquals(flagId, sentEvent.getKey());
        Assert.assertEquals(2, sentEvent.getProperties().get(variationId));

        eventHandler.close();
    }

    @Test
    public void testIfEventHandlerHasAProblemOneFirstCallButSecondSucceeds() {
        EventHandler eventHandler = Mockito.mock(EventHandler.class);
        when(eventHandler.handle(any())).thenThrow(new RuntimeException()).thenReturn(true);
        EventHandler flagInvocationMetricHandler = EventHandler.createCountAggregatorEventHandler(eventHandler,
                500, TimeUnit.MILLISECONDS);
        Event event = new Event("ignore", "blah", "blah");
        flagInvocationMetricHandler.handle(event);

        Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        // Next time it should go through and recover
        flagInvocationMetricHandler.handle(event);
        Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);
        Mockito.verify(eventHandler, Mockito.times(2)).handle(any());
    }
}
