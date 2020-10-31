package io.unlaunch;

import java.util.HashMap;
import java.util.Map;

final class DefaultUnlaunchDynamicConfig implements UnlaunchDynamicConfig {
    private final Map<String, String> configMap;

    DefaultUnlaunchDynamicConfig(Map<String, String> configMap) {
        if (configMap == null) {
            this.configMap = new HashMap<>(1);
        } else {
            this.configMap = configMap;
        }
    }

    @Override
    public boolean containsKey(String key) {
        return configMap.containsKey(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.valueOf(configMap.get(key));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        if (configMap.containsKey(key)) {
            return getBoolean(key);
        } else {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String key) {
        return Double.valueOf(configMap.get(key)).doubleValue();
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        try {
            return getDouble(key);
        } catch (NumberFormatException | NullPointerException nfe) {
            return defaultValue;
        }
    }

    @Override
    public float getFloat(String key) {
        return Float.valueOf(configMap.get(key)).floatValue();
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        try {
            return getFloat(key);
        } catch (NumberFormatException | NullPointerException nfe) {
            return defaultValue;
        }
    }

    @Override
    public int getInt(String key) {
        return Integer.valueOf(configMap.get(key)).intValue();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            return getInt(key);
        } catch (NumberFormatException | NullPointerException nfe) {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key) {
        return Long.valueOf(configMap.get(key)).longValue();
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            return getLong(key);
        } catch (NumberFormatException | NullPointerException nfe) {
            return defaultValue;
        }
    }

    @Override
    public String getString(String key) {
        return configMap.get(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        if (configMap.containsKey(key) && configMap.get(key) != null) {
            return configMap.get(key);
        } else {
            return defaultValue;
        }
    }

    @Override
    public boolean isEmpty() {
        return configMap.isEmpty();
    }

    @Override
    public int size() {
        return configMap.size();
    }
}
