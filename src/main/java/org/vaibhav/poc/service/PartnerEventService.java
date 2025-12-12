package org.vaibhav.poc.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaibhav.poc.model.AnomalyResult;
import org.vaibhav.poc.model.PartnerEvent;
import org.vaibhav.poc.repository.PartnerEventRepository;
import org.vaibhav.poc.util.EventParser;
import org.vaibhav.poc.util.JsonFlattener;

import java.io.IOException;
import java.util.*;

@Service
public class PartnerEventService {

    @Autowired
    private PartnerEventRepository repository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private PineconeService pineconeService;

    @Autowired
    private LLMService llmService;

    public void processEvent(String json) throws IOException, InterruptedException {

        // -------------------------------------------------------------
        // Step 1: Normalize (Flatten JSON)
        // -------------------------------------------------------------
        String normalized = JsonFlattener.flatten(json);
        String partnerId = EventParser.extractPartnerId(json);

        // -------------------------------------------------------------
        // Step 2: Persist in Postgres
        // -------------------------------------------------------------
        PartnerEvent event = new PartnerEvent(null, partnerId, json, normalized);
        repository.save(event);

        // Ensure ID is generated
        Long eventId = event.getId();

        // -------------------------------------------------------------
        // Step 3: Create Embedding
        // -------------------------------------------------------------
        List<Double> embedding = embeddingService.generateEmbedding(normalized);

        // -------------------------------------------------------------
        // Step 4: Upsert Vector Into Pinecone
        // -------------------------------------------------------------
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("raw_event", json);
        metadata.put("partnerId", partnerId);

        pineconeService.upsertVector(String.valueOf(eventId),embedding, metadata);

        // -------------------------------------------------------------
        // Step 5: Query Similar Events
        // -------------------------------------------------------------
        List<Object> matches = pineconeService.querySimilar(embedding, 3);

        List<String> similarEvents = new ArrayList<>();

        for (Object m : matches) {
            Map<String, Object> match = (Map<String, Object>) m;

            Map<String, Object> metadata_events =
                    (Map<String, Object>) match.get("metadata");

            if (metadata != null && metadata.containsKey("raw_event")) {
                similarEvents.add(metadata.get("raw_event").toString());
            }
        }

        // -------------------------------------------------------------
        // Step 6: LLM Anomaly Detection
        // -------------------------------------------------------------
        AnomalyResult result = llmService.detectAnomaly(json, similarEvents);

        System.out.println("Anomaly Result → " + result);
    }
}
