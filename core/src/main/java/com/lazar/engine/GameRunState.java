package com.lazar.engine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.lazar.engine.arc.StoryArcState;
import com.lazar.logic.GameStats;
import com.lazar.model.DecisionOption;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameRunState {

    private final GameStats stats = new GameStats();
    private final Set<String> flags = new HashSet<>();
    private final Map<String, StoryArcState> arcs = new HashMap<>();
    private final ObjectIntMap<String> cooldownUntilTurn = new ObjectIntMap<>();
    private final Array<String> recentEventIds = new Array<>();

    private int turn = 1;

    private String lastEventId;
    private DecisionOption lastDecisionOption;

    public GameStats getStats() {
        return stats;
    }

    public int getTurn() {
        return turn;
    }

    public void nextTurn() {
        turn++;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public void addFlag(String flag) {
        if (flag != null && !flag.trim().isEmpty()) {
            flags.add(flag);
        }
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
    }

    public Set<String> getFlags() {
        return flags;
    }

    public StoryArcState getArc(String arcId) {
        return arcs.get(arcId);
    }

    public StoryArcState getOrCreateArc(String arcId) {
        StoryArcState state = arcs.get(arcId);
        if (state == null) {
            state = new StoryArcState(arcId);
            arcs.put(arcId, state);
        }
        return state;
    }

    public int activeArcCount() {
        int count = 0;
        for (StoryArcState arc : arcs.values()) {
            if (arc.active && !arc.resolved) {
                count++;
            }
        }
        return count;
    }

    public boolean hasArcInCrisis() {
        for (StoryArcState arc : arcs.values()) {
            if (arc.active && !arc.resolved && arc.currentStage.name().equals("CRISIS")) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnCooldown(String eventId) {
        return turn < cooldownUntilTurn.get(eventId, 0);
    }

    public void putOnCooldown(String eventId, int turns) {
        if (turns > 0) {
            cooldownUntilTurn.put(eventId, turn + turns);
        }
    }

    public void rememberEvent(String eventId) {
        recentEventIds.add(eventId);
        while (recentEventIds.size > 10) {
            recentEventIds.removeIndex(0);
        }
    }

    public boolean seenRecently(String eventId) {
        for (String seen : recentEventIds) {
            if (seen.equals(eventId)) {
                return true;
            }
        }
        return false;
    }

    public void setLastDecision(String eventId, DecisionOption option) {
        this.lastEventId = eventId;
        this.lastDecisionOption = option;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public DecisionOption getLastDecisionOption() {
        return lastDecisionOption;
    }
}
