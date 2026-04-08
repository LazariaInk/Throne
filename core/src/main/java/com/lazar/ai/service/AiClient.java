package com.lazar.ai.service;


import com.lazar.ai.model.AiDecisionPayload;

public interface AiClient {
    AiDecisionPayload classifyDecision(String systemPrompt, String userPrompt) throws Exception;
}
