package io.unlaunch.engine;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A class representing an Unlaunch feature flag.
 *
 * @author jawad
 */
public class FeatureFlag {
    private final String key;
    private final String name;
    private final List<Variation> variations;
    private final Map<FeatureFlag, Variation> prerequisiteFlags;
    private final List<Rule> rules;
    private final boolean enabled;
    private final Variation offVariation;
    private final Rule defaultRule;
    private final String expectedVariationKey;
    private final String type;

    public FeatureFlag(String key, String name, List<Variation> variations, Map<FeatureFlag, Variation> prerequisiteFlags, List<Rule> rules, boolean enabled,
                       Variation offVariation, Rule defaultRule, String expectedVariationKey, String type) {
        this.key = key;
        this.name = name;
        this.variations = variations;
        this.prerequisiteFlags = prerequisiteFlags;
        this.rules = rules;
        this.enabled = enabled;
        this.offVariation = offVariation;
        this.defaultRule = defaultRule;
        this.expectedVariationKey = expectedVariationKey;
        this.type = type;
//        this.flagSubType = variationKeyType;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<Variation> getVariations() {
        return variations == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(variations);
    }

    
    public List<Rule> getRules() {
        return rules == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(rules);
    }

    public Map<FeatureFlag, Variation> getPrerequisiteFlags() {
        return prerequisiteFlags == null ? null : Collections.unmodifiableMap(prerequisiteFlags);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Variation getOffVariation() {
        return offVariation;
    }

    public Rule getDefaultRule() {
        return defaultRule;
    }

    public String getExpectedVariationKey() {
        return expectedVariationKey;
    }

    public String getType() {
        return type;
    }



}
