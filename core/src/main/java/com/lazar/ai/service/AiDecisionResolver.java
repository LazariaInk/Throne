package com.lazar.ai.service;

import com.badlogic.gdx.Gdx;
import com.lazar.config.LocalizationManager;
import com.lazar.ai.model.ResolveDecisionRequest;
import com.lazar.ai.model.ResolveDecisionResponse;
import com.lazar.config.ConsequenceMapper;
import com.lazar.config.DecisionRequestMapper;
import com.lazar.config.DecisionResolver;
import com.lazar.model.Consequence;
import com.lazar.model.DecisionOption;
import com.lazar.model.DecisionResolution;
import com.lazar.model.EventCard;


public class AiDecisionResolver implements DecisionResolver {

    private final DecisionRequestMapper requestMapper;
    private final ConsequenceMapper consequenceMapper;
    private final GameDecisionService gameDecisionService;

    public AiDecisionResolver(GameDecisionService gameDecisionService) {
        this.gameDecisionService = gameDecisionService;
        this.requestMapper = new DecisionRequestMapper();
        this.consequenceMapper = new ConsequenceMapper();
    }

    @Override
    public void resolveDecision(EventCard event, String playerInput, Callback callback) {
        ResolveDecisionRequest requestBody = requestMapper.toRequest(event, playerInput);

        Thread worker = new Thread(() -> {
            try {
                ResolveDecisionResponse response = gameDecisionService.resolve(requestBody);

                Gdx.app.postRunnable(() -> {
                    if (response == null || response.consequence == null || response.resolvedOption == null) {
                        callback.onError(LocalizationManager.get("ai.error.invalid"));
                        return;
                    }

                    Consequence consequence = consequenceMapper.fromDto(response.consequence);
                    if (consequence == null) {
                        callback.onError(LocalizationManager.get("ai.error.invalid"));
                        return;
                    }

                    try {
                        DecisionOption option = DecisionOption.valueOf(response.resolvedOption.trim().toUpperCase());
                        callback.onSuccess(new DecisionResolution(option, consequence));
                    } catch (Exception ex) {
                        callback.onError(LocalizationManager.get("ai.error.classification"));
                    }
                });

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> {
                    callback.onError(AiServiceException.messageFor(e));
                });
            }
        }, "throne-ai-decision");
        worker.setDaemon(true);
        worker.start();
    }
}
