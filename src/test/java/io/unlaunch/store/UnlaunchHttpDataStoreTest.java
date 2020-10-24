package io.unlaunch.store;

import io.unlaunch.exceptions.UnlaunchHttpException;
import io.unlaunch.UnlaunchRestWrapper;
import io.unlaunch.utils.UnlaunchData;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class UnlaunchHttpDataStoreTest {

    @Test
    public void testLatchAndSuccessThereIsAnHttpError() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        when(unlaunchRestWrapper.get(any())).thenThrow(new UnlaunchHttpException());
        CountDownLatch latch = Mockito.mock(CountDownLatch.class);
        AtomicBoolean atomicBoolean = new AtomicBoolean();

        UnlaunchHttpDataStore unlaunchHttpFetcher = new UnlaunchHttpDataStore(unlaunchRestWrapper, latch, atomicBoolean);

        executeRunnable(unlaunchHttpFetcher);

        Mockito.verify(latch, Mockito.times(1)).countDown();
        Assert.assertFalse(atomicBoolean.get());

    }

    @Test
    public void testCountdownLatchIsDecrementedOnServerResponse() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        when(unlaunchRestWrapper.get(any())).thenReturn(UnlaunchData.flagsResponseFromServerWithOneFlag());
        CountDownLatch latch = Mockito.mock(CountDownLatch.class);
        AtomicBoolean atomicBoolean = new AtomicBoolean();

        UnlaunchHttpDataStore unlaunchHttpFetcher = new UnlaunchHttpDataStore(unlaunchRestWrapper, latch, atomicBoolean);

        executeRunnable(unlaunchHttpFetcher);


        Mockito.verify(latch, Mockito.times(1)).countDown();
        Assert.assertTrue(atomicBoolean.get());
    }

    @Test
    public void testParsedFlagDataWhenSuccessfullyDownloaded() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        when(unlaunchRestWrapper.get(any())).thenReturn(UnlaunchData.flagsResponseFromServerWithOneFlag());
        CountDownLatch latch = Mockito.mock(CountDownLatch.class);
        AtomicBoolean atomicBoolean = new AtomicBoolean();

        UnlaunchHttpDataStore ulDataStore = new UnlaunchHttpDataStore(unlaunchRestWrapper, latch, atomicBoolean);
        executeRunnable(ulDataStore);

        Assert.assertTrue(ulDataStore.getAllFlags().size() == 1);
        Assert.assertTrue(ulDataStore.isFlagExist("demoflag"));
        Assert.assertEquals("Test", ulDataStore.getEnvironmentName());
        Assert.assertEquals("Project 1", ulDataStore.getProjectName());

        ulDataStore.close();
    }

    private void executeRunnable(Runnable runnable)  {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(runnable);
            Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }

}
