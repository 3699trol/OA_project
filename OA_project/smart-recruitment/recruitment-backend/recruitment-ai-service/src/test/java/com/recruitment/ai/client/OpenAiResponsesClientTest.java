package com.recruitment.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.OpenAiProperties;
import com.recruitment.api.dto.AiResumeParseResponse;
import com.recruitment.common.core.exception.BusinessException;
import com.sun.net.httpserver.HttpServer;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiResponsesClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldCallResponsesApiAndParseStructuredOutput() throws IOException {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/responses", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String outputText = "{\"name\":\"张三\",\"email\":\"zhangsan@example.com\","
                    + "\"phone\":\"13800138000\",\"skills\":[\"Java\",\"Redis\"]}";
            String response = new ObjectMapper().writeValueAsString(new Object() {
                public final Object[] output = {
                        new Object() {
                            public final Object[] content = {
                                    new Object() {
                                        public final String type = "output_text";
                                        public final String text = outputText;
                                    }
                            };
                        }
                };
            });
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        OpenAiProperties properties = properties("test-key");
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        properties.setBaseUrl(baseUrl);
        OpenAiResponsesClient client = new OpenAiResponsesClient(
                new OkHttpClient(), properties, new ObjectMapper());

        AiResumeParseResponse result = client.generateStructured(
                "parse resume", "resume content", "resume_parse",
                AiResponseSchemas.resumeParse(), AiResumeParseResponse.class);

        assertEquals("张三", result.getName());
        assertEquals(2, result.getSkills().size());
        assertEquals("Bearer test-key", authorization.get());
        assertTrue(requestBody.get().contains("\"json_schema\""));
        assertTrue(requestBody.get().contains("\"overallScore\""));
        assertTrue(requestBody.get().contains("\"suggestions\""));
        assertTrue(requestBody.get().contains("\"store\":false"));
        assertTrue(requestBody.get().contains("\"max_output_tokens\":2500"));
        assertTrue(requestBody.get().contains("\"effort\":\"xhigh\""));
    }

    @Test
    void shouldRejectCallWhenApiKeyIsMissing() {
        OpenAiResponsesClient client = new OpenAiResponsesClient(
                new OkHttpClient(), properties(""), new ObjectMapper());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                client.generateStructured("instructions", "input", "resume_parse",
                        AiResponseSchemas.resumeParse(), AiResumeParseResponse.class));

        assertEquals(503, exception.getCode());
        assertTrue(exception.getMessage().contains("OPENAI_API_KEY"));
    }

    private OpenAiProperties properties(String apiKey) {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey(apiKey);
        properties.setModel("qwen3.7-plus");
        properties.setReasoningEffort("xhigh");
        return properties;
    }
}
