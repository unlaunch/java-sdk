package io.unlaunch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p> This class wraps the result of a feature flag evaluation. It makes all evaluation data available in one class,
 * including:</p>
 *  <ul>
 *      <li>Variation: This is the variation (key) that you defined e.g. "on", "off", etc. </li>
 *      <li>Configuration: Key-Value or JSON Configuration that you attached to your variation.</li>
 *      <li>Evaluation Reason: Flag evaluation reason on why certain variation ws returned. Useful for debugging.</li>
 *  </ul>
 *
 *
 *  <p>Use this class if you want to retrieve Configuration attached to variation or get the Evaluation Reason. If
 *  you just want to retrieve variation, you can also use the (shortcut)
 *  {@link UnlaunchClient#getVariation(String, String)} method.</p>
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

    /**
     * Returns the key of the flag this was evaluated for.
     *
     * @return flag key for this evaluation
     */
    public String getFlag() {
        return this.flagKey;
    }

    /**
     * Evaluates and returns the variation (variation key) for this feature. Variations are defined using the Unlaunch
     * Console at <a href="https://app.unlaunch.io">https://app.unlaunch.io</a>.
     * <p>This method returns "control" if:</p>
     *  <ol>
     *      <li> The flag was not found.</li>
     *      <li> There was an exception evaluation the feature flag.</li>
     *      <li> The flag was archived.</li>
     *  </ol>
     * <p>This method doesn't throw any exceptions nor does it return <code>null</code> value</p>
     *
     * @return the evaluated variation or  "control" if there was an error.
     */
    public String getVariation() {
        return variationKey;
    }

    /**
     * Returns Key-Value configuration as a {@link Map} that you defined in the Unlaunch Console using the
     * "Configuration" tab on the feature flag details page.
     *
     * @return  {@link Map} of Key-Value configuration for the evaluated variation.
     */
    public Map<String, String> getVariationConfigAsMap() {
        return new HashMap<>(properties);
    }

    /**
     * Returns  {@link UnlaunchDynamicConfig} object which contains configuration (Key-Value) that you attached to
     * the variation in the Unlaunch Console. You can use either this or the
     * {@link UnlaunchFeature#getVariationConfigAsMap()} method to get Configuration depending on what you prefer.
     * @return  {@link UnlaunchDynamicConfig} for configuration (Key-Value)
     */
    public UnlaunchDynamicConfig getVariationConfig() {
        return new DefaultUnlaunchDynamicConfig(Collections.unmodifiableMap(properties));
    }

    /**
     * <p>Provides a detailed evaluation reason as to why the variation (result of
     * {@link UnlaunchFeature#getVariation()}) was chosen. This is useful for debugging and troubleshooting when you
     * are wondering why you're not getting the variation that you were expecting.</p> For example, if you were expecting
     * the "on" variation, but getting "off" variation, check why "off" is being selected using this method.
     *
     *<p>Tip: You can also check evaluation reason and view evaluations in real-time using the "Live Tail" feature of
     * the Unlaunch Console. To access the Unlaunch Console, visit
     *  <a href="https://app.unlaunch.io">https://app.unlaunch.io</a>.</p>
     *
     * @return evaluation reason (as String) explaining why the variation was selected
     */
    public String getEvaluationReason() {
        return evaluationReason;
    }

    public static UnlaunchFeature create(String flagKey, String variationKey, Map<String, String> properties,
                                    String evaluationReason) {
        return new UnlaunchFeature(flagKey, variationKey, evaluationReason, properties);
    }

    public static UnlaunchFeature create(String flagKey, String variationKey, Map<String, String> properties) {
        return new UnlaunchFeature(flagKey, variationKey, "", properties);
    }


}
