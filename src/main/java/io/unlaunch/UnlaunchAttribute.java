package io.unlaunch;

/**
 * @author umermansoor
 */
public class UnlaunchAttribute {

    private final String key;
    private final Object value;

    UnlaunchAttribute(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    private static UnlaunchAttribute create(String key, Object value) {
        if (key == null || key.isEmpty())  {
            throw new IllegalArgumentException("key argument must not be null or empty");
        }

        return new UnlaunchAttribute(key, value);

    }
    public static UnlaunchAttribute newString(String key, String value) {
        if (value == null || value.isEmpty())  {
            throw new IllegalArgumentException("value argument must not be null or empty");
        }
        return create(key, value);
    }

    public static UnlaunchAttribute  newNumber(String key, Number value) {
        return create(key, value);
    }

    public static UnlaunchAttribute newBoolean(String key, boolean value) {
        return create(key, value);
    }

    public static UnlaunchAttribute newDateTime(String key, long millisecondsSinceEpoch) {
        return create(key, millisecondsSinceEpoch);
    }

    public static UnlaunchAttribute newDate(String key, long millisecondsSinceEpoch) {
        return create(key, millisecondsSinceEpoch);
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
