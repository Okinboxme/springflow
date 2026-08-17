package dev.springflow.cli.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser used by the CLI to read springflow.config.json.
 *
 * <p>Kept dependency-free so the CLI jar stays thin and runnable as
 * {@code java -jar springflow-cli.jar}. Only string/number/boolean/null
 * literals are fully supported; comments are not.
 */
public final class Json {

    private final String input;
    private int pos;

    private Json(String input) {
        this.input = input;
    }

    public static Map<String, Object> parse(String content) {

        Json parser = new Json(content);

        Object value = parser.parseRoot();

        if (!(value instanceof Map)) {
            throw new IllegalArgumentException(
                    "JSON root must be an object"
            );
        }

        return castMap(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private Object parseRoot() {
        skipWhitespace();
        return parseValue();
    }

    private Object parseValue() {

        skipWhitespace();

        if (pos >= input.length()) {
            throw new IllegalArgumentException(
                    "Unexpected end of JSON"
            );
        }

        char c = input.charAt(pos);

        return switch (c) {

            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();

            case 't' -> {
                expect("true");
                yield Boolean.TRUE;
            }

            case 'f' -> {
                expect("false");
                yield Boolean.FALSE;
            }

            case 'n' -> {
                expect("null");
                yield null;
            }

            default -> parseNumber();
        };
    }

    private Map<String, Object> parseObject() {

        Map<String, Object> map =
                new LinkedHashMap<>();

        pos++; // '{'

        skipWhitespace();

        if (peek() == '}') {
            pos++;
            return map;
        }

        while (true) {

            skipWhitespace();

            String key = parseString();

            skipWhitespace();

            if (peek() != ':') {
                throw new IllegalArgumentException(
                        "Expected ':'"
                );
            }

            pos++;

            map.put(key, parseValue());

            skipWhitespace();

            char c = peek();

            if (c == ',') {
                pos++;
                continue;
            }

            if (c == '}') {
                pos++;
                return map;
            }

            throw new IllegalArgumentException(
                    "Expected ',' or '}'"
            );
        }
    }

    private List<Object> parseArray() {

        List<Object> list =
                new ArrayList<>();

        pos++; // '['

        skipWhitespace();

        if (peek() == ']') {
            pos++;
            return list;
        }

        while (true) {

            list.add(parseValue());

            skipWhitespace();

            char c = peek();

            if (c == ',') {
                pos++;
                continue;
            }

            if (c == ']') {
                pos++;
                return list;
            }

            throw new IllegalArgumentException(
                    "Expected ',' or ']'"
            );
        }
    }

    private String parseString() {

        if (peek() != '"') {
            throw new IllegalArgumentException(
                    "Expected string"
            );
        }

        pos++;

        StringBuilder output =
                new StringBuilder();

        while (pos < input.length()) {

            char c = input.charAt(pos++);

            if (c == '"') {
                return output.toString();
            }

            if (c != '\\') {
                output.append(c);
                continue;
            }

            if (pos >= input.length()) {
                throw new IllegalArgumentException(
                        "Invalid escape sequence"
                );
            }

            char escaped = input.charAt(pos++);

            output.append(switch (escaped) {

                case 'n' -> '\n';
                case 't' -> '\t';
                case 'r' -> '\r';
                case 'b' -> '\b';
                case 'f' -> '\f';
                case '"' -> '"';
                case '\\' -> '\\';
                case '/' -> '/';
                case 'u' -> parseUnicode();

                default -> throw new IllegalArgumentException(
                        "Invalid escape: \\" + escaped
                );
            });
        }

        throw new IllegalArgumentException(
                "Unterminated string"
        );
    }

    private char parseUnicode() {

        if (pos + 4 > input.length()) {
            throw new IllegalArgumentException(
                    "Invalid unicode escape"
            );
        }

        String hex = input.substring(pos, pos + 4);

        pos += 4;

        return (char) Integer.parseInt(hex, 16);
    }

    private Number parseNumber() {

        int start = pos;

        while (pos < input.length()
                && isNumberChar(input.charAt(pos))) {

            pos++;
        }

        String token =
                input.substring(start, pos);

        try {

            if (token.contains(".")
                    || token.contains("e")
                    || token.contains("E")) {

                return Double.parseDouble(token);
            }

            return Long.parseLong(token);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid number: " + token
            );
        }
    }

    private static boolean isNumberChar(char c) {
        return (c >= '0' && c <= '9')
                || c == '-'
                || c == '+'
                || c == '.'
                || c == 'e'
                || c == 'E';
    }

    private void expect(String token) {

        if (!input.startsWith(token, pos)) {
            throw new IllegalArgumentException(
                    "Invalid token"
            );
        }

        pos += token.length();
    }

    private char peek() {

        skipWhitespace();

        if (pos >= input.length()) {
            throw new IllegalArgumentException(
                    "Unexpected end of JSON"
            );
        }

        return input.charAt(pos);
    }

    private void skipWhitespace() {

        while (pos < input.length()
                && Character.isWhitespace(
                input.charAt(pos))) {

            pos++;
        }
    }
}
