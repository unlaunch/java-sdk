package io.unlaunch;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.unlaunch.event.EventHandler;
import io.unlaunch.exceptions.UnlaunchRuntimeException;
import io.unlaunch.store.RefreshableDataStoreProvider;
import io.unlaunch.store.UnlaunchDataStore;
import io.unlaunch.utils.UnlaunchConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Internal implementation of default builder for building Unlaunch clients.
 *
 * <>This class is mutable and hence it is NOT Thread-safe.</>
 *
 * @author umermansoor
 */

final class DefaultUnlaunchClientBuilder implements UnlaunchClientBuilder {
    private String sdkKey;
    private boolean isOffline;
    private AccountDetails accountDetail = null;
    private long pollingInterval = 60;
    private TimeUnit pollingIntervalTimeUnit = TimeUnit.SECONDS;
    private long eventFlushInterval = 60;
    private TimeUnit eventFlushIntervalTimeUnit = TimeUnit.SECONDS;
    private String host = "http://api.unlaunch.io";
    private final String flagApiPath = "/api/v1/flags";
    private final String eventApiPath = "/api/v1/events";
    private final String impressionApiPath = "/api/v1/impressions";
    private  String yamlFeaturesFilePath;

    // To reduce load on server from aggressive settings
    private static int MIN_POLL_INTERVAL_IN_SECONDS = 15;

    private static final Logger logger = LoggerFactory.getLogger(DefaultUnlaunchClientBuilder.class);

    @Override
    public UnlaunchClientBuilder sdkKey(String sdkKey) {
        this.sdkKey = sdkKey;
        return this;
    }

    @Override
    public UnlaunchClientBuilder offlineMode() {
        this.isOffline = true;
        return this;
    }

    @Override
    public UnlaunchClientBuilder offlineModeWithLocalFeatures(String yamlFeaturesFilePath) {
        this.isOffline = true;
        this.yamlFeaturesFilePath = yamlFeaturesFilePath;
        return this;
    }

    @Override
    public UnlaunchClientBuilder pollingInterval(long interval, TimeUnit unit) {
        this.pollingInterval = interval;
        this.pollingIntervalTimeUnit = unit;
        return this;
    }

    @Override
    public UnlaunchClientBuilder host(String host) {
        logger.warn("setting Unlaunch host is dangerous - make sure you know what you are doing");
        this.host = host;
        return this;
    }

    @Override
    public UnlaunchClientBuilder eventFlushInterval(long interval, TimeUnit unit) {
        this.eventFlushInterval = interval;
        this.eventFlushIntervalTimeUnit = unit;
        return this;
    }

    @Override
    public UnlaunchClientBuilder enableLazyLoading() {
        throw new UnlaunchRuntimeException("Not yet implemented.");
    }

    /**
     *
     * @return
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public UnlaunchClient build() {

        try {

            if (!isOffline) { // if not offline, check for SDK key and host
                if (Strings.isNullOrEmpty(sdkKey)) {
                    // User didn't supply SDK key, try reading from environment variable
                    String s = System.getenv(UnlaunchConstants.SDK_KEY_ENV_VARIABLE_NAME);
                    if (Strings.isNullOrEmpty(s)) {
                        throw new IllegalArgumentException("sdkKey cannot be null or empty. Must be supplied to the " +
                                "builder or set as an environment variable.");
                    } else {
                        logger.info("Setting SDK Key read from environment variable");
                        sdkKey = s;
                    }
                }

                if (Strings.isNullOrEmpty(host)) {
                    throw new IllegalArgumentException("hostname cannot be null or empty. Must point to a valid Unlaunch " +
                            "Service host");
                }
            }

            Preconditions.checkArgument(pollingInterval > 0, "Polling interval cannot be <= 0");
            Preconditions.checkArgument(pollingIntervalTimeUnit != null, "Polling interval TimeUnit cannot be null");
            Preconditions.checkArgument(eventFlushInterval > 0, "Event flush interval cannot be <= 0");
            Preconditions.checkArgument(eventFlushIntervalTimeUnit != null, "Event flush TimeUnit cannot be null");

        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException(e);
        }

        if (this.isOffline) {

            if (yamlFeaturesFilePath  == null) {
                return new OfflineUnlaunchClient();
            } else {
                return new OfflineUnlaunchClient(yamlFeaturesFilePath);
            }

        } else {
            return createDefaultClient();
        }
    }

    private UnlaunchClient createDefaultClient() {
        long pollingIntervalInSeconds =  pollingIntervalTimeUnit.toSeconds(pollingInterval);
        if (pollingIntervalInSeconds < MIN_POLL_INTERVAL_IN_SECONDS) {
            logger.warn("The pollingInterval must be equal than or greater than {} seconds. Setting it to that. ",
                    MIN_POLL_INTERVAL_IN_SECONDS );
        }

        long eventFlushIntervalInSeconds = eventFlushIntervalTimeUnit.toSeconds(eventFlushInterval);
        if (eventFlushInterval < MIN_POLL_INTERVAL_IN_SECONDS ) {
            logger.warn("The eventFlushInterval must be equal than or greater than {} seconds. Setting it to that. ",
                    MIN_POLL_INTERVAL_IN_SECONDS );
        }

        UnlaunchRestWrapper restWrapperForFlagApi = UnlaunchRestWrapper.create(sdkKey, host, flagApiPath);
        final CountDownLatch initialDownloadDoneLatch = new CountDownLatch(1);
         final AtomicBoolean isDownloadSuccess = new AtomicBoolean(false);
        RefreshableDataStoreProvider refreshableDataStoreProvider =
                new RefreshableDataStoreProvider(restWrapperForFlagApi, initialDownloadDoneLatch, isDownloadSuccess,
                        pollingIntervalInSeconds);

        // Try to make sure there are no errors or abandon object construction
        UnlaunchDataStore dataStore = refreshableDataStoreProvider.getNoOpDataStore();
        try {
            dataStore = refreshableDataStoreProvider.getDataStore();
        } catch (Exception e) {
            logger.error("Unable to download features and init. Make sure you're using the " +
                    "correct SDK Key. We'll retry again but this error  is usually not recoverable.");
        }

        UnlaunchRestWrapper eventsApiRestClient = UnlaunchRestWrapper.create(sdkKey, host, eventApiPath);
        EventHandler eventHandler = EventHandler.createGenericEventHandler("generic",
                eventsApiRestClient, eventFlushIntervalInSeconds);

        UnlaunchRestWrapper impressionApiRestClient = UnlaunchRestWrapper.create(sdkKey, host, impressionApiPath);
        EventHandler impressionsEventHandler = EventHandler.createGenericEventHandler("impression",
                impressionApiRestClient, 1);

        EventHandler flagInvocationMetricHandler = EventHandler.createCountAggregatorEventHandler(eventHandler, 30,
                TimeUnit.SECONDS);

        return  DefaultUnlaunchClient.create(
                dataStore, eventHandler, flagInvocationMetricHandler, impressionsEventHandler,
                initialDownloadDoneLatch, isDownloadSuccess,
                isOffline, () -> {
                    if (refreshableDataStoreProvider != null) {
                        refreshableDataStoreProvider.close();
                    }

                    if (flagInvocationMetricHandler != null) {
                        flagInvocationMetricHandler.close();
                    }

                    if (eventHandler != null) {
                        eventHandler.close();
                    }

                    if (impressionsEventHandler != null) {
                        impressionsEventHandler.close();
                    }

                    return true;
                });
    }
}
