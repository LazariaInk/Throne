package com.lazar.engine.flags;

import com.badlogic.gdx.utils.Array;
import com.lazar.model.EventAction;
import com.lazar.model.WarEncounterDefinition;

public class OptionEffects {
    public Array<FlagEffect> flags;
    public String nextArcStage;
    public Boolean resolveArc;
    public EventAction action;
    public WarEncounterDefinition war;

    public OptionEffects() {
    }
}
