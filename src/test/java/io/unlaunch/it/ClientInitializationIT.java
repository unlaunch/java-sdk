package io.unlaunch.it;

import io.unlaunch.UnlaunchClient;
import io.unlaunch.utils.UnlaunchConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * Various integration tests for client initialization
 *
 * @author umermansoor
 */
public class ClientInitializationIT {

    @Test
    public void testInvalidAPIKeyAllowsClientConstructionAndDefaultValuesWork() {
        UnlaunchClient clientWithIncorrectApiKey = UnlaunchClient.create(UUID.randomUUID().toString());

        String variation = clientWithIncorrectApiKey.getVariation("flagKey", "userId123");
        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, variation);

        clientWithIncorrectApiKey.shutdown();
    }

}
