package dev.springflow.cli.model;

import dev.springflow.cli.util.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelDefinition {

    private String name;
    private List<FieldDefinition> fields;

    public ModelDefinition() {
        this.fields = new ArrayList<>();
    }

    public ModelDefinition(
            String name,
            List<FieldDefinition> fields) {

        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields;
    }

    /**
     * Find the field marked as the ID.
     */
    public FieldDefinition idField() {

        return fields.stream()
                .filter(FieldDefinition::isId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Find a field by name.
     */
    public FieldDefinition field(String name) {

        return fields.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Return fields excluding the ID field.
     */
    public List<FieldDefinition> nonIdFields() {

        return fields.stream()
                .filter(f -> !f.isId())
                .toList();
    }

    /**
     * Return fields excluding the ID field and
     * boolean fields with defaults (auto-managed).
     */
    public List<FieldDefinition> createFields() {

        return fields.stream()
                .filter(f -> !f.isId())
                .filter(f -> !(f.isBoolean()
                        && f.getDefaultValue() != null))
                .toList();
    }

    /**
     * Return the TypeScript interface name.
     */
    public String typescriptName() {
        return name;
    }

    /**
     * Return the plural form (simple: just add "s").
     */
    public String pluralName() {

        if (name.endsWith("y")) {
            return name.substring(0, name.length() - 1)
                    + "ies";
        }

        if (name.endsWith("s")
                || name.endsWith("x")
                || name.endsWith("z")
                || name.endsWith("ch")
                || name.endsWith("sh")) {
            return name + "es";
        }

        return name + "s";
    }

    /**
     * Return the endpoint class name.
     */
    public String endpointName() {
        return name + "Endpoint";
    }

    /**
     * Return the service class name.
     */
    public String serviceName() {
        return name + "Service";
    }

    /**
     * Return the repository class name.
     */
    public String repositoryName() {
        return name + "Repository";
    }

    /**
     * Return the DTO class name.
     */
    public String dtoName() {
        return name + "Dto";
    }

    /**
     * Return the mapper class name.
     */
    public String mapperName() {
        return name + "Mapper";
    }

    /**
     * Return the lowercase variable name.
     */
    public String varName() {

        return Character.toLowerCase(name.charAt(0))
                + name.substring(1);
    }

    /**
     * Return the lowercase plural variable name.
     */
    public String pluralVarName() {

        return Character.toLowerCase(
                        pluralName().charAt(0))
                + pluralName().substring(1);
    }

    // ================================================================
    // JSON SERIALIZATION / DESERIALIZATION
    // ================================================================

    /**
     * Serialize to JSON string.
     */
    public String toJson() {

        Map<String, Object> root =
                new LinkedHashMap<>();

        root.put("name", name);

        List<Map<String, Object>> fieldList =
                new ArrayList<>();

        for (FieldDefinition field : fields) {
            fieldList.add(field.toJson());
        }

        root.put("fields", fieldList);

        return toJsonValue(root, 0);
    }

    /**
     * Write the model to a JSON file.
     */
    public void writeToFile(Path path)
            throws IOException {

        Files.createDirectories(path.getParent());

        Files.writeString(
                path,
                toJson(),
                StandardCharsets.UTF_8
        );
    }

    /**
     * Load a model from a JSON file.
     */
    @SuppressWarnings("unchecked")
    public static ModelDefinition load(Path path)
            throws IOException {

        String content =
                Files.readString(path, StandardCharsets.UTF_8);

        Map<String, Object> root =
                Json.parse(content);

        String name =
                (String) root.get("name");

        List<FieldDefinition> fields =
                new ArrayList<>();

        Object fieldsObj = root.get("fields");

        if (fieldsObj instanceof List<?> fieldList) {

            for (Object item : fieldList) {

                if (item instanceof Map<?, ?> map) {

                    FieldDefinition field =
                            new FieldDefinition();

                    field.setName(
                            (String) map.get("name")
                    );

                    field.setType(
                            (String) map.get("type")
                    );

                    Object idFlag = map.get("id");
                    if (idFlag instanceof Boolean b) {
                        field.setId(b);
                    }

                    Object labelVal = map.get("label");
                    if (labelVal instanceof String s) {
                        field.setLabel(s);
                    }

                    Object reqVal = map.get("required");
                    if (reqVal instanceof Boolean b) {
                        field.setRequired(b);
                    }

                    Object uniqVal = map.get("unique");
                    if (uniqVal instanceof Boolean b) {
                        field.setUnique(b);
                    }

                    Object defVal = map.get("default");
                    if (defVal != null) {
                        field.setDefaultValue(
                                String.valueOf(defVal)
                        );
                    }

                    Object maxVal = map.get("max");
                    if (maxVal instanceof Number n) {
                        field.setMax(n.intValue());
                    }

                    Object minVal = map.get("min");
                    if (minVal instanceof Number n) {
                        field.setMin(n.intValue());
                    }

                    fields.add(field);
                }
            }
        }

        return new ModelDefinition(name, fields);
    }

    // ================================================================
    // MINIMAL JSON SERIALIZER
    // ================================================================

    private static String toJsonValue(
            Object value,
            int indent) {

        if (value == null) {
            return "null";
        }

        if (value instanceof String s) {
            return "\"" + escapeJson(s) + "\"";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }

        if (value instanceof Map<?, ?> map) {
            return toJsonObject(map, indent);
        }

        if (value instanceof List<?> list) {
            return toJsonArray(list, indent);
        }

        return "\"" + escapeJson(
                String.valueOf(value)) + "\"";
    }

    private static String toJsonObject(
            Map<?, ?> map,
            int indent) {

        if (map.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        String pad = "  ".repeat(indent + 1);
        String closePad = "  ".repeat(indent);

        sb.append("{\n");

        int i = 0;

        for (Map.Entry<?, ?> entry : map.entrySet()) {

            sb.append(pad)
                    .append("\"")
                    .append(entry.getKey())
                    .append("\": ")
                    .append(toJsonValue(
                            entry.getValue(),
                            indent + 1
                    ));

            if (i < map.size() - 1) {
                sb.append(",");
            }

            sb.append("\n");
            i++;
        }

        sb.append(closePad).append("}");

        return sb.toString();
    }

    private static String toJsonArray(
            List<?> list,
            int indent) {

        if (list.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        String pad = "  ".repeat(indent + 1);
        String closePad = "  ".repeat(indent);

        sb.append("[\n");

        for (int i = 0; i < list.size(); i++) {

            sb.append(pad)
                    .append(toJsonValue(
                            list.get(i),
                            indent + 1
                    ));

            if (i < list.size() - 1) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append(closePad).append("]");

        return sb.toString();
    }

    private static String escapeJson(String s) {

        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
