package com.lazar.engine;

import com.badlogic.gdx.utils.Array;
import com.lazar.config.DecisionResolver;
import com.lazar.data.EventRepository;
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

    public GameEngine() {
        this.runState = new GameRunState();
        this.allEvents = new EventRepository().loadFromJson("data/events.json");
        this.spawnService = new EventSpawnService(allEvents);
        this.resolutionProcessor = new EventResolutionProcessor();
        this.gameOverEvaluator = new GameOverEvaluator();
    }

    public GameRunState getRunState() {
        return runState;
    }

    public EventCard nextCard() {
        currentEvent = spawnService.drawNext(runState);
        return currentEvent.toEventCard();
    }

    public EventDefinition getCurrentEvent() {
        return currentEvent;
    }

    public void submitPlayerText(DecisionResolver resolver, String input, DecisionResolver.Callback callback) {
        if (currentEvent == null) {
            callback.onError("Nu exista event curent");
            return;
        }

        resolver.resolveDecision(currentEvent.toEventCard(), input, new DecisionResolver.Callback() {
            @Override
            public void onSuccess(DecisionResolution resolution) {
                DecisionResolution adjusted = resolutionProcessor.process(runState, currentEvent, resolution);
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
