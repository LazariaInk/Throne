package com.lazar.ai.service;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.lazar.ai.model.AiDecisionPayload;
import com.lazar.ai.model.DecisionType;
import com.lazar.model.DecisionOption;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaClient implements AiClient {

    private final OllamaConfig config;

    public OllamaClient(OllamaConfig config) {
        this.config = config;
    }

    @Override
    public AiDecisionPayload classifyDecision(String systemPrompt, String userPrompt) throws Exception {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(config.baseUrl + "/api/generate");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(config.timeoutSeconds * 1000);
            conn.setReadTimeout(config.timeoutSeconds * 1000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String body =
                "{"
                    + "\"model\": " + quote(config.model) + ","
                    + "\"prompt\": " + quote(userPrompt) + ","
                    + "\"system\": " + quote(systemPrompt) + ","
                    + "\"stream\": false,"
                    + "\"keep_alive\": " + quote(config.keepAlive) + ","
                    + "\"think\": false"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

            String response = readFully(stream);

            if (code < 200 || code >= 300) {
                throw new RuntimeException("Ollama error " + code + ": " + response);
            }

            JsonValue root = new JsonReader().parse(response);
            String modelText = root.getString("response", "");

            if (modelText == null || modelText.trim().isEmpty()) {
                throw new RuntimeException("Empty response from Ollama");
            }

            String json = extractJsonObject(modelText);
            return parseDecisionPayload(json);

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new RuntimeException("Model response does not contain valid JSON: " + text);
    }

    private AiDecisionPayload parseDecisionPayload(String jsonText) {
        JsonValue root = new JsonReader().parse(jsonText);

        String decisionRaw = root.getString("decision", "C");
        String narrative = root.getString("narrative", "");
        String reason = root.getString("reason", "");

        DecisionType decision;
        try {
            decision = DecisionType.valueOf(decisionRaw.trim().toUpperCase());
        } catch (Exception e) {
            decision = DecisionType.C;
        }

        return new AiDecisionPayload(decision, narrative, reason);
    }

    private String quote(String value) {
        return "\"" + escapeJson(value) + "\"";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private String readFully(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
