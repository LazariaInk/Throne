package com.lazar.engine.spawn;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.lazar.engine.GameRunState;
import com.lazar.engine.arc.StoryArcState;
import com.lazar.engine.conditions.ConditionEvaluator;
import com.lazar.model.ArcStage;
import com.lazar.model.EventDefinition;
import com.lazar.model.EventRarity;

public class EventSpawnService {

    private final Array<EventDefinition> allEvents;
    private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

    public EventSpawnService(Array<EventDefinition> allEvents) {
        this.allEvents = allEvents;
    }

    public EventDefinition drawNext(GameRunState state) {
        Array<ScoredEvent> eligible = new Array<>();

        for (EventDefinition event : allEvents) {
            if (!isEligible(event, state)) {
                continue;
            }

            float score = calculateScore(event, state);
            if (score > 0f) {
                eligible.add(new ScoredEvent(event, score));
            }
        }

        if (eligible.size == 0) {
            return fallback();
        }

        float total = 0f;
        for (ScoredEvent item : eligible) {
            total += item.score;
        }

        float roll = MathUtils.random() * total;
        float cumulative = 0f;

        for (ScoredEvent item : eligible) {
            cumulative += item.score;
            if (roll <= cumulative) {
                return item.event;
            }
        }

        return eligible.peek().event;
    }

    private boolean isEligible(EventDefinition event, GameRunState state) {
        if (state.isOnCooldown(event.id)) {
            return false;
        }

        if (!conditionEvaluator.allMatch(event.requires, state)) {
            return false;
        }

        if (conditionEvaluator.anyMatch(event.blockedBy, state)) {
            return false;
        }

        if (event.arcId != null && event.arcStage == ArcStage.SEED && state.activeArcCount() >= 3) {
            return false;
        }

        if (event.rarity == EventRarity.CRISIS && state.hasArcInCrisis()) {
            StoryArcState arc = state.getArc(event.arcId);
            if (arc == null || arc.currentStage != ArcStage.CRISIS) {
                return false;
            }
        }

        return true;
    }

    private float calculateScore(EventDefinition event, GameRunState state) {
        float rarityWeight = rarityWeight(event, state);
        float stateWeight = stateWeight(event, state);
        float arcWeight = arcWeight(event, state);
        float noveltyWeight = state.seenRecently(event.id) ? 0.6f : 1.0f;
        float doomWeight = 1f + (state.getTurn() / 120f);
        float baseWeight = event.baseWeight > 0 ? event.baseWeight : 1f;

        return baseWeight * rarityWeight * stateWeight * arcWeight * noveltyWeight * doomWeight;
    }

    private float rarityWeight(EventDefinition event, GameRunState state) {
        if (event.rarity == EventRarity.CRISIS) {
            if (state.getStats().hasCriticalStat() || state.getTurn() >= 40) {
                return 1.50f + event.crisisBias;
            }
            return event.rarity.baseWeight;
        }

        return event.rarity.baseWeight;
    }

    private float arcWeight(EventDefinition event, GameRunState state) {
        if (event.arcId == null) {
            return 1.0f;
        }

        StoryArcState arc = state.getArc(event.arcId);
        if (arc == null || arc.resolved || !arc.active) {
            return 1.0f;
        }

        if (arc.currentStage == event.arcStage) {
            return 2.5f;
        }

        if (isAdjacent(arc.currentStage, event.arcStage)) {
            return 1.4f;
        }

        return 0.9f;
    }

    private boolean isAdjacent(ArcStage current, ArcStage target) {
        if (current == ArcStage.SEED && target == ArcStage.COMMITMENT) return true;
        if (current == ArcStage.COMMITMENT && target == ArcStage.ESCALATION) return true;
        if (current == ArcStage.ESCALATION && target == ArcStage.CRISIS) return true;
        if (current == ArcStage.CRISIS && target == ArcStage.OUTCOME) return true;
        return false;
    }

    private float stateWeight(EventDefinition event, GameRunState state) {
        float weight = 1f;

        int religion = state.getStats().getReligion();
        int people = state.getStats().getPeople();
        int army = state.getStats().getArmy();
        int money = state.getStats().getMoney();
        int turn = state.getTurn();

        switch (event.category) {
            case CHURCH_FAITH:
                if (religion < 35) weight *= 1.8f;
                if (religion > 75) weight *= 1.8f;
                break;

            case NOBILITY_COURT:
                if (money < 35) weight *= 1.2f;
                if (army > 70) weight *= 1.2f;
                break;

            case PEASANTRY_COMMON_FOLK:
                if (people < 35) weight *= 1.9f;
                if (people > 80) weight *= 1.3f;
                break;

            case ARMY_WAR:
                if (army < 35) weight *= 1.9f;
                if (army > 80) weight *= 1.5f;
                if (money < 30) weight *= 1.2f;
                break;

            case TREASURY_TRADE:
                if (money < 35) weight *= 1.9f;
                if (money > 80) weight *= 1.3f;
                break;

            case JUSTICE_LAW:
                if (people < 40) weight *= 1.3f;
                if (army < 35) weight *= 1.1f;
                break;

            case PLAGUE_HEALTH:
                if (people < 35) weight *= 2.0f;
                if (state.hasFlag("plague_known")) weight *= 1.7f;
                break;

            case DIPLOMACY_FOREIGN:
                if (army < 35) weight *= 1.3f;
                if (money < 35) weight *= 1.2f;
                break;

            case OMENS_SUPERSTITION:
                if (religion > 70) weight *= 1.6f;
                if (people < 35) weight *= 1.1f;
                break;

            case SUCCESSION_CROWN:
                if (turn > 40) weight *= 1.4f;
                if (people < 40) weight *= 1.2f;
                if (army < 40) weight *= 1.2f;
                break;
        }

        return weight;
    }

    private EventDefinition fallback() {
        for (EventDefinition event : allEvents) {
            if (event.rarity == EventRarity.COMMON) {
                return event;
            }
        }
        return allEvents.first();
    }

    private static class ScoredEvent {
        private final EventDefinition event;
        private final float score;

        private ScoredEvent(EventDefinition event, float score) {
            this.event = event;
            this.score = score;
        }
    }
}
