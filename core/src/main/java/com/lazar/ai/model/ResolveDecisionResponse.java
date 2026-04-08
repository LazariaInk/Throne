package com.lazar.ai.model;

public class ResolveDecisionResponse {
    public String resolvedOption;
    public ConsequenceDto consequence;

    public ResolveDecisionResponse() {
    }

    public ResolveDecisionResponse(String resolvedOption, ConsequenceDto consequence) {
        this.resolvedOption = resolvedOption;
        this.consequence = consequence;
    }
}
