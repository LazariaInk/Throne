package com.lazar.model;

public class DecisionResolution {
    public final DecisionOption option;
    public final Consequence consequence;

    public DecisionResolution(DecisionOption option, Consequence consequence) {
        this.option = option;
        this.consequence = consequence;
    }
}
