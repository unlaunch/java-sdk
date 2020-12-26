package io.unlaunch.engine;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
     * Returns true if userValue is equal to any of the value in list. Returns
     * false if userValue is null.
     */
    EQUALS("EQ") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null) {
                return false;
            }
            if (AttributeType.DATE.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
                LocalDate userDate = userDateTime.toLocalDate();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate()).anyMatch(ruleDate -> userDate.equals(ruleDate));
            }

            if (AttributeType.SET.equals(type)) {
                Set userSetValue = (Set) userValue.get();
                Set valuesSet = new HashSet(values);
                return valuesSet.equals(userSetValue);
            }

            return values.stream().anyMatch(value -> value.equals(userValue.toString()));
        }
    },
    /**
     * Returns true if userValue is not equal to any of the value in list.
     * Returns false if userValue is null.
     */
    NOT_EQUALS("NEQ") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null) {
                return false;
            }

            if (AttributeType.DATE.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
                LocalDate userDate = userDateTime.toLocalDate();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate()).anyMatch(ruleDate -> !userDate.equals(ruleDate));

            }

            if (AttributeType.SET.equals(type)) {
                Set userSetValue = (Set) userValue.get();
                Set valuesSet = new HashSet(values);
                return !valuesSet.equals(userSetValue);
            }

            return values.stream().noneMatch(value -> value.equals(userValue.toString()));
        }
    },
    /**
     * Returns true if userValue is greater than to any of the value in list.
     * Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    GREATER_THAN("GT") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || userValue instanceof UnlaunchStringValue || userValue instanceof UnlaunchBooleanValue) {
                return false;
            }

            if (AttributeType.DATE.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
                LocalDate userDate = userDateTime.toLocalDate();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate()).anyMatch(ruleDate -> userDate.isAfter(ruleDate));

            } else if (AttributeType.DATE_TIME.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDateTime()).anyMatch(ruleDateTime -> userDateTime.isAfter(ruleDateTime));
//                return values.stream().mapToLong(value -> Long.valueOf(value)).anyMatch(longValue -> userDateTime.getEpoc() > longValue);
            }

            return values.stream().mapToDouble(value -> Double.valueOf(value)).anyMatch(doubleValue -> Double.valueOf(userValue.toString()) > doubleValue);
        }
    },
    /**
     * Returns true if userValue is greater than or equals to any of the value
     * in list. Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    GREATER_THAN_OR_EQUALS("GTE") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || userValue instanceof UnlaunchStringValue || userValue instanceof UnlaunchBooleanValue) {
                return false;
            }

            if (AttributeType.DATE.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
                LocalDate userDate = userDateTime.toLocalDate();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate()).
                        anyMatch(ruleDate -> userDate.isEqual(ruleDate) || userDate.isAfter(ruleDate));

            } else if (AttributeType.DATE_TIME.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDateTime()).
                        anyMatch(ruleDateTime -> userDateTime.isEqual(ruleDateTime) || userDateTime.isAfter(ruleDateTime));
            }
            return values.stream().mapToDouble(value -> Double.valueOf(value)).anyMatch(doubleValue -> Double.valueOf(userValue.toString()) >= doubleValue);

        }
    },
    /**
     * Returns true if userValue is less than to any of the value in list.
     * Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    LESS_THAN("LT") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || userValue instanceof UnlaunchStringValue || userValue instanceof UnlaunchBooleanValue) {
                return false;
            }

            if (AttributeType.DATE.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
                LocalDate userDate = userDateTime.toLocalDate();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate()).
                        anyMatch(ruleDate -> userDate.isBefore(ruleDate));

            } else if (AttributeType.DATE_TIME.equals(type)) {
                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDateTime()).
                        anyMatch(ruleDateTime -> userDateTime.isBefore(ruleDateTime));
            }
            return values.stream().mapToDouble(value -> Double.valueOf(value)).anyMatch(doubleValue -> Double.valueOf(userValue.toString()) < doubleValue);
        }

    },
    /**
     * Returns true if userValue is less than or equals to any of the value in
     * list. Returns false if userValue is null or userValue is type of
     * UnlaunchStringValue or userValue is type of UnlaunchBooleanValue.
     */
    LESS_THAN_OR_EQUALS("LTE") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || userValue instanceof UnlaunchStringValue || userValue instanceof UnlaunchBooleanValue) {
                return false;
            }

            if (AttributeType.DATE.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();
                LocalDate userDate = userDateTime.toLocalDate();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDate()).anyMatch(ruleDate -> userDate.isEqual(ruleDate) || userDate.isBefore(ruleDate));
