package org.vaibhav.poc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagPromptBuilder {

    public String build(String newEventJson, List<Map<String, Object>> neighbors)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();

        sb.append("Analyze the following event for anomalies.\n\n");
        sb.append("NEW_EVENT:\n");
        sb.append(newEventJson).append("\n\n");

        sb.append("HISTORICAL_EVENTS:\n");

        for (int i = 0; i < neighbors.size(); i++) {
            Map<String, Object> metadata = (Map<String, Object>) neighbors.get(i).get("metadata");
            sb.append("Event ").append(i + 1).append(": ");
            sb.append(mapper.writeValueAsString(metadata)).append("\n");
        }

        sb.append("\nReturn JSON:\n");
        sb.append("{ \"isAnomaly\": true/false, \"reason\": \"<explanation>\" }\n");

        return sb.toString();
    }

    public List<String> toStringList(List<Object> neighbors) {
        List<String> results = new ArrayList<>();

        for (Object obj : neighbors) {
            Map<String, Object> match = (Map<String, Object>) obj;

            Map<String, Object> meta = (Map<String, Object>) match.get("metadata");

            if (meta != null && meta.containsKey("raw_event")) {
                results.add(meta.get("raw_event").toString());
            }
        }

        return results;
    }
}

