package io.unlaunch.engine;

import java.util.Map;
import java.util.Set;

/**
 * A class representing feature flag variable.
 * @author jawad
 */
public final class Variation {
    private String key;
    private String name;
    private long rolloutPercentage;
    private Map<String, String> properties;
    private String allowList;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRolloutPercentage() {
        return rolloutPercentage;
    }

    public void setRolloutPercentage(long rolloutPercentage) {
        this.rolloutPercentage = rolloutPercentage;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getAllowList() {
        return allowList;
    }

    public void setAllowList(String allowList) {
        this.allowList = allowList;
    }
}
