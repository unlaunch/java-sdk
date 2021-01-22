package io.unlaunch.store;

import io.unlaunch.exceptions.UnlaunchHttpException;
import io.unlaunch.UnlaunchRestWrapper;

import io.unlaunch.utils.UnlaunchTestHelper;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.when;

/**
 * @author umermansoor
 */
public class RefreshableDataStoreProviderTest {
    final CountDownLatch downLatch = new CountDownLatch(1);
    final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    @Test
    public void testThatOnlySingletonInstanceIsCreated() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        Response response = Mockito.mock(Response.class);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(String.class)).thenReturn(UnlaunchTestHelper.flagsResponseFromServerWithOneFlag());
        when(unlaunchRestWrapper.get()).thenReturn(response);

        RefreshableDataStoreProvider dataStoreProvider = new RefreshableDataStoreProvider(
                unlaunchRestWrapper,
                downLatch,
                atomicBoolean,
                Long.MAX_VALUE
        );

        UnlaunchDataStore obj1 =  dataStoreProvider.getDataStore();
        UnlaunchDataStore obj2 =  dataStoreProvider.getDataStore();
        Assert.assertSame(obj1, obj2);

        dataStoreProvider.close();
    }

    @Test
    public void testNoExceptionIsThrownWhenThereIsAProblem() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        when(unlaunchRestWrapper.get()).thenThrow(new UnlaunchHttpException());

        RefreshableDataStoreProvider dataStoreProvider = new RefreshableDataStoreProvider(
                unlaunchRestWrapper,
                downLatch,
                atomicBoolean,
                Long.MAX_VALUE
        );

        // This should not throw an exception
        dataStoreProvider.getDataStore();
    }

    @Test
    public void testDataStoreIsTriedRepeatedlyWhenThereAreErrors() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        when(unlaunchRestWrapper.get()).thenThrow(new UnlaunchHttpException());

        RefreshableDataStoreProvider dataStoreProvider = new RefreshableDataStoreProvider(
                unlaunchRestWrapper,
                downLatch,
                atomicBoolean,
                1
        );

        UnlaunchHttpDataStore dataStore = (UnlaunchHttpDataStore) dataStoreProvider.getDataStore();

        Awaitility.await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

        Assert.assertTrue(dataStore.getNumberOfHttpCalls() >= 2);
    }
}
