import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// JsonHelper writes and reads the stored messages as JSON.
// No outside library is used: each message is a flat JSON object whose values are
// all text, so a small reader and writer are enough.
//
// References:
// Bray, T. (ed.). 2017. RFC 8259: The JavaScript Object Notation (JSON) Data
// Interchange Format. [Online]. Available at: https://www.rfc-editor.org/rfc/rfc8259
// [Accessed 10 November 2026]. (The rules for arrays, objects, strings and escape
// characters in this class follow sections 4, 5 and 7 of the RFC.)
final class JsonHelper {

    private JsonHelper() {
    }

    // Replaces characters that are not allowed raw inside a JSON string.
    static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Converts a list of objects (each a set of text keys and values) into a JSON array.
    static String toJson(List<Map<String, String>> objects) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < objects.size(); i++) {
            Map<String, String> object = objects.get(i);
            json.append("  {\n");
            int field = 0;
            for (Map.Entry<String, String> entry : object.entrySet()) {
                json.append("    \"").append(escape(entry.getKey())).append("\": \"")
                        .append(escape(entry.getValue())).append("\"");
                field++;
                if (field < object.size()) {
                    json.append(",");
                }
                json.append("\n");
            }
            json.append("  }");
            if (i < objects.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");
        return json.toString();
    }

    // Reads a JSON array of flat objects into a list of key/value maps.
    // Throws IllegalArgumentException if the text is not valid JSON of that shape.
    static List<Map<String, String>> parseArray(String json) {
        Reader reader = new Reader(json);
        return reader.readArray();
    }

    // Walks through the JSON text one character at a time.
    private static final class Reader {

        private final String text;
        private int position = 0;

        Reader(String text) {
            this.text = text;
        }

        List<Map<String, String>> readArray() {
            List<Map<String, String>> result = new ArrayList<>();
            skipWhitespace();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                position++;
            } else {
                while (true) {
                    result.add(readObject());
                    skipWhitespace();
                    char next = take();
                    if (next == ']') {
                        break;
                    }
                    if (next != ',') {
                        throw error("Expected ',' or ']'");
                    }
                }
            }
            skipWhitespace();
            if (position != text.length()) {
                throw error("Unexpected text after the end of the array");
            }
            return result;
        }

        private Map<String, String> readObject() {
            Map<String, String> object = new LinkedHashMap<>();
            skipWhitespace();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                position++;
                return object;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                String value;
                if (peek() == '"') {
                    value = readString();
                } else {
                    value = readBareValue();
                }
                object.put(key, value);
                skipWhitespace();
                char next = take();
                if (next == '}') {
                    return object;
                }
                if (next != ',') {
                    throw error("Expected ',' or '}'");
                }
            }
        }

        // Reads a quoted string and turns escape sequences back into characters.
        private String readString() {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (true) {
                char c = take();
                if (c == '"') {
                    return value.toString();
                }
                if (c != '\\') {
                    value.append(c);
                    continue;
                }
                char escaped = take();
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        value.append(escaped);
                        break;
                    case 'b':
                        value.append('\b');
                        break;
                    case 'f':
                        value.append('\f');
                        break;
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'u':
                        if (position + 4 > text.length()) {
                            throw error("Incomplete unicode escape");
                        }
                        value.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        position += 4;
                        break;
                    default:
                        throw error("Unknown escape \\" + escaped);
                }
            }
        }

        // Reads a value that is not in quotes (a number, true, false or null) as text.
        private String readBareValue() {
            int start = position;
            while (position < text.length() && ",}]".indexOf(text.charAt(position)) < 0) {
                position++;
            }
            String value = text.substring(start, position).trim();
            if (value.isEmpty()) {
                throw error("Missing value");
            }
            return value;
        }

        private void skipWhitespace() {
            while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
                position++;
            }
        }

        private char peek() {
            if (position >= text.length()) {
                throw error("Unexpected end of the JSON text");
            }
            return text.charAt(position);
        }

        private char take() {
            char c = peek();
            position++;
            return c;
        }

        private void expect(char expected) {
            char found = take();
            if (found != expected) {
                position--;
                throw error("Expected '" + expected + "' but found '" + found + "'");
            }
        }

        private IllegalArgumentException error(String problem) {
            return new IllegalArgumentException(problem + " (at character " + position + ")");
        }
    }
}