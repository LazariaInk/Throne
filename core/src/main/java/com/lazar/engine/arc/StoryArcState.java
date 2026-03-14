package com.lazar.engine.arc;

import com.lazar.model.ArcStage;

public class StoryArcState {
    public final String arcId;
    public ArcStage currentStage;
    public boolean active;
    public boolean resolved;

    public StoryArcState(String arcId) {
        this.arcId = arcId;
        this.currentStage = ArcStage.SEED;
        this.active = true;
        this.resolved = false;
    }
}
