package io.unlaunch;

import io.unlaunch.engine.FeatureFlag;
import io.unlaunch.engine.Variation;
import io.unlaunch.event.EventHandler;
import io.unlaunch.store.RefreshableDataStoreProvider;
import io.unlaunch.store.UnlaunchDataStore;
import io.unlaunch.utils.UnlaunchConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 *
 * @author umermansoor
 */
public class DefaultUnlaunchClientTest {

    RefreshableDataStoreProvider refreshableDataStoreProvider = Mockito.mock(RefreshableDataStoreProvider.class);
    UnlaunchDataStore unlaunchHttpDataStore = Mockito.mock(UnlaunchDataStore.class);
    EventHandler eventHandler = Mockito.mock(EventHandler.class);
    EventHandler flagInvocationMetricHandler = Mockito.mock(EventHandler.class);
    EventHandler impressionsEventHandler = Mockito.mock(EventHandler.class);
    boolean isOffline = false;

    final CountDownLatch downLatch = new CountDownLatch(1);
    final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    @Before
    public void beforeEachTestReInitializeMocks() {
        refreshableDataStoreProvider = Mockito.mock(RefreshableDataStoreProvider.class);
        unlaunchHttpDataStore = Mockito.mock(UnlaunchDataStore.class);
        when(refreshableDataStoreProvider.getDataStore()).thenReturn(unlaunchHttpDataStore);

         eventHandler = Mockito.mock(EventHandler.class);
        flagInvocationMetricHandler = Mockito.mock(EventHandler.class);
    }

    @Test
    public void testConstruction() {
        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, downLatch, atomicBoolean,
                 Boolean.TRUE::booleanValue);

        Assert.assertNotNull(client);
    }

    @Test
    public void tesWhenFlagIsDisabledAndOffVariationIsServed() {
        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, downLatch, atomicBoolean,
                Boolean.TRUE::booleanValue);

        final String flagKey = "flag123";
        FeatureFlag flag  = Mockito.mock(FeatureFlag.class);
        when(unlaunchHttpDataStore.getFlag(flagKey)).thenReturn(flag);
        when(flag.isEnabled()).thenReturn(false);

        Variation variation = Mockito.mock(Variation.class);
        final String variationKey = "false";
        when(variation.getKey()).thenReturn(variationKey);
        when(flag.getOffVariation()).thenReturn(variation);

        Assert.assertEquals(variationKey, client.getVariation(flagKey, UUID.randomUUID().toString()));
    }

    @Test
    public void testDefaultValueIsReturnedWhenTheFlagIsNotFound() {
        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, downLatch, atomicBoolean,
                Boolean.TRUE::booleanValue);

        when(unlaunchHttpDataStore.getFlag(any())).thenReturn(null);

        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, client.getVariation("unknownflag",
                UUID.randomUUID().toString()));

    }

    @Test
    public void testFallbackValueIsReturnedWhenDataStoreThrowsAnException() {
        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, downLatch, atomicBoolean,
                Boolean.TRUE::booleanValue);

        when(unlaunchHttpDataStore.getFlag(any())).thenThrow(new RuntimeException());

        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, client.getVariation("unreachableflag",
                UUID.randomUUID().toString()));
    }

    @Test
    public void testFlagWhenFlagIsDisabledAndOffVariationIsServed() {
        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, downLatch, atomicBoolean,
                Boolean.TRUE::booleanValue);

        final String flagKey = "flag123";

        FeatureFlag flag  = Mockito.mock(FeatureFlag.class);
        when(unlaunchHttpDataStore.getFlag(flagKey)).thenReturn(flag);
        when(flag.isEnabled()).thenReturn(false);
        when(flag.getType()).thenReturn("string");

        Variation variation = Mockito.mock(Variation.class);
        final String VALUE ="RESULT";
        when(variation.getKey()).thenReturn(VALUE);
        when(flag.getOffVariation()).thenReturn(variation);

        final String result = client.getVariation(flagKey, UUID.randomUUID().toString());
        Assert.assertEquals(VALUE, result);
    }

    @Test(expected = TimeoutException.class)
    public void testAwaitThrowsTimeOutExceptionIfNotReady() throws TimeoutException {
        final CountDownLatch latchNeverCloses = new CountDownLatch(1);

        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, latchNeverCloses, atomicBoolean,
                Boolean.TRUE::booleanValue);

        try {
            client.awaitUntilReady(100, TimeUnit.MILLISECONDS);
        } catch(InterruptedException e) {
            Assert.fail();
        } catch (TimeoutException te) {
            throw new TimeoutException(te.getMessage());
        }
    }

    @Test
    public void testAwaitUnblocksWhenLatchIsClosed()  {
        final CountDownLatch latchThatCloses = new CountDownLatch(1);

        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, latchThatCloses, atomicBoolean,
                Boolean.TRUE::booleanValue);

        latchThatCloses.countDown();

        try {
            client.awaitUntilReady(100, TimeUnit.MILLISECONDS);
        } catch(InterruptedException | TimeoutException e) {
            Assert.fail();
        }
    }

    @Test
    public void testClientReturnsControlVariationAfterShutdown() {
        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, downLatch, atomicBoolean,
                Boolean.TRUE::booleanValue);

        final String flagKey = "flag123";
        FeatureFlag flag  = Mockito.mock(FeatureFlag.class);
        when(unlaunchHttpDataStore.getFlag(flagKey)).thenReturn(flag);
        when(flag.isEnabled()).thenReturn(false);

        Variation variation = Mockito.mock(Variation.class);
        final String variationKey = "ON";
        when(variation.getKey()).thenReturn(variationKey);
        when(flag.getOffVariation()).thenReturn(variation);

        String v = client.getVariation(flagKey, UUID.randomUUID().toString());
        Assert.assertTrue(v.isEmpty() == false);

        client.shutdown();

        // After shutdown, the variation that's returned must be empty string
        v = client.getVariation(flagKey, UUID.randomUUID().toString());
        Assert.assertTrue(v.isEmpty());

       UnlaunchFeature f = client.getFeature(flagKey, UUID.randomUUID().toString());
        Assert.assertTrue(v.isEmpty());
    }

    @Test
    public void testIsInitializedReturnsTrueWhenClientIsReady()  {
        final CountDownLatch latchThatCloses = new CountDownLatch(1);
        final AtomicBoolean downloadSuccess = new AtomicBoolean();

        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, latchThatCloses, downloadSuccess,
                Boolean.TRUE::booleanValue);

        latchThatCloses.countDown();
        downloadSuccess.set(true);

        Assert.assertTrue(client.isReady());
    }

    @Test
    public void testIsInitializedReturnsFalseWhenClientIsNotReady()  {
        final CountDownLatch latchThatCloses = new CountDownLatch(1);
        final AtomicBoolean downloadSuccess = new AtomicBoolean();

        DefaultUnlaunchClient client = DefaultUnlaunchClient.create(refreshableDataStoreProvider.getDataStore(),
                eventHandler, flagInvocationMetricHandler, impressionsEventHandler, latchThatCloses, downloadSuccess,
                Boolean.TRUE::booleanValue);

        latchThatCloses.countDown();
        Assert.assertFalse(client.isReady());
    }

}
