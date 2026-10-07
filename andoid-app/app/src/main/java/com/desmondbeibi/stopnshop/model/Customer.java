package com.desmondbeibi.stopnshop.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Validated, immutable details for a simulated customer order. */
public final class Customer {
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_LOCATION_LENGTH = 200;
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?[0-9]{7,15}");

    public enum Field { NAME, PHONE, LOCATION }

    private final String name;
    private final String phone;
    private final String location;

    public Customer(String name, String phone, String location) {
        Map<Field, String> errors = validate(name, phone, location);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        this.name = trim(name);
        this.phone = normalizePhone(phone);
        this.location = trim(location);
    }

    /** Returns all field errors together; a future screen can bind each to its input. */
    public static Map<Field, String> validate(String name, String phone, String location) {
        EnumMap<Field, String> errors = new EnumMap<>(Field.class);
        validateText(errors, Field.NAME, name, "Name", MAX_NAME_LENGTH);
        validateText(errors, Field.LOCATION, location, "Location", MAX_LOCATION_LENGTH);
        if (trim(phone).isEmpty()) {
            errors.put(Field.PHONE, "Phone number is required.");
        } else if (!PHONE_PATTERN.matcher(normalizePhone(phone)).matches()) {
            errors.put(Field.PHONE, "Use 7-15 digits, optionally starting with +. Spaces and hyphens are allowed.");
        }
        return Collections.unmodifiableMap(errors);
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    private static void validateText(Map<Field, String> errors, Field field, String text,
                                     String label, int maxLength) {
        String trimmed = trim(text);
        if (trimmed.isEmpty()) {
            errors.put(field, label + " is required.");
        } else if (trimmed.length() > maxLength) {
            errors.put(field, label + " must be at most " + maxLength + " characters.");
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        int start = 0;
        int end = value.length();
        while (start < end && isWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }

    private static boolean isWhitespace(char character) {
        return Character.isWhitespace(character) || Character.isSpaceChar(character);
    }

    private static String normalizePhone(String value) {
        return trim(value).replace(" ", "").replace("-", "");
    }

    /** Retains specific field errors without creating a partially valid Customer. */
    public static final class ValidationException extends IllegalArgumentException {
        private final Map<Field, String> errors;

        private ValidationException(Map<Field, String> errors) {
            super(errors.values().iterator().next());
            EnumMap<Field, String> copy = new EnumMap<>(Field.class);
            copy.putAll(errors);
            this.errors = Collections.unmodifiableMap(copy);
        }

        public Map<Field, String> getErrors() {
            return errors;
        }
    }
}
