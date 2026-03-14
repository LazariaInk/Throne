package com.lazar.config;

import com.lazar.model.DecisionResolution;
import com.lazar.model.EventCard;

public interface DecisionResolver {

    void resolveDecision(EventCard event, String playerInput, Callback callback);

    interface Callback {
        void onSuccess(DecisionResolution resolution);
        void onError(String message);
    }
}
