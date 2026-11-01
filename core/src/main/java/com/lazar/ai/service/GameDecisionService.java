package com.lazar.ai.service;

import com.lazar.ai.model.*;

public class GameDecisionService {

    private final AiClient aiClient;
    private final PromptFactory promptFactory;

    public GameDecisionService(AiClient aiClient, PromptFactory promptFactory) {
        this.aiClient = aiClient;
        this.promptFactory = promptFactory;
    }

    public ResolveDecisionResponse resolve(ResolveDecisionRequest request) {
        validate(request);

        try {
            String systemPrompt = promptFactory.buildSystemPrompt();
            String userPrompt = promptFactory.buildUserPrompt(request);

            AiDecisionPayload ai = aiClient.classifyDecision(systemPrompt, userPrompt);

            if (ai == null || ai.decision == null) throw AiServiceException.localized("ai.error.invalid");
            DecisionType decision = ai.decision;

            ConsequenceDto selected = getSelectedConsequence(request, decision);

            String narrative = NarrativePolicy.select(ai.narrative, selected.text);

            ConsequenceDto finalConsequence = new ConsequenceDto(
                safeFallback(selected.title, defaultTitleFor(decision)),
                narrative,
                selected.religion,
                selected.population,
                selected.army,
                selected.money
            );

            return new ResolveDecisionResponse(decision.name(), finalConsequence);

        } catch (Exception e) {
            if (e instanceof AiServiceException) throw (AiServiceException) e;
            throw new AiServiceException(AiServiceException.messageFor(e));
        }
    }

    private void validate(ResolveDecisionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is null");
        }
        if (request.event == null) {
            throw new IllegalArgumentException("Event is null");
        }
        if (request.optionA == null) {
            throw new IllegalArgumentException("Option A is null");
        }
        if (request.optionB == null) {
            throw new IllegalArgumentException("Option B is null");
        }
        if (request.optionC == null) {
            throw new IllegalArgumentException("Option C is null");
        }
        if (request.playerInput == null || request.playerInput.trim().isEmpty()) {
            throw new IllegalArgumentException("Player input is blank");
        }
        if (request.playerInput.length() > 400) {
            throw new IllegalArgumentException("Player input too long");
        }
    }

    private ConsequenceDto getSelectedConsequence(ResolveDecisionRequest request, DecisionType decision) {
        switch (decision) {
            case A:
                return request.optionA;
            case B:
                return request.optionB;
            case C:
            default:
                return request.optionC;
        }
    }

    private String defaultTitleFor(DecisionType decision) {
        return decision == DecisionType.C ? "Umbre în sala tronului" : "Ecoul poruncii";
    }

    private String safeFallback(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value.trim();
    }
}
