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
            String userPrompt = promptFactory.buildUserPrompt(
                request.event,
                request.optionA,
                request.optionB,
                request.optionC,
                request.playerInput
            );

            AiDecisionPayload ai = aiClient.classifyDecision(systemPrompt, userPrompt);

            DecisionType decision = normalize(ai != null ? ai.decision : null);

            ConsequenceDto selected = getSelectedConsequence(request, decision);

            String narrative = normalizeNarrative(
                ai != null ? ai.narrative : null,
                decision,
                selected
            );

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
            ConsequenceDto fallback = request.optionC;
            return new ResolveDecisionResponse(
                "C",
                new ConsequenceDto(
                    safeFallback(fallback.title, "Hotarare neclara"),
                    safeFallback(fallback.text, "Curtea nu poate deslusi vointa regelui."),
                    fallback.religion,
                    fallback.population,
                    fallback.army,
                    fallback.money
                )
            );
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

    private DecisionType normalize(DecisionType decision) {
        return decision == null ? DecisionType.C : decision;
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

    private String normalizeNarrative(String narrative, DecisionType decision, ConsequenceDto selected) {
        if (narrative != null && !narrative.trim().isEmpty()) {
            return narrative.trim();
        }

        if (selected != null && selected.text != null && !selected.text.trim().isEmpty()) {
            return selected.text.trim();
        }

        switch (decision) {
            case A:
                return "Regele inclina spre prima cale, iar curtea se supune poruncii sale.";
            case B:
                return "Regele alege a doua cale, iar sfetnicii isi pleaca fruntile.";
            case C:
            default:
                return "Regele graieste fara limpezime, iar sala ramane tulburata de nehotarare.";
        }
    }

    private String defaultTitleFor(DecisionType decision) {
        switch (decision) {
            case A:
                return "Hotararea intaia";
            case B:
                return "Hotararea a doua";
            case C:
            default:
                return "Hotarare neclara";
        }
    }

    private String safeFallback(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value.trim();
    }
}
