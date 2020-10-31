package io.unlaunch;

import io.unlaunch.exceptions.UnlaunchConversionException;

/**
 * The main interface for accessing Unlaunch Key-Value configuration data in a read-only fashion
 *
 * @author umermansoor
 */
public interface UnlaunchDynamicConfig {

    /**
     * Checks if the configuration contains the specified key.
     *
     * @param key the key whose presence in this configuration is to be tested
     * @return {@code true} if the configuration contains a value for this key, {@code false} otherwise
     */
    boolean containsKey(String key);

    /**
     * Gets a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     * @throws UnlaunchConversionException is thrown if the key maps to an object that is not a Boolean.
     */
    boolean getBoolean(String key);
    /**
     * Gets a boolean associated with the given configuration key. If the key doesn't map to an existing object, the
     * default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Gets a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     * @throws UnlaunchConversionException is thrown if the key maps to an object that is not a Double
     * @throws NullPointerException if the specified key is null
     */
    double getDouble(String key);

    /**
     * Gets a double associated with the given configuration key. If the key doesn't map to an existing object, the
     * default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double.
     */
    double getDouble(String key, double defaultValue);

    /**
     * Gets a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated float.
     * @throws UnlaunchConversionException is thrown if the key maps to an object that is not a Float.
     * @throws NullPointerException if the specified key is null
     */
    float getFloat(String key);

    /**
     * Gets a float associated with the given configuration key. If the key doesn't map to an existing object, the
     * default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float.
     */
    float getFloat(String key, float defaultValue);


    /**
     * Gets a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     * @throws UnlaunchConversionException is thrown if the key maps to an object that is not a Integer.
     * @throws NullPointerException if the specified key is null
     */
    int getInt(String key);

    /**
     * Gets a int associated with the given configuration key. If the key doesn't map to an existing object, the
     * default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int.
     */
    int getInt(String key, int defaultValue);

    /**
     * Gets a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated long.
     * @throws UnlaunchConversionException is thrown if the key maps to an object that is not a Long.
     * @throws NullPointerException if the specified key is null
     */
    long getLong(String key);

    /**
     * Gets a long associated with the given configuration key. If the key doesn't map to an existing object, the
     * default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long.
     */
    long getLong(String key, long defaultValue);

    /**
     * Gets a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     *
     * @throws UnlaunchConversionException is thrown if the key maps to an object that is not a String.
     * @throws NullPointerException if the specified key is null
     */
    String getString(String key);

    /**
     * Gets a string associated with the given configuration key. If the key doesn't map to an existing object, the
     * default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found and has valid format, default value otherwise.
     */
    String getString(String key, String defaultValue);

    /**
     * Checks if the configuration is empty.
     *
     * @return {@code true} if the configuration contains no property, {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the number of keys stored in this configuration.
     *
     * @return the number of keys stored in this configuration
     */
    int size();

}
