package com.lazar.ai.service;

public class OllamaConfig {
    public final String baseUrl;
    public final String model;
    public final String keepAlive;
    public final int timeoutSeconds;

    public OllamaConfig(String baseUrl, String model, String keepAlive, int timeoutSeconds) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.keepAlive = keepAlive;
        this.timeoutSeconds = timeoutSeconds;
    }
}
