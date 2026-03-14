package com.lazar.engine;

import com.badlogic.gdx.utils.Array;
import com.lazar.engine.arc.StoryArcState;
import com.lazar.engine.flags.FlagEffect;
import com.lazar.engine.flags.OptionEffects;
import com.lazar.model.ArcStage;
import com.lazar.model.DecisionOption;
import com.lazar.model.DecisionResolution;
import com.lazar.model.EventDefinition;

public class EventResolutionProcessor {

    private final DoomService doomService = new DoomService();

    public DecisionResolution process(GameRunState state, EventDefinition event, DecisionResolution resolution) {
        DecisionResolution adjusted = new DecisionResolution(
            resolution.option,
            doomService.applyTurnScaling(state, event, resolution.consequence)
        );

        state.getStats().apply(adjusted.consequence);
        state.setLastDecision(event.id, adjusted.option);
        state.rememberEvent(event.id);
        state.putOnCooldown(event.id, event.cooldownTurns);

        if (event.arcId != null) {
            state.getOrCreateArc(event.arcId);
        }

        OptionEffects effects = effectsForOption(event, adjusted.option);
        applyOptionEffects(state, event, effects);

        state.nextTurn();
        return adjusted;
    }

    private OptionEffects effectsForOption(EventDefinition event, DecisionOption option) {
        switch (option) {
            case A: return event.onA;
            case B: return event.onB;
            case C: return event.onC;
            default: return null;
        }
    }

    private void applyOptionEffects(GameRunState state, EventDefinition event, OptionEffects effects) {
        if (effects == null) {
            autoAdvanceArc(state, event);
            return;
        }

        applyFlags(state, effects.flags);
        applyArcMutation(state, event, effects);
    }

    private void applyFlags(GameRunState state, Array<FlagEffect> flags) {
        if (flags == null) {
            return;
        }

        for (FlagEffect flagEffect : flags) {
            if (flagEffect == null || flagEffect.flag == null || flagEffect.action == null) {
                continue;
            }

            if ("ADD".equalsIgnoreCase(flagEffect.action)) {
                state.addFlag(flagEffect.flag);
            } else if ("REMOVE".equalsIgnoreCase(flagEffect.action)) {
                state.removeFlag(flagEffect.flag);
            }
        }
    }

    private void applyArcMutation(GameRunState state, EventDefinition event, OptionEffects effects) {
        if (event.arcId == null) {
            return;
        }

        StoryArcState arc = state.getOrCreateArc(event.arcId);

        if (Boolean.TRUE.equals(effects.resolveArc)) {
            arc.resolved = true;
            arc.active = false;
            arc.currentStage = ArcStage.OUTCOME;
            return;
        }

        if (effects.nextArcStage != null) {
            arc.currentStage = ArcStage.valueOf(effects.nextArcStage);
            arc.active = true;
            return;
        }

        autoAdvanceArc(state, event);
    }

    private void autoAdvanceArc(GameRunState state, EventDefinition event) {
        if (event.arcId == null) {
            return;
        }

        StoryArcState arc = state.getOrCreateArc(event.arcId);

        switch (event.arcStage) {
            case SEED:
                arc.currentStage = ArcStage.COMMITMENT;
                break;
            case COMMITMENT:
                arc.currentStage = ArcStage.ESCALATION;
                break;
            case ESCALATION:
                arc.currentStage = ArcStage.CRISIS;
                break;
            case CRISIS:
                arc.currentStage = ArcStage.OUTCOME;
                break;
            case OUTCOME:
                arc.currentStage = ArcStage.OUTCOME;
                arc.active = false;
                arc.resolved = true;
                break;
        }
    }
}
