package org.vaibhav.poc.util;

import org.json.JSONObject;

public class JsonFlattener {

    public static String flatten(String json) {
        JSONObject obj = new JSONObject(json);
        return flattenObject(obj, "");
    }

    private static String flattenObject(JSONObject json, String prefix) {
        StringBuilder flat = new StringBuilder();

        for (String key : json.keySet()) {
            Object value = json.get(key);

            if (value instanceof JSONObject) {
                flat.append(flattenObject((JSONObject) value, prefix + key + "."));
            } else {
                flat.append(prefix).append(key).append("=").append(value).append("\n");
            }
        }

        return flat.toString();
    }
}
