package com.lazar.ai.model;

public class AiDecisionPayload {
    public DecisionType decision;
    public String narrative;
    public String reason;

    public AiDecisionPayload() {
    }

    public AiDecisionPayload(DecisionType decision, String narrative, String reason) {
        this.decision = decision;
        this.narrative = narrative;
        this.reason = reason;
    }
}
