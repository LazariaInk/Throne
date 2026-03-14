package com.lazar.engine.conditions;

import com.badlogic.gdx.utils.Array;
import com.lazar.engine.GameRunState;
import com.lazar.engine.arc.StoryArcState;
import com.lazar.model.ArcStage;
import com.lazar.model.DecisionOption;

public class ConditionEvaluator {

    public boolean allMatch(Array<EventCondition> conditions, GameRunState state) {
        if (conditions == null || conditions.size == 0) {
            return true;
        }

        for (EventCondition condition : conditions) {
            if (!matches(condition, state)) {
                return false;
            }
        }

        return true;
    }

    public boolean anyMatch(Array<EventCondition> conditions, GameRunState state) {
        if (conditions == null || conditions.size == 0) {
            return false;
        }

        for (EventCondition condition : conditions) {
            if (matches(condition, state)) {
                return true;
            }
        }

        return false;
    }

    public boolean matches(EventCondition c, GameRunState state) {
        if (c == null || c.type == null) {
            return false;
        }

        switch (c.type) {
            case STAT:
                return evaluateStat(c, state);

            case FLAG:
                return state.hasFlag(c.flag) == Boolean.TRUE.equals(c.expected);

            case LAST_DECISION:
                if (state.getLastEventId() == null || state.getLastDecisionOption() == null) {
                    return false;
                }
                return c.eventId != null
                    && c.eventId.equals(state.getLastEventId())
                    && c.option != null
                    && state.getLastDecisionOption() == DecisionOption.valueOf(c.option);

            case ARC_STAGE:
                StoryArcState arc = state.getArc(c.arcId);
                if (arc == null || c.arcStage == null) {
                    return false;
                }
                return arc.currentStage == ArcStage.valueOf(c.arcStage);

            case TURN_AT_LEAST:
                return c.value != null && state.getTurn() >= c.value;

            default:
                return false;
        }
    }

    private boolean evaluateStat(EventCondition c, GameRunState state) {
        if (c.stat == null || c.op == null || c.value == null) {
            return false;
        }

        int current;
        switch (c.stat) {
            case RELIGION:
                current = state.getStats().getReligion();
                break;
            case POPULATION:
                current = state.getStats().getPeople();
                break;
            case ARMY:
                current = state.getStats().getArmy();
                break;
            case MONEY:
                current = state.getStats().getMoney();
                break;
            default:
                return false;
        }

        switch (c.op) {
            case LT: return current < c.value;
            case LTE: return current <= c.value;
            case GT: return current > c.value;
            case GTE: return current >= c.value;
            case EQ: return current == c.value;
            case NEQ: return current != c.value;
            default: return false;
        }
    }
}
