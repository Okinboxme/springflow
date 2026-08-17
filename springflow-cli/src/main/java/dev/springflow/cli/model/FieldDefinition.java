package dev.springflow.cli.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class FieldDefinition {

    private String name;
    private String type;
    private boolean id;
    private String label;
    private boolean required;
    private boolean unique;
    private String defaultValue;
    private Integer max;
    private Integer min;

    public FieldDefinition() {
    }

    public FieldDefinition(
            String name,
            String type) {

        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public String getLabel() {
        return label != null ? label : humanize(name);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    /**
     * Convert the Java type to a TypeScript type.
     */
    public String typescriptType() {

        if (type == null) {
            return "unknown";
        }

        return switch (type) {

            case "String", "Character" -> "string";

            case "boolean", "Boolean" -> "boolean";

            case "int", "Integer",
                    "long", "Long",
                    "double", "Double",
                    "float", "Float",
                    "short", "Short",
                    "byte", "Byte" -> "number";

            case "LocalDate",
                    "LocalDateTime",
                    "Instant" -> "string";

            default -> "unknown";
        };
    }

    /**
     * Return the Java primitive type for a boxed type.
     */
    public String javaPrimitive() {

        if (type == null) {
            return "Object";
        }

        return switch (type) {
            case "Long" -> "long";
            case "Integer" -> "int";
            case "Double" -> "double";
            case "Float" -> "float";
            case "Boolean" -> "boolean";
            default -> type;
        };
    }

    /**
     * Whether the type is numeric.
     */
    public boolean isNumeric() {

        if (type == null) {
            return false;
        }

        return switch (type) {
            case "int", "Integer",
                    "long", "Long",
                    "double", "Double",
                    "float", "Float",
                    "short", "Short",
                    "byte", "Byte" -> true;
            default -> false;
        };
    }

    /**
     * Whether the type is a boolean.
     */
    public boolean isBoolean() {

        return "boolean".equals(type)
                || "Boolean".equals(type);
    }

    /**
     * Whether the type is a String.
     */
    public boolean isString() {

        return "String".equals(type);
    }

    /**
     * Convert camelCase or snake_case to "First Last".
     */
    private static String humanize(String name) {

        if (name == null || name.isEmpty()) {
            return "";
        }

        String spaced = name
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ');

        return Character.toUpperCase(spaced.charAt(0))
                + spaced.substring(1);
    }

    /**
     * Build a JSON map for serialization.
     */
    public Map<String, Object> toJson() {

        Map<String, Object> map =
                new LinkedHashMap<>();

        map.put("name", name);
        map.put("type", type);

        if (id) {
            map.put("id", true);
        }

        if (label != null) {
            map.put("label", label);
        }

        if (required) {
            map.put("required", true);
        }

        if (unique) {
            map.put("unique", true);
        }

        if (defaultValue != null) {
            map.put("default", defaultValue);
        }

        if (max != null) {
            map.put("max", max);
        }

        if (min != null) {
            map.put("min", min);
        }

        return map;
    }
}
