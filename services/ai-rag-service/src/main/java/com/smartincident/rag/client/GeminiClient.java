package com.smartincident.rag.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for Google Gemini API.
 * Provides embedding and text generation capabilities.
 */
@Component
@Slf4j
public class GeminiClient {

    private final HttpClient httpClient;
    private final Gson gson;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Value("${gemini.model.embedding:models/gemini-embedding-001}")
    private String embeddingModel;

    @Value("${gemini.model.embedding.dimensions:768}")
    private int embeddingDimensions;

    @Value("${gemini.model.generation:models/gemini-1.5-flash}")
    private String generationModel;

    public GeminiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }

    /**
     * Generate embedding for text using Gemini embedding model.
     *
     * @param text Text to embed
     * @return 768-dimensional float array
     */
    public float[] generateEmbedding(String text) {
        try {
            String url = String.format("%s/%s:embedContent?key=%s",
                    baseUrl, embeddingModel, apiKey);

            JsonObject content = new JsonObject();
            JsonObject parts = new JsonObject();
            parts.addProperty("text", text);

            JsonArray partsArray = new JsonArray();
            partsArray.add(parts);
            content.add("parts", partsArray);

            JsonObject requestBody = new JsonObject();
            requestBody.add("content", content);
            requestBody.addProperty("outputDimensionality", embeddingDimensions);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini embedding API error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Embedding API failed: " + response.statusCode());
            }

            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            JsonArray values = jsonResponse
                    .getAsJsonObject("embedding")
                    .getAsJsonArray("values");

            float[] embedding = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                embedding[i] = values.get(i).getAsFloat();
            }

            log.debug("Generated embedding with {} dimensions", embedding.length);
            return embedding;

        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage(), e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    /**
     * Generate text response using Gemini generation model.
     *
     * @param prompt The prompt to send
     * @return Generated text response
     */
    public String generateContent(String prompt) {
        try {
            String url = String.format("%s/%s:generateContent?key=%s",
                    baseUrl, generationModel, apiKey);

            JsonObject parts = new JsonObject();
            parts.addProperty("text", prompt);

            JsonArray partsArray = new JsonArray();
            partsArray.add(parts);

            JsonObject content = new JsonObject();
            content.add("parts", partsArray);

            JsonArray contents = new JsonArray();
            contents.add(content);

            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.3);
            generationConfig.addProperty("maxOutputTokens", 2048);

            JsonObject requestBody = new JsonObject();
            requestBody.add("contents", contents);
            requestBody.add("generationConfig", generationConfig);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini generation API error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Generation API failed: " + response.statusCode());
            }

            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            String generatedText = jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            log.debug("Generated response with {} characters", generatedText.length());
            return generatedText;

        } catch (Exception e) {
            log.error("Failed to generate content: {}", e.getMessage(), e);
            throw new RuntimeException("Content generation failed", e);
        }
    }

    /**
     * Convert float array to pgvector-compatible string format.
     */
    public static String embeddingToString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
