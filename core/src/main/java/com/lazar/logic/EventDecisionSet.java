package com.lazar.logic;

import com.lazar.dto.ConsequenceDto;

public class EventDecisionSet {

    private final ConsequenceDto optionA;
    private final ConsequenceDto optionB;
    private final ConsequenceDto optionC;

    public EventDecisionSet(ConsequenceDto optionA, ConsequenceDto optionB, ConsequenceDto optionC) {
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
    }

    public ConsequenceDto getOptionA() {
        return optionA;
    }

    public ConsequenceDto getOptionB() {
        return optionB;
    }

    public ConsequenceDto getOptionC() {
        return optionC;
    }
}
