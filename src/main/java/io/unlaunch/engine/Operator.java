package io.unlaunch.engine;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This Enum represents supported operators in Unlaunch with implementation.
 *
 * @author jawad
 */
enum Operator {

    /**
     * Returns true if userValue is equal to value. Returns
     * false if userValue is null.
     */
    EQUALS("EQ") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return equals(value, userValue, type);
        }
    },
    /**
     * Returns true if userValue is not equal to value.
     * Returns false if userValue is null.
     */
    NOT_EQUALS("NEQ") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !equals(value, userValue, type);
        }
    },
    /**
     * Returns true if userValue is greater than value.
     * Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    GREATER_THAN("GT") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return greaterThan(value, userValue, type);
        }
    },
    /**
     * Returns true if userValue is greater than or equals value
     * Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    GREATER_THAN_OR_EQUALS("GTE") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !lessThan(value, userValue, type);
        }
    },
    /**
     * Returns true if userValue is less than value.
     * Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    LESS_THAN("LT") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return lessThan(value, userValue, type);
        }
    },
    /**
     * Returns true if userValue is less than or equals to value
     * Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    LESS_THAN_OR_EQUALS("LTE") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !greaterThan(value, userValue, type);
        }
    },

    IS_ONE_OF("IN") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null) {
                return false;
            }

            Set<String> values = new HashSet<>(Arrays.asList(value.split(",")));
            return values.contains(userValue.toString());
        }
    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and starts with value
     * Returns false if userValue is null or userValue
     * is not type of UnlaunchStringValue
     */
    STARTS_WITH("SW") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return startsWith(value, userValue);
        }
    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and ends with value
     * Returns false if userValue is null or userValue
     * is not type of UnlaunchStringValue
     */
    ENDS_WITH("EW") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return endsWith(value, userValue);
        }
    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and contains value
     * Returns false if userValue is null or userValue is
     * not type of UnlaunchStringValue
     */
    CONTAINS("CON") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return contains(value, userValue);
        }

    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and does not contain value
     * Returns false if userValue is null or userValue is
     * not type of UnlaunchStringValue
     */
    NOT_CONTAINS("NCON") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !contains(value, userValue);
        }

    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and does not start with value
     * Returns false if userValue is null
     * or userValue is not type of UnlaunchStringValue
     */
    NOT_STARTS_WITH("NSW") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !startsWith(value, userValue);
        }
    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and does not ends with value
     * Returns false if userValue is null or
     * userValue is not type of UnlaunchStringValue
     */
    NOT_ENDS_WITH("NEW") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !endsWith(value, userValue);
        }
    },
    IS_PART_OF("PO") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return partOf(value, userValue);
        }
    },
    IS_NOT_PART_OF("NPO") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !partOf(value, userValue);
        }
    },
    HAS_ANY_OF("HA") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return hasAny(value, userValue);
        }
    },
    DOES_NOT_HAVE_ANY_OF("NHA") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !hasAny(value, userValue);
        }
    },
    HAS_ALL_OF("AO") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return allOf(value, userValue);
        }
    },
    DOES_NOT_HAVE_ALL_OF("NAO") {

        @Override
        public boolean apply(String value, UnlaunchValue userValue, AttributeType type) {
            return !allOf(value, userValue);
        }
    };

    private String key;

    private Operator(String key) {
        this.key = key;
    }

    /**
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     *
     * @param value
     * @param userValue
     * @param valueType
     * @return
     */
    public abstract boolean apply(String value, UnlaunchValue userValue, AttributeType valueType);

    /**
     *
     * @param key
     * @return
     */
    public static Operator findByKey(String key) {

        for (Operator operator : values()) {
            if (key.equals(operator.getKey())) {
                return operator;
            }
        }

        return null;
    }

    static boolean equals(String value, UnlaunchValue userValue, AttributeType type) {
        if (userValue == null) {
            return false;
        }
        if (AttributeType.DATE.equals(type)) {

            LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
            LocalDate userDate = userDateTime.toLocalDate();

            return userDate.equals(Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate());
        }

        if (AttributeType.SET.equals(type)) {
            Set userSetValue = (Set) userValue.get();
            Set valuesSet = new HashSet(Arrays.asList(value.split(",")));
            return valuesSet.equals(userSetValue);
        }

        return userValue.toString().equals(value);
    }

    static boolean greaterThan(String value, UnlaunchValue userValue, AttributeType type) {
        if (userValue == null || userValue instanceof UnlaunchStringValue || userValue instanceof UnlaunchBooleanValue) {
            return false;
        }

        if (AttributeType.DATE.equals(type)) {

            LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
            LocalDate userDate = userDateTime.toLocalDate();

            return userDate.isAfter(Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate());
        } else if (AttributeType.DATE_TIME.equals(type)) {
            LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();

            return userDateTime.isAfter(Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDateTime());
        }

        return Double.valueOf(userValue.toString()) > Double.valueOf(value);
    }

    static boolean lessThan(String value, UnlaunchValue userValue, AttributeType type) {
        if (userValue == null || userValue instanceof UnlaunchStringValue || userValue instanceof UnlaunchBooleanValue) {
            return false;
        }

        if (AttributeType.DATE.equals(type)) {

            LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
            LocalDate userDate = userDateTime.toLocalDate();
            LocalDate valueDate = Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate();

            return userDate.isBefore(valueDate);
        } else if (AttributeType.DATE_TIME.equals(type)) {

            LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
            LocalDateTime valueDateTime = Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDateTime();

            return userDateTime.isBefore(valueDateTime);
        }

        return Double.valueOf(userValue.toString()) < Double.valueOf(value);
    }

    static boolean startsWith(String value, UnlaunchValue userValue) {
        if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
            return false;
        }

        return userValue.toString().startsWith(value);
    }

    static boolean endsWith(String value, UnlaunchValue userValue) {
        if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
            return false;
        }

        return userValue.toString().endsWith(value);
    }

    static boolean contains(String value, UnlaunchValue userValue) {
        if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
            return false;
        }

        return userValue.toString().contains(value);
    }

    static boolean partOf(String value, UnlaunchValue userValue) {
        if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
            return false;
        }

        Set userSet = (Set) userValue.get();
        List<String> values = Arrays.asList(value.split(","));

        Set<String> intersection = values.stream()
                .distinct()
                .filter(userSet::contains)
                .collect(Collectors.toSet());

        return userSet.equals(intersection);
    }

    static boolean hasAny(String value, UnlaunchValue userValue) {
        if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
            return false;
        }

        Set userSet = (Set) userValue.get();
        List<String> values = Arrays.asList(value.split(","));

        return values.stream().anyMatch(item -> userSet.contains(item));
    }

    static boolean allOf(String value, UnlaunchValue userValue) {
        if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
            return false;
        }

        Set userSet = (Set) userValue.get();
        List<String> values = Arrays.asList(value.split(","));

        return values.stream().allMatch(item -> userSet.contains(item));
    }
}
