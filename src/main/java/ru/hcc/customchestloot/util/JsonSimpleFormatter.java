package ru.hcc.customchestloot.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;

class JsonSimpleFormatter {

    protected static void writeJson(JSONObject json, FileWriter writer) throws IOException {
        writeFormattedJson(json, writer, 0);
    }

    private static void writeFormattedJson(Object obj, FileWriter writer, int indent) throws IOException {
        switch (obj) {

            case JSONObject jsonObject -> {
                writer.write("{\n");

                int size = jsonObject.size();
                int count = 0;

                for (Object key : jsonObject.keySet()) {
                    addIndent(writer, indent + 2);
                    writer.write("\"" + key + "\": ");

                    Object value = jsonObject.get(key);
                    writeFormattedJson(value, writer, indent + 2);

                    if (++count < size) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }

                addIndent(writer, indent);
                writer.write("}");

            } case JSONArray jsonArray -> {
                boolean isNestedNumberArray = !jsonArray.isEmpty() &&
                        jsonArray.getFirst() instanceof JSONArray;

                if (isNestedNumberArray) {
                    writer.write("[ ");

                    int size = jsonArray.size();
                    for (int i = 0; i < size; i++) {
                        if (jsonArray.get(i) instanceof JSONArray array) {
                            writer.write("[");
                            int nestedArraySize = array.size();

                            for (int j = 0; j < nestedArraySize; j++) {
                                writer.write(array.get(j).toString());
                                if (j < nestedArraySize - 1) {
                                    writer.write(", ");
                                }
                            }

                            writer.write("]");

                            if (i < size - 1) {
                                writer.write(", ");
                            }
                        }
                    }

                    writer.write(" ]");
                } else {
                    writer.write("[\n");

                    int size = jsonArray.size();
                    for (int i = 0; i < size; i++) {
                        addIndent(writer, indent + 2);

                        Object element = jsonArray.get(i);
                        if (element instanceof String str) {
                            writer.write("\"" + escapeString(str) + "\"");
                        } else {
                            writer.write(element.toString());
                        }

                        if (i < size - 1) {
                            writer.write(",");
                        }
                        writer.write("\n");
                    }

                    addIndent(writer, indent);
                    writer.write("]");
                }
            }
            case String s -> writer.write("\"" + escapeString(s) + "\"");
            case null -> writer.write("null");
            default -> writer.write(obj.toString());
        }
        writer.flush();
    }

    private static void addIndent(FileWriter writer, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            writer.write(" ");
        }
    }

    private static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
