package com.lazar.ai.service;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.lazar.ai.model.AiDecisionPayload;
import com.lazar.ai.model.DecisionType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAiClient implements AiClient {

    private final OpenAiConfig config;

    public OpenAiClient(OpenAiConfig config) {
        this.config = config;
    }

    @Override
    public AiDecisionPayload classifyDecision(String systemPrompt, String userPrompt) throws Exception {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(config.baseUrl + "/responses");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + config.apiKey);
            conn.setRequestProperty("Content-Type", "application/json");

            String body =
                "{"
                    + "\"model\": " + quote(config.model) + ","
                    + "\"instructions\": " + quote(systemPrompt) + ","
                    + "\"input\": " + quote(userPrompt) + ","
                    + "\"text\": {"
                    + "\"format\": {"
                    + "\"type\": \"json_schema\","
                    + "\"name\": \"ai_decision_payload\","
                    + "\"strict\": true,"
                    + "\"schema\": {"
                    + "\"type\": \"object\","
                    + "\"additionalProperties\": false,"
                    + "\"properties\": {"
                    + "\"decision\": {"
                    + "\"type\": \"string\","
                    + "\"enum\": [\"A\", \"B\", \"C\"]"
                    + "},"
                    + "\"narrative\": {"
                    + "\"type\": \"string\""
                    + "},"
                    + "\"reason\": {"
                    + "\"type\": \"string\""
                    + "}"
                    + "},"
                    + "\"required\": [\"decision\", \"narrative\", \"reason\"]"
                    + "}"
                    + "}"
                    + "}"
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
                throw new RuntimeException("OpenAI error " + code + ": " + response);
            }

            String outputText = extractOutputText(response);
            if (outputText == null || outputText.trim().isEmpty()) {
                throw new RuntimeException("OpenAI returned empty output_text");
            }

            return parseDecisionPayload(outputText);

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String extractOutputText(String json) {
        JsonValue root = new JsonReader().parse(json);
        JsonValue output = root.get("output");
        if (output == null) {
            return null;
        }

        for (JsonValue item = output.child; item != null; item = item.next) {
            JsonValue content = item.get("content");
            if (content == null) {
                continue;
            }

            for (JsonValue part = content.child; part != null; part = part.next) {
                String type = part.getString("type", "");
                if ("output_text".equals(type)) {
                    return part.getString("text", null);
                }
            }
        }

        return null;
    }

    private AiDecisionPayload parseDecisionPayload(String jsonText) {
        JsonValue root = new JsonReader().parse(jsonText);

        String decisionRaw = root.getString("decision", "C");
        String narrative = root.getString("narrative", "");
        String reason = root.getString("reason", "");

        DecisionType decision;
        try {
            decision = DecisionType.valueOf(decisionRaw);
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
