package io.unlaunch;

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
    public void testInvalidEventFlushInterval() {
        UnlaunchClient.builder().
                sdkKey("sdkKey").
                eventFlushInterval(-1, TimeUnit.SECONDS).
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
