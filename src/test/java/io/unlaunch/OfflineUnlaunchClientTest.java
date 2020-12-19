package io.unlaunch;

import io.unlaunch.utils.UnlaunchConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author umermansoor
 */
public class OfflineUnlaunchClientTest {

    @Test
    public void testInvalidApiKeyAllowsOfflineClientConstructionAndDefaultValuesWork() {
        UnlaunchClient offlineClient = UnlaunchClient.builder().
                sdkKey(UUID.randomUUID().toString()).
                offlineMode().
                build();

        String variation = offlineClient.getVariation(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, variation);

        offlineClient.shutdown();
    }

    @Test
    public void testNoApiKeyAllowsOfflineClientConstructionAndDefaultValuesWork() {
        UnlaunchClient offlineClient = UnlaunchClient.builder().
                offlineMode().
                build();

        String variation = offlineClient.getVariation(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, variation);

        offlineClient.shutdown();
    }

    @Test
    public void testNoApiKeyAllowsOfflineClientConstructionAndEmptyConfigIsReturned() {
        UnlaunchClient offlineClient = UnlaunchClient.builder().
                offlineMode().
                build();

        UnlaunchFeature f = offlineClient.getFeature(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE, f.getVariation());
        Assert.assertEquals(0, f.getVariationConfigAsMap().size());
        Assert.assertEquals(100, f.getVariationConfig().getInt("price", 100));

        offlineClient.shutdown();
    }

    @Test
    public void testLoadFlagsFromYamlFile() {
        String filePath = "src/test/resources/flags.yaml";
        UnlaunchClient offlineClient = UnlaunchClient.builder().offlineModeWithLocalFeatures(filePath).build();

        Assert.assertEquals("on", offlineClient.getVariation("featureA", UUID.randomUUID().toString()));
        Assert.assertEquals("off", offlineClient.getVariation("featureB", UUID.randomUUID().toString()));

        // random flag (unknown) should return none
        Assert.assertEquals(UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE,
                offlineClient.getVariation(UUID.randomUUID().toString(),
                UUID.randomUUID().toString()));
    }

    @Test
    public void testLoadFlagsFromYamlFileWithConfig() {
        String filePath = "src/test/resources/flags.yaml";
        UnlaunchClient offlineClient = UnlaunchClient.builder().offlineModeWithLocalFeatures(filePath).build();

        Map<String, String> map = offlineClient.getFeature("featureA", UUID.randomUUID().toString()).getVariationConfigAsMap();
        Assert.assertEquals("usa", map.get("country"));

        boolean is_paid =
        offlineClient.getFeature("featureA", UUID.randomUUID().toString()).getVariationConfig().getBoolean("is_paid", false);
        Assert.assertEquals(true, is_paid);
    }
}
