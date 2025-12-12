package org.vaibhav.poc.service;

import com.google.gson.Gson;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientImpl;
import com.openai.client.okhttp.OkHttpClient;
import com.openai.core.ClientOptions;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaibhav.poc.model.AnomalyResult;

import java.util.List;

@Service
public class LLMService {

    private final OpenAIClient client;
    private final Gson gson = new Gson();

    public LLMService(@Value("${spring.ai.openai.api-key}") String apiKey) {
        var http = OkHttpClient.builder()
                .baseUrl("https://api.openai.com/v1/")
                .build();

        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .httpClient(http)
                .build();

        this.client = new OpenAIClientImpl(options);
    }

    public AnomalyResult detectAnomaly(String event, List<String> similarEvents) {

        String prompt = """
        You are an anomaly detection engine.

        NEW EVENT:
        %s

        SIMILAR HISTORICAL EVENTS:
        %s

        Return ONLY JSON:
        {
          "isAnomaly": true/false,
          "reason": "explanation"
        }
    """.formatted(event, similarEvents);

        ChatCompletionUserMessageParam userMsg =
                ChatCompletionUserMessageParam.builder()
                        .content(prompt)
                        .build();

        ChatCompletionMessageParam message =
                ChatCompletionMessageParam.ofUser(userMsg);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("gpt-4o-mini")
                .messages(List.of(message))
                .build();

        ChatCompletion response = client.chat()
                .completions()
                .create(params);

        // ✅ Extract ACTUAL TEXT from the "content" list
        String output = response
                .choices()
                .get(0)
                .message()
                .content()
                .orElse("")
                .trim();

        System.out.println("LLM RAW OUTPUT = " + output);

        // If model wraps JSON inside Markdown code blocks, strip them:
        if (output.startsWith("```")) {
            output = output.replace("```json", "")
                    .replace("```", "")
                    .trim();
        }

        return gson.fromJson(output, AnomalyResult.class);
    }
}
