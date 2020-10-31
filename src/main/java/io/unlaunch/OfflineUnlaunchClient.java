package io.unlaunch;

import com.google.common.base.Preconditions;
import io.unlaunch.utils.UnlaunchConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *  Offline Unlaunch client which doesn't connect to the Unlauch server over network to download feature flags, nor
 *  does it sends any events or metrics data. By default, it will return default variation for all feature flags. Users
 *  can specify a YAML file containing feature flags and default variations and it will return those when the feature is
 *  evaluated.
 *
 * @author umermansoor
 */
public class OfflineUnlaunchClient implements UnlaunchClient {

    private final Map<String, UnlaunchFeature> dataStore = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(OfflineUnlaunchClient.class);

    private final String yamlFilePath;

    public OfflineUnlaunchClient() {
        this.yamlFilePath = null;
    }

    public OfflineUnlaunchClient(String yamlFilePath) {
        this.yamlFilePath = yamlFilePath;
        loadFromFile();
    }

    @Override
    public AccountDetails accountDetails() {
        return new AccountDetails("client_is_in_offline_mode", "offine_mode", -1);
    }

    @Override
    public String getVariation(String flagKey, String identity) {
        return getVariation(flagKey, identity, null);
    }

    @Override
    public String getVariation(String flagKey, String identity, UnlaunchAttribute... attributes) {
        return getFeature(flagKey, identity, null).getVariationKey();
    }

    @Override
    public UnlaunchFeature getFeature(String flagKey, String identity) {
        return getFeature(flagKey, identity, null);
    }

    @Override
    public UnlaunchFeature getFeature(String flagKey, String identity, UnlaunchAttribute... attributes) {
        return evaluate(flagKey, identity, attributes);
    }

    @Override
    public void awaitUntilReady(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        // Do nothing
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void shutdown() {
        // do nothing
    }

    private UnlaunchFeature evaluate(String flag, String identity, UnlaunchAttribute ... attributes) {
        Preconditions.checkArgument(flag != null && !flag.isEmpty(), "flag key must not be empty or null");

        if (dataStore.containsKey(flag)) {
            return dataStore.get(flag);
        } else {
            return UnlaunchFeature.create(flag,
                    UnlaunchConstants.FLAG_DEFAULT_RETURN_TYPE,
                    new HashMap<>(1),
                    "Client is initialized in Offline Mode. Returning 'control' variation for all flags.");
        }
    }

    private void loadFromFile() {
        Yaml yaml = new Yaml();

        try {
            List<Map<String, Map<String, Object>>> features = (List<Map<String, Map<String, Object>>>) yaml.load(new FileReader(yamlFilePath));

            for (Map<String, Map<String, Object>> feature : features) {
                Map.Entry<String, Map<String, Object>> featureSet = feature.entrySet().iterator().next();
                Map<String, String> properties = (Map<String, String>) featureSet.getValue().get("config");
                UnlaunchFeature f = UnlaunchFeature.create(featureSet.getKey(), (String) featureSet.getValue().get("variation"), properties);
                dataStore.put(featureSet.getKey(), f);
            }
        } catch (Exception e) {
            logger.error("Features file [" + this.yamlFilePath + "] was not found or there was an error parsing it. " +
                    "The client will return default variations for all evaluations. If you wish to specify variations" +
                    " to be returned, please provide a YAML file containing features. For more information, " +
                    "see this: https://blog.unlaunch.io/2020-08-30-offline-mode");
        }
    }

}
