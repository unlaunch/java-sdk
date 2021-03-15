package io.unlaunch.store;

import io.unlaunch.UnlaunchGenericRestWrapper;
import io.unlaunch.exceptions.UnlaunchHttpException;
import io.unlaunch.UnlaunchRestWrapper;

import io.unlaunch.utils.UnlaunchTestHelper;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
        UnlaunchGenericRestWrapper ugrw = Mockito.mock(UnlaunchGenericRestWrapper.class);

        Response response = Mockito.mock(Response.class);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(String.class)).thenReturn(UnlaunchTestHelper.flagsResponseFromServerWithOneFlag());
        when(unlaunchRestWrapper.get()).thenReturn(response);

        RefreshableDataStoreProvider dataStoreProvider = new RefreshableDataStoreProvider(
                unlaunchRestWrapper,
                ugrw,
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
        UnlaunchGenericRestWrapper ugrw = Mockito.mock(UnlaunchGenericRestWrapper.class);

        when(unlaunchRestWrapper.get()).thenThrow(new UnlaunchHttpException());

        RefreshableDataStoreProvider dataStoreProvider = new RefreshableDataStoreProvider(
                unlaunchRestWrapper,
                ugrw,
                downLatch,
                atomicBoolean,
                Long.MAX_VALUE
        );

        // This should not throw an exception
        dataStoreProvider.getDataStore();
    }

    @Test
    public void testWhen_Sync0Fails_Then_RegularSyncIsCalledImmediately() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        UnlaunchGenericRestWrapper ugrw = Mockito.mock(UnlaunchGenericRestWrapper.class);

        when(ugrw.get()).thenThrow(new UnlaunchHttpException());

        RefreshableDataStoreProvider dataStoreProvider = new RefreshableDataStoreProvider(
                unlaunchRestWrapper,
                ugrw,
                downLatch,
                atomicBoolean,
                Integer.MAX_VALUE // never re-sync
        );

        UnlaunchHttpDataStore dataStore = (UnlaunchHttpDataStore) dataStoreProvider.getDataStore();


        Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        Mockito.verify(ugrw, Mockito.times(1)).get();
        Mockito.verify(unlaunchRestWrapper, Mockito.times(1)).get();
        Assert.assertTrue(dataStore.getNumberOfHttpCalls() == 1);
    }

    @Test
    public void testDataStoreIsTriedRepeatedlyWhenThereAreErrors() {
        UnlaunchRestWrapper unlaunchRestWrapper = Mockito.mock(UnlaunchRestWrapper.class);
        UnlaunchGenericRestWrapper ugrw = Mockito.mock(UnlaunchGenericRestWrapper.class);

        when(ugrw.get()).thenThrow(new UnlaunchHttpException());
        when(unlaunchRestWrapper.get()).thenThrow(new UnlaunchHttpException());

        RefreshableDataStoreProvider dataStoreProvider = new RefreshableDataStoreProvider(
                unlaunchRestWrapper,
                ugrw,
                downLatch,
                atomicBoolean,
                1
        );

        UnlaunchHttpDataStore dataStore = (UnlaunchHttpDataStore) dataStoreProvider.getDataStore();

        Awaitility.await().pollDelay(Durations.FIVE_SECONDS).until(() -> true);

        Assert.assertTrue(dataStore.getNumberOfHttpCalls() >= 2);
    }
}
