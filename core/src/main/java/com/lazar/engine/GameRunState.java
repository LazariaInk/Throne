package com.lazar.engine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.lazar.engine.arc.StoryArcState;
import com.lazar.logic.GameStats;
import com.lazar.model.DecisionOption;
import com.lazar.model.Consequence;
import com.lazar.model.WarEncounterDefinition;
import com.lazar.logic.WarBattle;

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
    private WarEncounterDefinition pendingWar;

    public static class Snapshot {
        public int religion, people, army, money, turn;
        public String[] flags, recentEvents;
        public Array<SavedArc> arcs = new Array<>();
        public Array<SavedCooldown> cooldowns = new Array<>();
        public String lastEventId;
        public DecisionOption lastDecisionOption;
        public WarEncounterDefinition pendingWar;
    }

    public static class SavedArc {
        public String id;
        public com.lazar.model.ArcStage stage;
        public boolean active, resolved;
        public int stageEnteredTurn;
    }

    public static class SavedCooldown {
        public String id;
        public int until;
    }

    public Snapshot snapshot() {
        Snapshot saved = new Snapshot();
        saved.religion = stats.getReligion(); saved.people = stats.getPeople();
        saved.army = stats.getArmy(); saved.money = stats.getMoney();
        saved.turn = turn;
        saved.flags = flags.toArray(new String[0]);
        saved.recentEvents = recentEventIds.toArray(String.class);
        saved.lastEventId = lastEventId;
        saved.lastDecisionOption = lastDecisionOption;
        saved.pendingWar = pendingWar;
        for (StoryArcState arc : arcs.values()) {
            SavedArc item = new SavedArc();
            item.id = arc.arcId; item.stage = arc.currentStage;
            item.active = arc.active; item.resolved = arc.resolved;
            item.stageEnteredTurn = arc.stageEnteredTurn;
            saved.arcs.add(item);
        }
        for (ObjectIntMap.Entry<String> entry : cooldownUntilTurn) {
            SavedCooldown item = new SavedCooldown();
            item.id = entry.key; item.until = entry.value;
            saved.cooldowns.add(item);
        }
        return saved;
    }

    public static GameRunState restore(Snapshot saved) {
        if (saved == null || saved.turn < 1 || saved.flags == null || saved.recentEvents == null
            || saved.arcs == null || saved.cooldowns == null || saved.recentEvents.length > 10) {
            throw new IllegalArgumentException("Invalid saved run state");
        }
        GameRunState state = new GameRunState();
        state.stats.restore(saved.religion, saved.people, saved.army, saved.money);
        state.turn = saved.turn;
        for (String flag : saved.flags) state.addFlag(flag);
        for (String id : saved.recentEvents) state.rememberEvent(id);
        for (SavedArc arc : saved.arcs) {
            if (arc.id == null || arc.stage == null) throw new IllegalArgumentException("Invalid saved arc");
            StoryArcState restored = state.getOrCreateArc(arc.id);
            restored.currentStage = arc.stage; restored.active = arc.active; restored.resolved = arc.resolved;
            restored.stageEnteredTurn = arc.stageEnteredTurn > 0 ? arc.stageEnteredTurn : saved.turn;
        }
        for (SavedCooldown cooldown : saved.cooldowns) {
            if (cooldown.id == null) throw new IllegalArgumentException("Invalid saved cooldown");
            state.cooldownUntilTurn.put(cooldown.id, cooldown.until);
        }
        state.setLastDecision(saved.lastEventId, saved.lastDecisionOption);
        if (saved.pendingWar != null) state.queueWar(saved.pendingWar);
        return state;
    }

    public boolean hasPendingWar() { return pendingWar != null; }

    public void queueWar(WarEncounterDefinition war) {
        if (pendingWar != null) throw new IllegalStateException("A war is already pending");
        if (war == null || war.victory == null || war.draw == null || war.defeat == null) {
            throw new IllegalArgumentException("War requires all three outcomes");
        }
        pendingWar = war;
    }

    /** Consumes the pending encounter before applying it, preventing duplicate rewards. */
    public Consequence resolveWar(int wins) {
        if (wins < 0 || wins > WarBattle.TOTAL_ROUNDS) throw new IllegalArgumentException("Invalid war score");
        if (pendingWar == null) return null;
        Consequence consequence = pendingWar.consequenceFor(wins);
        pendingWar = null;
        stats.apply(consequence);
        return consequence;
    }

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
            state.stageEnteredTurn = turn;
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
