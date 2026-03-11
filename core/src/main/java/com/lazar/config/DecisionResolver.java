package com.lazar.config;

import com.lazar.model.Consequence;
import com.lazar.model.EventCard;

public interface DecisionResolver {

    void resolveDecision(EventCard event, String playerInput, Callback callback);

    interface Callback {
        void onSuccess(Consequence consequence);
        void onError(String message);
    }
}
