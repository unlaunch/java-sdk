package io.unlaunch;

import java.util.concurrent.TimeUnit;

/**
 * This builder class contains configuration required by Unlaunch client.
 *
 *  <p>Implementations of this interface are mutable hence not thread-safe.</p>
 *
 * @author umermansoor
 */
public interface UnlaunchClientBuilder {
    UnlaunchClient build();

    UnlaunchClientBuilder sdkKey(String sdkKey);

    /**
     * This is intended for testing purposes. Starts the client is offline mode. All flag evaluations will return
     * <pre>control</pre> variation and no data is sent to server.
     * <p>For more information <a href="https://docs.unlaunch.io/docs/sdks/java-sdk#offline-mode">read this</a>.</p>
     * @return  {@link UnlaunchClientBuilder} builder
     */
    UnlaunchClientBuilder offlineMode();

    /**
     * This is intended for testing, including unit testing. This allows you to pass a YAML file containing feature
     * flags and the variations to return when they are evaluated. You can also control dynamic configuration and
     * specify which values to return.
     * <p>For more information and a template,<a href="https://docs.unlaunch.io/docs/sdks/java-sdk#offline-mode">read this</a></p>
     * @param yamlFeaturesFilePath
     * @return builder
     */
    UnlaunchClientBuilder offlineModeWithLocalFeatures(String yamlFeaturesFilePath);

    /**
     * This is for controlling how often the SDK download flags from the servers if the data has changed.
     * <p>The default interval is 60 seconds for production, and 20 seconds for non-production environments.</p>
     * @param interval interval
     * @param unit time unit
     * @return {@link UnlaunchClientBuilder}
     */
    UnlaunchClientBuilder pollingInterval(long interval, TimeUnit unit);

    /**
     * Sets the default connect timeout for HTTP connections.
     * <p>This is the time to establish the connection with remote Unlaunch servers.</p>
     * <p>The default value is 10 seconds. The minimum value allowed is 1 second.</p>
     * @param timeout time out
     * @param unit time unit
     * @return  {@link UnlaunchClientBuilder}
     */
    UnlaunchClientBuilder connectionTimeout(long timeout, TimeUnit unit);

    /**
     * Sets the default read timeout for HTTP connections.
     * <p> Specifies the time to wait for data to arrive after establishing the connection.</p>
     *  <p>The default value is 10 seconds. The minimum value allowed is 1 second.</p>
     * @param timeout time out
     * @param unit time out
     * @return  {@link UnlaunchClientBuilder}
     */
    UnlaunchClientBuilder readTimeout(long timeout, TimeUnit unit);

    /**
     * Unlaunch server to connect to for downloading feature flags, submitting events, etc.
     * <p>Use this if you are running Unlaunch backend service on-premise or are enterprise customer. The default
     * value is https://api.unlaunch.io</p>
     * @param host - Unlaunch backend service to connect to
     * @return  {@link UnlaunchClientBuilder}
     */
    UnlaunchClientBuilder host(String host);

    /**
     * The SDK periodically sends events like metrics and diagnostics data to our servers. This controls how
     * frequently this data is sent.
     * <p>When the SDK is shutdown using the {@link UnlaunchClient#shutdown()}, all buffered data is automatically
     * sent.</p>
     * <p>The default value is 30 seconds for production and 10 seconds for non-production environments.</p>
     * @param interval time interval
     * @param unit time out
     * @return {@link UnlaunchClientBuilder}
     */
    UnlaunchClientBuilder metricsFlushInterval(long interval, TimeUnit unit);

    /**
     * This controls how frequently tracking events are sent to the server.
     * <p>When the SDK is shutdown using the {@link UnlaunchClient#shutdown()}, all buffered data is automatically
     * sent.</p>
     * <p>The default value is 60 seconds for production and 15 seconds for non-production environments.</p>
     * @param interval time interval
     * @param unit time out
     * @return {@link UnlaunchClientBuilder}
     */
    UnlaunchClientBuilder eventsFlushInterval(long interval, TimeUnit unit);

    /**
     * The maximum number of events to keep in memory.
     * <p>Events are sent to the server when either the  queue size  or events flush interval is reached, whichever
     * comes first.  </p>
     * @param maxQueueSize maximum queue size
     * @return  {@link UnlaunchClientBuilder}
     */
     UnlaunchClientBuilder eventsQueueSize(int maxQueueSize);

    /**
     * The maximum number of metrics (impressions) to keep in memory.
     * <p>Metrics are sent to the server when either the  queue size  or the flush interval is reached, whichever
     * comes first. </p>
     * @param maxQueueSize maximum queue size
     * @return  {@link UnlaunchClientBuilder}
     */
    UnlaunchClientBuilder metricsQueueSize(int maxQueueSize);

    UnlaunchClientBuilder enableLazyLoading();

}
