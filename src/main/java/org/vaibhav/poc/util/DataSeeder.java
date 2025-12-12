package org.vaibhav.poc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaibhav.poc.service.EmbeddingService;
import org.vaibhav.poc.service.PineconeService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private PineconeService pineconeService;

    private final ObjectMapper mapper = new ObjectMapper();


    public void seedEvents(List<Map<String, Object>> events) throws Exception {

        int counter = 1;

        for (Map<String, Object> event : events) {

            String json = mapper.writeValueAsString(event);

            // Normalize for embeddings
            String flat = json;

            // Generate vector
            List<Double> embedding = embeddingService.generateEmbedding(flat);

            System.out.println("Embedding size = " + embedding.size());

            // Insert into Pinecone
            pineconeService.upsertVector(
                    "evt-" + counter,
                    embedding,
                    event // metadata = event itself
            );

            System.out.println("Inserted evt-" + counter);

            counter++;
        }

        System.out.println("Seed complete!");
    }
}
