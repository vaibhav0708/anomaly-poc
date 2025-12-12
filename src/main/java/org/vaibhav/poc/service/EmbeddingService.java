package org.vaibhav.poc.service;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientImpl;
import com.openai.client.okhttp.OkHttpClient;
import com.openai.core.ClientOptions;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    private final OpenAIClient client;

    public EmbeddingService(@Value("${spring.ai.openai.api-key}") String apiKey) {

        var http = OkHttpClient.builder()
                .baseUrl("https://api.openai.com/v1/")
                .build();

        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .httpClient(http)   // REQUIRED FIELD
                .build();

        this.client = new OpenAIClientImpl(options);
    }
    public List<Double> generateEmbedding(String input) {

        EmbeddingCreateParams req = EmbeddingCreateParams.builder()
                .model("text-embedding-3-small")
                .input(input)
                .build();

        Embedding embedding = client.embeddings()
                .create(req)
                .data()
                .get(0);

        List<Double> result = new ArrayList<>();
        for (Number n : embedding.embedding()) {
            result.add(n.doubleValue());
        }

        System.out.println("Embedding size = " + result.size());
        return result;
    }
}
