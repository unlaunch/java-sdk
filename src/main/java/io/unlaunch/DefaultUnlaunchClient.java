package io.unlaunch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import io.unlaunch.engine.FeatureFlag;
import io.unlaunch.engine.UnlaunchUser;
import io.unlaunch.event.Impression;
import io.unlaunch.engine.Evaluator;
import io.unlaunch.event.EventHandler;
import io.unlaunch.store.UnlaunchDataStore;
import io.unlaunch.utils.UnlaunchConstants;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * Default client class for the Unlaunch Service and API.
 *
 * This class is not mutable and it is thread-safe.
 *
 * @author umermansoor
 */
final class DefaultUnlaunchClient implements UnlaunchClient {
    protected final EventHandler defaultEventHandler;
    private final UnlaunchDataStore dataStore;
    private final EventHandler flagInvocationMetricHandler;
    private final EventHandler impressionsEventHandler;
    private final Evaluator evaluator = new Evaluator();
    private final BooleanSupplier runCodeOnShutdown;
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);
    private final CountDownLatch initialDownloadDoneLatch;
    private final AtomicBoolean isDownloadSuccessful;

    private static final Logger logger = LoggerFactory.getLogger(DefaultUnlaunchClient.class);

    private DefaultUnlaunchClient (
            UnlaunchDataStore dataStore,
            EventHandler eventHandler,
            EventHandler flagInvocationMetricHandler,
            EventHandler impressionsEventHandler,
            CountDownLatch initialDownloadDoneLatch,
            AtomicBoolean isDownloadSuccessful,
            boolean isOffline,
            BooleanSupplier runCodeOnShutdown) {
        this.flagInvocationMetricHandler = flagInvocationMetricHandler;
        this.impressionsEventHandler = impressionsEventHandler;
        this.dataStore =  dataStore;
        this.initialDownloadDoneLatch = initialDownloadDoneLatch;
        this.isDownloadSuccessful = isDownloadSuccessful;
        this.runCodeOnShutdown = runCodeOnShutdown;
        if (!isOffline) {
            this.defaultEventHandler = eventHandler;

        } else {
            // TODO: What about refreshableDataStoreProvider not being null? It will still Refresh in the background.
            //  Need a different (offline) instance?
            defaultEventHandler = null; // TODO: I don't understand this logic (UM). Alsp track() method will throw NPW if this is null
        }

        // Add shutdown hook to automatically close
        Runtime.getRuntime().addShutdownHook( new Thread(this::shutdown));
    }

    public static DefaultUnlaunchClient create(
            UnlaunchDataStore dataStore,
            EventHandler eventHandler,
            EventHandler flagInvocationMetricHandler,
            EventHandler impressionsEventHandler,
            CountDownLatch initialDownloadDoneLatch,
            AtomicBoolean isDownloadSuccessful,
            boolean isOffline,
            BooleanSupplier runCodeOnShutdown) {
        return new DefaultUnlaunchClient(dataStore, eventHandler, flagInvocationMetricHandler,
                impressionsEventHandler, initialDownloadDoneLatch, isDownloadSuccessful, isOffline, runCodeOnShutdown);
    }

    private UnlaunchFeature evaluate(String flagKey, String identity, UnlaunchAttribute ... attributes) {
        if (flagKey == null || flagKey.isEmpty()) {
            throw new IllegalArgumentException("flagKey must not be null or empty: " + flagKey);
        }

        if (shutdownInitiated.get()) {
            logger.debug("Asked to evaluate flag {} but shutdown already initiated on the client", flagKey);
            return UnlaunchConstants.getControlFeatureByName(flagKey);
        }

        UnlaunchUser user;
        if (attributes == null) {
            user = UnlaunchUser.create(identity);
        } else {
            user = UnlaunchUser.createWithAttributes(identity, attributes);
        }

        FeatureFlag flag;
        try {
            flag = dataStore.getFlag(flagKey);
        } catch (Exception e) {
            return UnlaunchFeature.create(flagKey, UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, null,
                    "there was an error fetching flag: " + e.getMessage() );
        }

        if (flag == null) {
            logger.warn("UnlaunchFeature '{}' not found in the data store. Variation is unknown", flagKey);
            return UnlaunchFeature.create(flagKey, UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, null,
                    "flag was not found in the in-memory cache");
        }

        UnlaunchFeature result  = evaluator.evaluate(flag, user);

        Impression impression = new Impression(flag.getKey(), user.getId(), result.getVariationKey(),
                    flag.isEnabled(), result.getEvaluationReason());
        track(impression);

        return  result;
    }

    @Override
    public String getVariation(String flagKey, String identity, UnlaunchAttribute ... attributes) {
        UnlaunchFeature f =  evaluate(flagKey, identity, attributes);
        return f.getVariationKey();
    }

    @Override
    public String getVariation(String flagKey, String identity) {
        return getVariation(flagKey, identity, null);
    }

    @Override
    public UnlaunchFeature getFeature(String flagKey, String identity, UnlaunchAttribute ... attributes) {
        UnlaunchFeature f =  evaluate(flagKey, identity, attributes);
        return f;
    }

    @Override
    public void awaitUntilReady(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException{
        boolean closed = initialDownloadDoneLatch.await(timeout, unit);

        if (!closed) {
            logger.error("Unlaunch client didn't finish initialization in {} seconds. Check logs are any errors.", unit.toSeconds(timeout) );
            throw new TimeoutException("Unlaunch client was not ready in " +  unit.toSeconds(timeout) + " seconds" );
        }
    }

    @Override
    public boolean isInitialized() {
        try {
            boolean closed = initialDownloadDoneLatch.await(5, TimeUnit.MILLISECONDS);

            if (closed) {
                return isDownloadSuccessful.get();
            }

        } catch (InterruptedException ie) {

        }

        return false;
    }

    @Override
    public UnlaunchFeature getFeature(String flagKey, String identity) {
        return getFeature(flagKey, identity, null);
    }

    /**
     * Close everything by calling close on providerss.
     *
     */
    @Override
    public void shutdown() {
        if (shutdownInitiated.get()) {
            logger.debug("shutdown already initiated on the client");
        } else {
            shutdownInitiated.set(true);
            runCodeOnShutdown.getAsBoolean();
        }
    }

    private void track(Impression impression) {
        if (shutdownInitiated.get()) {
            logger.error("Cannot track impression because client shutdown is already initiated.");
        } else {
            flagInvocationMetricHandler.handle(impression);
            impressionsEventHandler.handle(impression);
        }
    }

    @Override
    public AccountDetails accountDetails() {
        return new AccountDetails(dataStore.getProjectName(), dataStore.getEnvironmentName(),
                dataStore.getAllFlags().size());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new CloneNotSupportedException();
    }

}







