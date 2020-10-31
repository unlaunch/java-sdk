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
 * @author umer mansoor
 */

final class DefaultUnlaunchClientBuilder implements UnlaunchClientBuilder {
    private String sdkKey;
    private boolean isOffline;
    private AccountDetails accountDetail = null;
    private long pollingInterval = 60;
    private TimeUnit pollingIntervalTimeUnit = TimeUnit.SECONDS;
    private long metricsFlushInterval = 30;
    private TimeUnit metricsFlushIntervalTimeUnit = TimeUnit.SECONDS;
    private long eventsFlushInterval = 60;
    private TimeUnit eventsFlushIntervalTimeUnit = TimeUnit.SECONDS;
    private int eventsQueueSize = 500;
    private long impressionsForLiveTailIntervalInSeconds = 5;
    private String host = "http://api.unlaunch.io"; // TODO Change to https;
    private final String flagApiPath = "/api/v1/flags";
    private final String eventApiPath = "/api/v1/events";
    private final String impressionApiPath = "/api/v1/impressions";
    private  String yamlFeaturesFilePath;

    // To reduce load on server from aggressive settings
    public static int MIN_POLL_INTERVAL_IN_SECONDS = 15;
    public static int MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS = 10;
    public static int MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS = 15;
    public static int MIN_EVENTS_QUEUE_SIZE = 500;

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
        logger.warn("setting host manually is only for enterprise users or for testing.");
        this.host = host;
        return this;
    }


    @Override
    public UnlaunchClientBuilder metricsFlushInterval(long interval, TimeUnit unit) {
        this.metricsFlushInterval = interval;
        this.metricsFlushIntervalTimeUnit = unit;
        return this;
    }


    @Override
    public UnlaunchClientBuilder eventsFlushInterval(long interval, TimeUnit unit) {
        this.eventsFlushInterval = interval;
        this.eventsFlushIntervalTimeUnit = unit;
        return this;
    }

    @Override
    public UnlaunchClientBuilder eventsQueueSize(int maxQueueSize) {
        this.eventsQueueSize = maxQueueSize;
        return this;
    }

    @Override
    public UnlaunchClientBuilder enableLazyLoading() {
        throw new UnlaunchRuntimeException("Not yet implemented.");
    }

    /**
     * Builds and returns an new {@link UnlaunchClient}.
     *
     * @return
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public UnlaunchClient build() {
        validateConfigurationParameters();

        UnlaunchClient client;

        if (this.isOffline) {
            if (yamlFeaturesFilePath  == null) {
                client = new OfflineUnlaunchClient();
            } else {
                client = new OfflineUnlaunchClient(yamlFeaturesFilePath);
            }
        } else {
            client = createDefaultClient();
        }

        logger.info("client built with following parameters {}", getConfigurationAsPrintableString());
        return client;
    }

    private UnlaunchClient createDefaultClient() {
        long pollingIntervalInSeconds =  pollingIntervalTimeUnit.toSeconds(pollingInterval);
        if (pollingIntervalInSeconds < MIN_POLL_INTERVAL_IN_SECONDS) {
            logger.warn("The pollingInterval must be equal than or greater than {} seconds. Setting it to that. ",
                    MIN_POLL_INTERVAL_IN_SECONDS );
        }

        UnlaunchRestWrapper restWrapperForFlagApi = UnlaunchRestWrapper.create(sdkKey, host, flagApiPath);
        final CountDownLatch initialDownloadDoneLatch = new CountDownLatch(1);
         final AtomicBoolean downloadSuccessful = new AtomicBoolean(false);
        RefreshableDataStoreProvider refreshableDataStoreProvider =
                new RefreshableDataStoreProvider(restWrapperForFlagApi, initialDownloadDoneLatch, downloadSuccessful,
                        pollingIntervalInSeconds);

        // Try to make sure there are no errors or abandon object construction
        UnlaunchDataStore dataStore = refreshableDataStoreProvider.getNoOpDataStore();
        try {
            dataStore = refreshableDataStoreProvider.getDataStore();
        } catch (Exception e) {
            logger.error("Unable to download features and init. Make sure you're using the " +
                    "correct SDK Key. We'll retry again but this error  is usually not recoverable.");
        }

        // This is currently not is use; we'll use this for event tracking
        long eventFlushIntervalInSeconds = eventsFlushIntervalTimeUnit.toSeconds(eventsFlushInterval);
        UnlaunchRestWrapper eventsApiRestClient = UnlaunchRestWrapper.create(sdkKey, host, eventApiPath);
        EventHandler eventHandler = EventHandler.createGenericEventHandler("generic",
                eventsApiRestClient, eventFlushIntervalInSeconds, eventsQueueSize);

        UnlaunchRestWrapper impressionApiRestClient = UnlaunchRestWrapper.create(sdkKey, host, impressionApiPath);
        EventHandler impressionsEventHandler = EventHandler.createGenericEventHandler("impression",
                impressionApiRestClient, impressionsForLiveTailIntervalInSeconds, 100);

        EventHandler variationsCountEventHandler = EventHandler.createCountAggregatorEventHandler(eventHandler, metricsFlushInterval,
                metricsFlushIntervalTimeUnit);

        return  DefaultUnlaunchClient.create(
                dataStore, eventHandler, variationsCountEventHandler, impressionsEventHandler,
                initialDownloadDoneLatch, downloadSuccessful,
                isOffline, () -> {
                    if (refreshableDataStoreProvider != null) {
                        refreshableDataStoreProvider.close();
                    }

                    if (variationsCountEventHandler != null) {
                        variationsCountEventHandler.close();
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

    // This method will throw exception is errors are encountered
    private void validateConfigurationParameters() {
        try {
            // if not offline, check for SDK key and host
            if (!isOffline) {
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
                    throw new IllegalArgumentException("hostname cannot be null or empty. Must point to a valid Unlaunch Service host");
                }
            }

            Preconditions.checkArgument(pollingIntervalTimeUnit != null, "pollingIntervalTimeUnit cannot be null");
            Preconditions.checkArgument(pollingIntervalTimeUnit.toSeconds(pollingInterval) >= MIN_POLL_INTERVAL_IN_SECONDS,
                    "pollingInterval() must be great than " + MIN_POLL_INTERVAL_IN_SECONDS);

            Preconditions.checkArgument(metricsFlushIntervalTimeUnit != null, "metricsFlushIntervalTimeUnit cannot be null");
            Preconditions.checkArgument(metricsFlushIntervalTimeUnit.toSeconds(metricsFlushInterval) >= MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS,
                    "metricsFlushInterval() must be great than " + MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS);

            Preconditions.checkArgument(eventsFlushIntervalTimeUnit != null, "eventsFlushIntervalTimeUnit cannot be null");
            Preconditions.checkArgument(eventsFlushIntervalTimeUnit.toSeconds(eventsFlushInterval) >= MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS,
                    "eventsFlushInterval() must be great than " + MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS);

            Preconditions.checkArgument(eventsQueueSize >= 500, "eventsQueue must be at least 500");

        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getConfigurationAsPrintableString() {
        return "isOffline=" + isOffline +
                ", pollingInterval (seconds) =" + pollingIntervalTimeUnit.toSeconds(pollingInterval) +
                ", metricsFlushInterval (seconds) =" + metricsFlushIntervalTimeUnit.toSeconds(metricsFlushInterval) +
                ", host='" + host + '\'' +
                '}';
    }
}
