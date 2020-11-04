package io.unlaunch;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author umermansoor
 */
public class DefaultUnlaunchClientBuilderTest {

    @Test(expected = IllegalStateException.class)
    public void testInvalidPollingInterval() {
        UnlaunchClient.builder().
                sdkKey("sdkKey").
                pollingInterval(-1, TimeUnit.SECONDS).
                build();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidMetricsFlushInterval() {
        int aggressiveFlushInterval = 1;
        Assert.assertTrue(aggressiveFlushInterval < DefaultUnlaunchClientBuilder.MIN_METRICS_FLUSH_INTERVAL_IN_SECONDS);

        UnlaunchClient.builder().
                sdkKey("sdkKey").
                metricsFlushInterval(1, TimeUnit.SECONDS).
                build();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidEventsFlushInterval() {
        int aggressiveFlushInterval = 1;
        Assert.assertTrue(aggressiveFlushInterval < DefaultUnlaunchClientBuilder.MIN_EVENTS_FLUSH_INTERVAL_IN_SECONDS);

        UnlaunchClient.builder().
                sdkKey("sdkKey").
                eventsFlushInterval(aggressiveFlushInterval, TimeUnit.SECONDS).
                build();
    }
    @Test(expected = IllegalStateException.class)
    public void testInvalidQueueSize() {
        int invalidQueueSize = 1;
        Assert.assertTrue(invalidQueueSize < DefaultUnlaunchClientBuilder.MIN_EVENTS_QUEUE_SIZE);

        UnlaunchClient.builder().
                sdkKey("sdkKey").
                eventsQueueSize(1).
                build();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidNegativeConnectionTimeOut() {
        UnlaunchClient.builder().
                sdkKey("sdkKey").
                connectionTimeout(-1, TimeUnit.SECONDS).
                build();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidNegativeReadTimeOut() {
        UnlaunchClient.builder().
                sdkKey("sdkKey").
                readTimeout(-1, TimeUnit.SECONDS).
                build();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidMaxIntConnectionTimeOut() {
        UnlaunchClient.builder().
                sdkKey("sdkKey").
                connectionTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS).
                build();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidMaxIntReadTimeOut() {
        UnlaunchClient.builder().
                sdkKey("sdkKey").
                readTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS).
                build();
    }

    @Test(expected = IllegalStateException.class)
    public void testNoSdkKeyProvidedAndNoEnvironmentVariableSet() {
        UnlaunchClient.builder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void testWithInvalidHostName() {
        UnlaunchClient.builder().host("").build();
    }

}
