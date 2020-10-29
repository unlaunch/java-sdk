package io.unlaunch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author umermansoor
 */
public class UnlaunchFeature {

    private final String flagKey;
    private final String variationKey;
    private final Map<String, String> properties;
    private final String evaluationReason;

    private UnlaunchFeature(String flagKey, String variationKey, String evaluationReason, Map<String, String> properties) {
        this.flagKey = flagKey;
        this.variationKey = variationKey;
        this.evaluationReason = evaluationReason;
        this.properties = properties;
    }

    public String getFlagKey() {
        return this.flagKey;
    }

    public String getVariationKey() {
        return variationKey;
    }

    public Map<String, String> getVariationConfigAsMap() {
        return new HashMap<>(properties);
    }

    public UnlaunchDynamicConfig getVariationConfig() {
        return new DefaultUnlaunchDynamicConfig(Collections.unmodifiableMap(properties));
    }

    public String getEvaluationReason() {
        return evaluationReason;
    }

    public static UnlaunchFeature create(String flagKey, String variationKey, Map<String, String> properties, String evaluationReason) {
        return new UnlaunchFeature(flagKey, variationKey, evaluationReason, properties);
    }

    public static UnlaunchFeature create(String flagKey, String variationKey, Map<String, String> properties) {
        return new UnlaunchFeature(flagKey, variationKey, "", properties);
    }


}
