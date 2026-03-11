package com.lazar.dto;

public class ResolveDecisionRequest {
    public EventCardDto event;
    public ConsequenceDto optionA;
    public ConsequenceDto optionB;
    public ConsequenceDto optionC;
    public String playerInput;

    public ResolveDecisionRequest() {
    }

    public ResolveDecisionRequest(
        EventCardDto event,
        ConsequenceDto optionA,
        ConsequenceDto optionB,
        ConsequenceDto optionC,
        String playerInput
    ) {
        this.event = event;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.playerInput = playerInput;
    }
}
