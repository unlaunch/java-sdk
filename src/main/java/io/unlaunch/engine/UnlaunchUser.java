package io.unlaunch.engine;

import io.unlaunch.UnlaunchAttribute;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *
 * @author umermansoor
 */
public class UnlaunchUser {
	
    private final String id;
    private final boolean anon;
    private Map<String, UnlaunchValue> attributes = new ConcurrentHashMap<>();

    UnlaunchUser(String id) {
       this(id, false);
    }

    UnlaunchUser(String id, boolean anon) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("The argument `id` must be a valid String.");
        }

        this.id = id;
        this.anon = anon;
    }

    public static UnlaunchUser create(String id) {
        return new UnlaunchUser(id);
    }

    public static UnlaunchUser createWithAttributes(String id,  UnlaunchAttribute... attributes) {
        Map<String, Object> map = new HashMap<>();

        if (attributes != null) {
            Stream<UnlaunchAttribute> s = Stream.of(attributes);
            s.forEach(a -> {
                map.put(a.getKey(), a.getValue());
            });
        }

        return new UnlaunchUser(id, false, map);
    }

    public static UnlaunchUser createAnonymous() {
        String id = "anon -" + UUID.randomUUID();
        return new UnlaunchUser(id, true);
    }


    
    /**
     * id must be a non-null and non-empty String. attributes map should
     * contains the exact same keys defined in the rule conditions. For each
     * attribute key user specific value is provided.
     *
     * @param id
     * @param attributes
     */
     UnlaunchUser(String id, boolean anon, Map<String, Object> attributes) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("The argument `id` must be a valid String.");
        }

        if (attributes == null) {
            throw new IllegalArgumentException("The argument `id` must not be null");
        }

        this.id = id;
        this.anon = anon;
        resetAndSetAttributesMap(attributes);

    }

    private void resetAndSetAttributesMap(Map<String, Object> map) {
        this.clearAllAttributes();
        map.forEach((k,v) -> {
            try {
                UnlaunchValue<?> ula = getUnlaunchAttributeFromJavaObject(v);
                this.attributes.put(k, ula);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported value type for key `" + k + "`.", e);
            }
        });
    }

    /**
     * Returns a map of user attributes with values.
     *
     * @return map of user attributes
     */
    public Map<String, UnlaunchValue> getAllAttributes() {
        return new HashMap<>(attributes);
    }

    public void setAttributes(Map<String, Object> attributes) {
        resetAndSetAttributesMap(attributes);
    }

    /**
     * Key should be non-null and non-empty string. key should match to the
     * attribute name used in the rule conditions.
     *
     * @param key attribute key
     * @param value attribute value
     */
    public void putAttribute(String key, Object value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be null or empty");
        }

        UnlaunchValue v = getUnlaunchAttributeFromJavaObject(value);

        attributes.put(key, v);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if attributes map do not have the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if attributes do not contain this key
     * @throws NullPointerException if the specified key is null or is
     *         empty.
     */
    public Object getAttribute(String key) {

        if (key == null || key.isEmpty()) {
            throw new NullPointerException("key cannot be null or empty");
        }

        if (this.attributes.containsKey(key)) {
            return this.attributes.get(key).get();
        } else {
            return null;
        }
    }

    private UnlaunchValue<?> getUnlaunchAttributeFromJavaObject(Object value) {
        if (!(value instanceof Number) && !(value instanceof String) && !(value instanceof Boolean)
                && !(value instanceof LocalDateTime) && !(value instanceof LocalDate) && !(value instanceof Date)
                && !(value instanceof Set)) {
            throw new IllegalArgumentException("value `" + value + "` must be of type String, Number, Boolean, LocalDate, LocalDateTime, Date or Set");
        }

        if (value instanceof Number) {
            return new UnlaunchNumberValue((Number)value);
        } else if (value instanceof String) {
            return new UnlaunchStringValue(String.valueOf(value));
        } else if (value instanceof Boolean) {
            return new UnlaunchBooleanValue((Boolean) value);
        } else if (value instanceof LocalDateTime) {
            return new UnlaunchDateTimeValue((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            LocalDate d = (LocalDate) value;
            return new UnlaunchDateTimeValue(d.atStartOfDay());
        } else if (value instanceof LocalDateTime) {
            LocalDateTime ld = (LocalDateTime) value;
            return new UnlaunchDateTimeValue(ld);
        } else if (value instanceof LocalDateTime) {
            LocalDateTime d = (LocalDateTime) value;
            return new UnlaunchDateTimeValue(d);
        } else if (value instanceof Set) {
            Set d = (Set) value;
            return new UnlaunchSetValue(d);
        }

        return null;
    }

    public void clearAllAttributes() {
        this.attributes.clear();
    }

    /**
     * Returns id of the UnlaunchUser
     *
     * @return id of the user
     */
    public String getId() {
        return id;
    }


    /**
     * Returns true if the given UnlaunchUser object has same id as this object
     * and the user attributes map has the same mappings as this map. key should
     * match to the attribute name used in the rule conditions.
     *
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnlaunchUser user = (UnlaunchUser) o;
        return getId().equals(user.getId()) &&
                getAllAttributes().equals(user.getAllAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAllAttributes());
    }
}
