package io.unlaunch;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.unlaunch.event.EventHandler;
import io.unlaunch.exceptions.UnlaunchRuntimeException;
import io.unlaunch.store.RefreshableDataStoreProvider;
import io.unlaunch.store.UnlaunchDataStore;
import io.unlaunch.utils.UnlaunchConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Internal implementation of default builder for building Unlaunch clients.
 *
 * <p>This class is mutable and hence it is NOT Thread-safe.</p>
 *
 * @author umer mansoor
 */

final class DefaultUnlaunchClientBuilder implements UnlaunchClientBuilder {

    private String sdkKey;
    private boolean isOffline;
    private long pollingInterval = 60;
    private TimeUnit pollingIntervalTimeUnit = TimeUnit.SECONDS;
    private long metricsFlushInterval = 30;
    private TimeUnit metricsFlushIntervalTimeUnit = TimeUnit.SECONDS;
    private int metricsQueueSize = 100;
    private long eventsFlushInterval = 60;
    private TimeUnit eventsFlushIntervalTimeUnit = TimeUnit.SECONDS;
    private int eventsQueueSize = 500;
    private String host = "https://api.unlaunch.io";
    private String s3BucketHost = "https://api-unlaunch-io-master-flags.s3-us-west-1.amazonaws.com";
    private long connectionTimeoutMs = 10_000;
    private long readTimeoutMs = 10_000;
    private  String yamlFeaturesFilePath;

    // These are internal flags to track if values are updated by the user. If so,
    // don't change these by environments e.g. Pre-production vs Production.
    private boolean pollingIntervalUpdatedByUser;
    private boolean metricsFlushIntervalUpdatedByUser;
    private boolean eventsFlushIntervalUpdatedByUser;

    private final String flagApiPath = "/api/v1/flags";
    private final String eventApiPath = "/api/v1/events";
    private final String impressionApiPath = "/api/v1/impressions";


    // To reduce load on server from aggressive settings
    public static int MIN_POLL_INTERVAL_IN_SECONDS = 15;
    public static int MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS = 15;
    public static int MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS = 15;
    public static int MIN_EVENTS_QUEUE_SIZE = 500;
    public static int MIN_METRICS_QUEUE_SIZE = 100;
    public static int MIN_CONNECTION_TIMEOUT_MILLIS = 1000;
    public static int MIN_READOUT_TIMEOUT_MILLIS = 1000;

