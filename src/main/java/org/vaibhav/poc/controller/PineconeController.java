package org.vaibhav.poc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.vaibhav.poc.model.AnomalyResult;
import org.vaibhav.poc.service.EmbeddingService;
import org.vaibhav.poc.service.LLMService;
import org.vaibhav.poc.service.PineconeService;
import org.vaibhav.poc.service.RagPromptBuilder;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/pinecone")
public class PineconeController {

    @Autowired
    private PineconeService pineconeService;

    @Autowired
    private EmbeddingService embeddingService;


    @Autowired
    private RagPromptBuilder ragPromptBuilder;

    @Autowired
    private LLMService llmService;

    // CREATE / UPSERT -----------------------------------------------------
    @PostMapping("/upsert")
    public String upsert(@RequestBody Map<String, Object> payload) throws IOException, InterruptedException {
        String vectorId = UUID.randomUUID().toString();

        // Build metadata from the entire payload
        Map<String, Object> metadata = new HashMap<>(payload);

        // Generate embedding based on raw JSON
        String jsonString = new ObjectMapper().writeValueAsString(payload);
        List<Double> embedding = embeddingService.generateEmbedding(jsonString);

        return pineconeService.upsertVector(vectorId, embedding, metadata);
    }


    // READ ----------------------------------------------------------------
    @GetMapping("/fetch/{id}")
    public String fetch(@PathVariable String id) {
        return pineconeService.fetchVectorById(id);
    }


    // UPDATE ---------------------------------------------------------------
    @PutMapping("/update/{id}")
    public String update(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) throws IOException, InterruptedException {
        List<Double> embedding = (List<Double>) payload.get("embedding");
        Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");

        return pineconeService.updateVector(id, embedding, metadata);
    }


    // DELETE ---------------------------------------------------------------
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        return pineconeService.deleteVector(id);
    }

    @DeleteMapping("delete")
    public String deleteAll(){
        return pineconeService.deleteAll();
    }

    // QUERY ---------------------------------------------------------------
    @PostMapping("/query")
    public List<Object> query(@RequestBody Map<String, Object> payload) {
        String input = (String) payload.get("input");
        int topK = payload.get("topK") != null
                ? ((Number) payload.get("topK")).intValue()
                : 3;

        List<Double> embedding = embeddingService.generateEmbedding(input);
        return pineconeService.querySimilar(embedding, topK);
    }

    @PostMapping("/detect")
    public AnomalyResult detectAnomaly(@RequestBody Map<String, Object> payload)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(payload);

        // 1. Generate embedding for new event
        List<Double> queryEmbedding = embeddingService.generateEmbedding(json);

        // 2. Query Pinecone for similar events (top 5)
        List<Object> neighbors = pineconeService.querySimilar(queryEmbedding, 5);

        // 3. Convert neighbors -> List<String> for LLM
        List<String> similarEvents = ragPromptBuilder.toStringList(neighbors);

        // 4. Call LLM detection
        AnomalyResult result = llmService.detectAnomaly(json, similarEvents);

        return result;
    }


}
