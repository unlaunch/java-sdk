package io.unlaunch.it;

import io.unlaunch.UnlaunchClient;
import io.unlaunch.utils.UnlaunchConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Various integration tests for client initialization
 *
 * @author umermansoor
 */
public class ClientInitializationIT {

    @Test
    public void testInvalidAPIKeyAllowsClientConstructionAndDefaultValuesWork() {
        UnlaunchClient clientWithIncorrectApiKey = UnlaunchClient.create("prod-server-" + UUID.randomUUID().toString());

        // awaitUntilReady should return if SDK Key is rejected
        try {
            clientWithIncorrectApiKey.awaitUntilReady(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            Assert.fail();
        }

        String variation = clientWithIncorrectApiKey.getVariation("flagKey", "userId123");
        Assert.assertEquals(false, clientWithIncorrectApiKey.isReady());
        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, variation);

        clientWithIncorrectApiKey.shutdown();
    }

}
