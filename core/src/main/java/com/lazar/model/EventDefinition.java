package com.lazar.model;

import com.badlogic.gdx.utils.Array;
import com.lazar.engine.conditions.EventCondition;
import com.lazar.engine.flags.OptionEffects;

public class EventDefinition {
    public String id;
    public String title;
    public String description;
    public String imagePath;

    public EventCategory category;
    public EventRarity rarity;

    public String arcId;
    public ArcStage arcStage;

    public float crisisBias;
    public int cooldownTurns;
    public float baseWeight;

    public Array<EventCondition> requires;
    public Array<EventCondition> blockedBy;

    public OptionEffects onA;
    public OptionEffects onB;
    public OptionEffects onC;

    public EventDecisionData decisions;

    public EventDefinition() {
    }

    public EventCard toEventCard() {
        return new EventCard(id, title, description, imagePath, decisions);
    }
}
