package com.lazar.engine;

import com.badlogic.gdx.utils.Array;
import com.lazar.config.DecisionResolver;
import com.lazar.data.EventRepository;
import com.lazar.data.SavedGame;
import com.lazar.logic.WarBattle;
import com.lazar.model.Consequence;
import com.lazar.model.DecisionOutcomeData;
import com.lazar.model.DecisionOption;
import com.lazar.engine.spawn.EventSpawnService;
import com.lazar.model.DecisionResolution;
import com.lazar.model.EventCard;
import com.lazar.model.EventDefinition;

public class GameEngine {

    private final GameRunState runState;
    private final EventSpawnService spawnService;
    private final EventResolutionProcessor resolutionProcessor;
    private final GameOverEvaluator gameOverEvaluator;
    private final Array<EventDefinition> allEvents;
    private int turnsSurvived = 0;
    private String emperorName;

    private EventDefinition currentEvent;
    private DecisionResolution lastResolution;
    private WarBattle.Snapshot savedBattle;

    public GameEngine() {
        this(new EventRepository().loadFromJson("data/events.json"), null);
    }

    public GameEngine(Array<EventDefinition> events, SavedGame saved) {
        this.runState = saved == null ? new GameRunState() : GameRunState.restore(saved.state);
        this.allEvents = events;
        this.spawnService = new EventSpawnService(allEvents);
        this.resolutionProcessor = new EventResolutionProcessor();
        this.gameOverEvaluator = new GameOverEvaluator();
        if (saved != null) {
            emperorName = saved.emperorName;
            turnsSurvived = saved.turnsSurvived;
            for (EventDefinition event : events) {
                if (event.id.equals(saved.currentEventId)) currentEvent = event;
            }
            if (currentEvent == null) throw new IllegalArgumentException("Saved event no longer exists");
            if (saved.consequence != null) {
                DecisionOutcomeData c = saved.consequence;
                lastResolution = new DecisionResolution(saved.resolvedOption,
                    new Consequence(c.title, c.text, c.religion, c.population, c.army, c.money));
            }
            savedBattle = saved.battle;
        }
    }

    public SavedGame snapshot() {
        if (currentEvent == null) throw new IllegalStateException("No current event to save");
        SavedGame saved = new SavedGame();
        saved.emperorName = emperorName;
        saved.turnsSurvived = turnsSurvived;
        saved.currentEventId = currentEvent.id;
        saved.state = runState.snapshot();
        saved.battle = savedBattle;
        if (lastResolution != null) {
            saved.resolvedOption = lastResolution.option;
            Consequence c = lastResolution.consequence;
            saved.consequence = new DecisionOutcomeData();
            saved.consequence.title = c.title; saved.consequence.text = c.text;
            saved.consequence.religion = c.religion; saved.consequence.population = c.population;
            saved.consequence.army = c.army; saved.consequence.money = c.money;
        }
        return saved;
    }

    public DecisionResolution getLastResolution() { return lastResolution; }
    public WarBattle.Snapshot getSavedBattle() { return savedBattle; }
    public void setSavedBattle(WarBattle.Snapshot saved) { savedBattle = saved; }

    public Consequence resolveWar(int wins) {
        Consequence outcome = runState.resolveWar(wins);
        if (outcome != null) {
            savedBattle = null;
            lastResolution = new DecisionResolution(lastResolution == null ? DecisionOption.B : lastResolution.option, outcome);
        }
        return outcome;
    }

    public GameRunState getRunState() {
        return runState;
    }

    public EventCard nextCard() {
        if (runState.hasPendingWar()) throw new IllegalStateException("Resolve the pending war before drawing a card");
        currentEvent = spawnService.drawNext(runState);
        lastResolution = null;
        savedBattle = null;
        return currentEvent.toEventCard();
    }

    public EventDefinition getCurrentEvent() {
        return currentEvent;
    }

    public void submitPlayerText(DecisionResolver resolver, String input, DecisionResolver.Callback callback) {
        if (runState.hasPendingWar()) {
            callback.onError("Bătălia trebuie încheiată înainte de următoarea hotărâre");
            return;
        }
        if (currentEvent == null) {
            callback.onError("Nu exista event curent");
            return;
        }
        if (lastResolution != null) {
            callback.onError("Hotărârea a fost deja aplicată");
            return;
        }

        resolver.resolveDecision(currentEvent.toEventCard(), input, new DecisionResolver.Callback() {
            @Override
            public void onSuccess(DecisionResolution resolution) {
                DecisionResolution adjusted = resolutionProcessor.process(runState, currentEvent, resolution);
                lastResolution = adjusted;
                callback.onSuccess(adjusted);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public GameOverType checkGameOver() {
        return gameOverEvaluator.evaluate(runState.getStats());
    }

    public void setEmperorName(String emperorName) {
        this.emperorName = emperorName;
    }

    public String getEmperorName() {
        return emperorName;
    }

    public int getTurnsSurvived() {
        return turnsSurvived;
    }

    public void onTurnCompleted() {
        turnsSurvived++;
    }

    public float getYearsRuled() {
        return turnsSurvived * 0.5f;
    }

    public String getYearsRuledText() {
        if (turnsSurvived % 2 == 0) {
            return String.valueOf(turnsSurvived / 2);
        }
        return turnsSurvived / 2 + ".5";
    }
}