//                

            } else if (AttributeType.DATE_TIME.equals(type)) {

                LocalDateTime userDateTime = ((UnlaunchDateTimeValue) userValue).get();

                return values.stream().map(value -> Instant.ofEpochMilli(Long.valueOf(value)).atZone(ZoneId.of("UTC")).toLocalDateTime()).anyMatch(ruleDateTime -> userDateTime.isEqual(ruleDateTime) || userDateTime.isBefore(ruleDateTime));
//                return values.stream().mapToLong(value -> Long.valueOf(value)).anyMatch(longValue -> userDateTime.getEpoc() <= longValue);
            }

            return values.stream().mapToDouble(value -> Double.valueOf(value)).anyMatch(doubleValue -> Double.valueOf(userValue.toString()) <= doubleValue);
        }

    },
    /**
     *
     */
    IS_ONE_OF("IN") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null) {
                return false;
            }

            return values.contains(userValue.toString());
        }

    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and starts with
     * any of the value in list. Returns false if userValue is null or userValue
     * is not type of UnlaunchStringValue
     */
    STARTS_WITH("SW") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
                return false;
            }

            return values.stream().anyMatch(value -> userValue.toString().startsWith(value));
        }

    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and ends with
     * any of the value in list. Returns false if userValue is null or userValue
     * is not type of UnlaunchStringValue
     */
    ENDS_WITH("EW") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
                return false;
            }

            return values.stream().anyMatch(value -> userValue.toString().endsWith(value));
        }

    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and contains any
     * of the value in list. Returns false if userValue is null or userValue is
     * not type of UnlaunchStringValue
     */
    CONTAINS("CON") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
                return false;
            }

            return values.stream().anyMatch(value -> userValue.toString().contains(value));
        }

    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and does not
     * start with any of the value in list. Returns false if userValue is null
     * or userValue is not type of UnlaunchStringValue
     */
    NOT_STARTS_WITH("NSW") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
                return false;
            }

            return values.stream().noneMatch(value -> userValue.toString().startsWith(value));
        }
    },
    /**
     * Returns true if userValue is type of UnlaunchStringValue and does not
     * ends with any of the value in list. Returns false if userValue is null or
     * userValue is not type of UnlaunchStringValue
     */
    NOT_ENDS_WITH("NEW") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchStringValue)) {
                return false;
            }

            return values.stream().noneMatch(value -> userValue.toString().endsWith(value));
        }
    },
    IS_PART_OF("PO") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
                return false;
            }

            Set userSet = (Set) userValue.get();
            Set<String> intersection = values.stream()
                    .distinct()
                    .filter(userSet::contains)
                    .collect(Collectors.toSet());
            return userSet.equals(intersection);
        }
    },
    IS_NOT_PART_OF("NPO") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
                return false;
            }

            Set userSet = (Set) userValue.get();
            return values.stream().noneMatch(value -> userSet.contains(value));
        }
    },
    HAS_ANY_OF("HA") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
                return false;
            }

            Set userSet = (Set) userValue.get();
            return values.stream().anyMatch(value -> userSet.contains(value));
        }
    },
    DOES_NOT_HAVE_ANY_OF("NHA") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
                return false;
            }

            Set userSet = (Set) userValue.get();
            return values.stream().noneMatch(value -> userSet.contains(value));
        }
    },
    HAS_ALL_OF("AO") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
                return false;
            }

            Set userSet = (Set) userValue.get();
            return values.stream().allMatch(value -> userSet.contains(value));
        }
    },
    DOES_NOT_HAVE_ALL_OF("NAO") {

        @Override
        public boolean apply(List<String> values, UnlaunchValue userValue, AttributeType type) {

            if (userValue == null || !(userValue instanceof UnlaunchSetValue)) {
                return false;
            }

            Set userSet = (Set) userValue.get();
            return values.stream().anyMatch(value -> !userSet.contains(value));
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
     * @param values
     * @param userValue
     * @param valueType
     * @return
     */
    public abstract boolean apply(List<String> values, UnlaunchValue userValue, AttributeType valueType);

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

}
