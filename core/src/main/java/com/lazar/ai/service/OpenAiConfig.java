package com.lazar.ai.service;

public class OpenAiConfig {
    public final String baseUrl;
    public final String apiKey;
    public final String model;

    public OpenAiConfig(String baseUrl, String apiKey, String model) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }
}
