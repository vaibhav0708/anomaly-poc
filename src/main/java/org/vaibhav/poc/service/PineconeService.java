package org.vaibhav.poc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class PineconeService {

    @Value("${pinecone.api-key}")
    private String apiKey;

    @Value("${pinecone.index-url}")
    private String indexUrl;

    private final HttpClient http = HttpClient.newHttpClient();


    // -------------------------------------------------------------------------
    // CREATE / UPSERT
    // -------------------------------------------------------------------------
    public String upsertVector(String vectorId, List<Double> embedding, Map<String, Object> metadata) throws IOException, InterruptedException {

        Map<String, Object> vector = Map.of(
                "id", vectorId,
                "values", embedding,
                "metadata", metadata
        );

        Map<String, Object> body = Map.of("vectors", List.of(vector));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(indexUrl + "/vectors/upsert"))
                .header("Content-Type", "application/json")
                .header("Api-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(body)))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }



    // -------------------------------------------------------------------------
    // FETCH VECTOR BY ID
    // -------------------------------------------------------------------------
    public String fetchVectorById(String id) {

        String url = indexUrl + "/vectors/fetch?ids=" + id;
        System.out.println("Fetching from: " + url);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            String response = res.body();

            System.out.println("RAW Pinecone Fetch Response → " + response);

            if (response == null || response.isBlank()) {
                return "{ \"error\": \"Empty response from Pinecone\" }";
            }

            JSONObject json = new JSONObject(response);

            // Pinecone returns: { "vectors": { "96": { ... } } }
            JSONObject vectors = json.optJSONObject("vectors");
            if (vectors == null || vectors.length() == 0) {
                return "{ \"error\": \"Vector not found\", \"id\": \"" + id + "\" }";
            }

            JSONObject vectorData = vectors.optJSONObject(id);
            if (vectorData == null) {
                return "{ \"error\": \"Vector exists in response but missing under ID\", \"id\": \"" + id + "\" }";
            }

            return vectorData.toString(2); // pretty JSON

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch vector: " + id, e);
        }
    }


    // -------------------------------------------------------------------------
    // UPDATE (Same as upsert)
    // -------------------------------------------------------------------------
    public String updateVector(String id, List<Double> embedding, Map<String, Object> metadata) throws IOException, InterruptedException {
        return upsertVector(id, embedding, metadata);
    }


    // -------------------------------------------------------------------------
    // DELETE VECTOR
    // -------------------------------------------------------------------------
    public String deleteVector(String id) {

        JSONObject body = new JSONObject();
        body.put("ids", new JSONArray().put(id));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(indexUrl + "/vectors/delete"))
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))  // MUST be POST
                .build();

        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete vector: " + id, e);
        }
    }

    public String deleteAll() {
        JSONObject body = new JSONObject();
        body.put("deleteAll", true);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(indexUrl + "/vectors/delete"))
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete all vectors", e);
        }
    }


    // -------------------------------------------------------------------------
    // QUERY SIMILAR VECTORS
    // -------------------------------------------------------------------------
    public List<Object> querySimilar(List<Double> embedding, int topK) {
        JSONObject body = new JSONObject();
        body.put("topK", topK);
        body.put("includeMetadata", true);
//        body.put("includeValues", true); // to show vector values
        body.put("vector", embedding);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(indexUrl + "/query"))
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(res.body());
            JSONArray matches = json.optJSONArray("matches");

            if (matches == null) {
                return List.of();  // empty list
            }

            return matches.toList();  // <-- THIS is what Spring can serialize
        } catch (Exception e) {
            throw new RuntimeException("Failed querying similar vectors", e);
        }
    }
}
