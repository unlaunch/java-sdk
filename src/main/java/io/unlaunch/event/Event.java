package io.unlaunch.event;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UnLaunch Event class.
 *
 * @author umer
 */
public class Event {
    private long createdTime; // milliseconds
    private String type;
    private String key;
    private final Object value;
    private Map<String, Object> properties;
    private String sdk = "Java";
    private final String sdkVersion = "1.0";
    private final String secondaryKey;

    final static Object EMPTY_OBJECT = new Object();

    public Event(String type, String key) {
        this(type, key, "");
    }

    public Event(String type, String key, String secondaryKey) {
        this(type, key, secondaryKey, EMPTY_OBJECT);
    }

    public Event(String type, String key, String secondaryKey, Object value) {
        this.type = type;
        this.key = key;
        this.secondaryKey = secondaryKey;
        this.value = value;
        this.properties = new ConcurrentHashMap<>();
        this.createdTime = System.currentTimeMillis();
    }

    public void addProperty(String key, Object value) {
        if (!(value instanceof Number) && !(value instanceof String) && !(value instanceof Boolean)) {
            throw new IllegalArgumentException("value must be of type String, Number or Boolean");
        }

        if (key == null && key.isEmpty()) {
            throw new IllegalArgumentException("Properties key must not be null or empty");
        }

        properties.put(key, value);
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public Map<String, Object> getProperties() {
        return new ConcurrentHashMap<>(properties);
    }

    public String getSdk() {
        return sdk;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }


    @Override
    public String toString() {
        return "Event{" +
                "timestamp=" + createdTime +
                ", type='" + type + '\'' +
                ", Key='" + key + '\'' +
                ", secondaryKey='" + secondaryKey + '\'' +
                ", # of properties=" + properties.size() +
                ", sdk='" + sdk + '\'' +
                ", sdkVersion='" + sdkVersion + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return getType().equals(event.getType()) &&
                getKey().equals(event.getKey()) &&
                Objects.equals(value, event.value) &&
                Objects.equals(getSdkVersion(), event.getSdkVersion()) &&
                Objects.equals(getSecondaryKey(), event.getSecondaryKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getKey(), value, getSdkVersion(), getSecondaryKey());
    }
}