    private static final Map<String, UnlaunchClient> Clients = new ConcurrentHashMap<>();

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
        this.pollingIntervalUpdatedByUser = true;
        return this;
    }

    @Override
    public UnlaunchClientBuilder connectionTimeout(long timeout, TimeUnit unit) {
        connectionTimeoutMs = unit.toMillis(timeout);
        return this;
    }

    @Override
    public UnlaunchClientBuilder readTimeout(long timeout, TimeUnit unit) {
        readTimeoutMs = unit.toMillis(timeout);
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
        this.metricsFlushIntervalUpdatedByUser = true;
        return this;
    }

    @Override
    public UnlaunchClientBuilder metricsQueueSize(int maxQueueSize) {
        this.metricsQueueSize = maxQueueSize;
        return this;
    }

    @Override
    public UnlaunchClientBuilder eventsFlushInterval(long interval, TimeUnit unit) {
        this.eventsFlushInterval = interval;
        this.eventsFlushIntervalTimeUnit = unit;
        this.eventsFlushIntervalUpdatedByUser = true;
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

        buildS3BucketAddress();

        if (sdkKey != null && !sdkKey.isEmpty()) {
            if (!sdkKey.startsWith("prod")) {
                logger.info("SDK key doesn't appear to be for production environment. Using relaxed settings to " +
                        "poll and sync events more frequently so changes sync faster.");
                loadPreProductionDefaults();
            }

            if (sdkKey.contains("-mob-")) {
                logger.warn("You are using 'Mobile / App SDK Key'. The SDK will only be able to download flags that " +
                        "have the client-side option enabled. If you are running this application on your own " +
                        "servers, use the 'Server Key' to fetch all features flags. {}", UnlaunchConstants.getSdkKeyHelpMessage());
            } else if (sdkKey.contains("-public-")) {
                logger.warn("You are using 'Browser / Public SDK Key'. The SDK will only be able to download flags that " +
                        "have the client-side option enabled. If you are running this application on your own " +
                        "servers, use the 'Server Key' to fetch all features flags. {}",
                        UnlaunchConstants.getSdkKeyHelpMessage());
            }
        }

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
            checkIfClientAlreadyCreated(client);
        }

        logConfigurationOptions();
        return client;
    }

    private void checkIfClientAlreadyCreated(UnlaunchClient client) {
        if (Clients.containsKey(sdkKey)) {
            logger.warn("Duplicated Unlaunch client is created for sdk key {}. Consider creating only one client per sdkKey.", sdkKey);
        } else {
            Clients.put(sdkKey, client);
        }
    }

    private void buildS3BucketAddress() {
        if (host == null || host.isEmpty()) {
            throw new IllegalStateException("The host cannot be null or empty");
        }

        if (!host.equals("https://api.unlaunch.io")) {
            s3BucketHost = "https://app-qa-unlaunch-io-master-flags.s3-us-west-1.amazonaws.com";
        }

    }

    private UnlaunchClient createDefaultClient() {
        long pollingIntervalInSeconds =  pollingIntervalTimeUnit.toSeconds(pollingInterval);
        if (pollingIntervalInSeconds < MIN_POLL_INTERVAL_IN_SECONDS) {
            logger.warn("The pollingInterval must be equal than or greater than {} seconds. Setting it to that. ",
                    MIN_POLL_INTERVAL_IN_SECONDS );
        }

        UnlaunchRestWrapper restWrapperForFlagApi =
                UnlaunchRestWrapper.create(sdkKey, host, flagApiPath, connectionTimeoutMs, readTimeoutMs);
        final CountDownLatch initialSyncCompleteLatch = new CountDownLatch(1);
        final AtomicBoolean initialSyncSuccessful = new AtomicBoolean(false);

        UnlaunchGenericRestWrapper s3BucketRestClient =
                UnlaunchGenericRestWrapper.create(s3BucketHost, sdkKey, connectionTimeoutMs, readTimeoutMs);


        RefreshableDataStoreProvider refreshableDataStoreProvider = new RefreshableDataStoreProvider(
                restWrapperForFlagApi,
                s3BucketRestClient,
                initialSyncCompleteLatch,
                initialSyncSuccessful,
                pollingIntervalInSeconds);

        // Try to make sure there are no errors or abandon object construction
        UnlaunchDataStore dataStore = refreshableDataStoreProvider.getNoOpDataStore();
        try {
            dataStore = refreshableDataStoreProvider.getDataStore();
        } catch (Exception e) {
            logger.error("Unable to download features and init. Make sure you're using the " +
                    "correct SDK Key. We'll retry again but this error  is usually not recoverable. ");
        }

        // This is currently not is use; we'll use this for event tracking
        long eventFlushIntervalInSeconds = eventsFlushIntervalTimeUnit.toSeconds(eventsFlushInterval);
        UnlaunchRestWrapper eventsApiRestClient =
                UnlaunchRestWrapper.create(sdkKey, host, eventApiPath, connectionTimeoutMs, readTimeoutMs);
        EventHandler eventHandler = EventHandler.createGenericEventHandler(
                "generic",
                true,
                eventsApiRestClient,
                eventFlushIntervalInSeconds,
                eventsQueueSize);

        UnlaunchRestWrapper impressionApiRestClient =
                UnlaunchRestWrapper.create(sdkKey, host, impressionApiPath, connectionTimeoutMs, readTimeoutMs);
        EventHandler impressionsEventHandler =
                EventHandler.createGenericEventHandler(
                "metrics-impressions",
                false,
                impressionApiRestClient,
                metricsFlushInterval,
                    metricsQueueSize);

        EventHandler variationsCountEventHandler = EventHandler.createCountAggregatorEventHandler(
                eventHandler,
                metricsFlushInterval,
                metricsFlushIntervalTimeUnit
        );

        return  DefaultUnlaunchClient.create(
                dataStore, eventHandler, variationsCountEventHandler, impressionsEventHandler,
                initialSyncCompleteLatch, initialSyncSuccessful,
                () -> {
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

    private void logConfigurationOptions() {
        String partiallyObfuscatedSdkKey = null;

        if (sdkKey != null && sdkKey.isEmpty()) {
            int i = sdkKey.indexOf("-");
            i = sdkKey.indexOf("-", i+1); // Second -
            partiallyObfuscatedSdkKey = sdkKey.substring(0, i+2);
        }


        logger.info("UnlaunchClient created. Configuration [sdkKey = {}-*, offlineMode = {}, pollingInterval = {} seconds,  " +
                        "connectionTimeout = {} milliseconds, readTimeout = {} milliseconds, " +
                        "metricsFlushInterval = {} seconds, metricsQueueSize = {}, eventsFlushInterval = {}, " +
                        "eventsQueueSize = {} ]",
                partiallyObfuscatedSdkKey, isOffline, pollingIntervalTimeUnit.toSeconds(pollingInterval),
                connectionTimeoutMs, readTimeoutMs, metricsFlushIntervalTimeUnit.toSeconds(metricsFlushInterval),
                metricsQueueSize, eventsFlushIntervalTimeUnit.toSeconds(eventsFlushInterval), eventsQueueSize);
    }

    /**
     * On Pre-production environments which are mostly for testing, set thresholds to their minimum values.
     */
    private void loadPreProductionDefaults() {
        if (!this.pollingIntervalUpdatedByUser) {
            this.pollingInterval = MIN_POLL_INTERVAL_IN_SECONDS;
            this.pollingIntervalTimeUnit = TimeUnit.SECONDS;
        }

        if (!this.eventsFlushIntervalUpdatedByUser) {
            this.eventsFlushInterval = MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS;
            this.eventsFlushIntervalTimeUnit = TimeUnit.SECONDS;
        }

        if (!this.metricsFlushIntervalUpdatedByUser) {
            this.metricsFlushInterval = MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS;
            this.metricsFlushIntervalTimeUnit = TimeUnit.SECONDS;
        }
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
                                "builder or set as an environment variable. " + UnlaunchConstants.getSdkKeyHelpMessage());
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

            Preconditions.checkArgument(connectionTimeoutMs >= MIN_CONNECTION_TIMEOUT_MILLIS,
                    "connectionTimeOut must be at least 1 second");
            Preconditions.checkArgument(connectionTimeoutMs < Integer.MAX_VALUE,
                    "connectionTimeOut must be less than Integer.MAX_VALUE=" + Integer.MAX_VALUE );

            Preconditions.checkArgument(readTimeoutMs >= MIN_READOUT_TIMEOUT_MILLIS,
                    "readTimeOut must be at least 1 second");
            Preconditions.checkArgument(readTimeoutMs < Integer.MAX_VALUE,
                    "readTimeOut must be less than Integer.MAX_VALUE=" + Integer.MAX_VALUE );

            Preconditions.checkArgument(metricsFlushIntervalTimeUnit != null, "metricsFlushIntervalTimeUnit cannot be null");
            Preconditions.checkArgument(metricsFlushIntervalTimeUnit.toSeconds(metricsFlushInterval) >= MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS,
                    "metricsFlushInterval() must be great than " + MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS);

            Preconditions.checkArgument(eventsFlushIntervalTimeUnit != null, "eventsFlushIntervalTimeUnit cannot be null");
            Preconditions.checkArgument(eventsFlushIntervalTimeUnit.toSeconds(eventsFlushInterval) >= MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS,
                    "eventsFlushInterval() must be great than " + MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS);

            Preconditions.checkArgument(eventsQueueSize >= MIN_EVENTS_QUEUE_SIZE, "eventsQueue must be at least 500");
            Preconditions.checkArgument(metricsQueueSize >= MIN_METRICS_QUEUE_SIZE, "eventsQueue must be at least 100");

        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException(e);
        }
    }


    private String getConfigurationAsPrintableString() {
        return "isOffline=" + isOffline +
                ", pollingInterval (seconds) =" + pollingIntervalTimeUnit.toSeconds(pollingInterval) +
                ", metricsFlushInterval (seconds) =" + metricsFlushIntervalTimeUnit.toSeconds(metricsFlushInterval) +
                ", metricsQueueSize = " + metricsQueueSize +
                ", eventsFlushInterval (seconds) = " + eventsFlushIntervalTimeUnit.toSeconds(eventsFlushInterval) +
                ", eventsQueueSize = " + eventsQueueSize +
                ", host='" + host + '\'' +
                '}';
    }
}
