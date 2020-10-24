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

    UnlaunchClientBuilder offlineMode();

    UnlaunchClientBuilder offlineModeWithLocalFeatures(String yamlFeaturesFilePath);

    UnlaunchClientBuilder pollingInterval(long interval, TimeUnit unit);

    UnlaunchClientBuilder host(String host);

    UnlaunchClientBuilder eventFlushInterval(long interval, TimeUnit unit);

    UnlaunchClientBuilder enableLazyLoading();

}
