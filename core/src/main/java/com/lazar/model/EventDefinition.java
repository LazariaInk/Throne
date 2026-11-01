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
    public boolean strictArcOrder;
    public int followUpMaxWaitTurns;

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
        EventCard card = new EventCard(id, title, description, imagePath, decisions);
        card.actionA = onA == null ? null : onA.action;
        card.actionB = onB == null ? null : onB.action;
        card.actionC = onC == null ? null : onC.action;
        return card;
    }
}
