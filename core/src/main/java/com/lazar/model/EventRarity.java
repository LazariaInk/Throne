package com.lazar.model;

public enum EventRarity {
    COMMON(1.00f),
    UNCOMMON(0.55f),
    RARE(0.22f),
    CRISIS(0.12f);

    public final float baseWeight;

    EventRarity(float baseWeight) {
        this.baseWeight = baseWeight;
    }
}
