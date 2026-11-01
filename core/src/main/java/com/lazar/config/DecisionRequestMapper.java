package com.lazar.config;

import com.lazar.ai.model.ConsequenceDto;
import com.lazar.ai.model.EventCardDto;
import com.lazar.ai.model.ResolveDecisionRequest;
import com.lazar.model.DecisionOutcomeData;
import com.lazar.model.EventCard;

public class DecisionRequestMapper {

    public ResolveDecisionRequest toRequest(EventCard event, String playerInput) {
        ResolveDecisionRequest request = new ResolveDecisionRequest(
            new EventCardDto(
                event.id,
                event.title,
                event.description,
                event.imagePath
            ),
            toDto(event.decisions != null ? event.decisions.A : null),
            toDto(event.decisions != null ? event.decisions.B : null),
            toDto(event.decisions != null ? event.decisions.C : null),
            playerInput
        );
        request.actionA = event.actionA == null ? "NONE" : event.actionA.name();
        request.actionB = event.actionB == null ? "NONE" : event.actionB.name();
        request.actionC = event.actionC == null ? "NONE" : event.actionC.name();
        return request;
    }

    private ConsequenceDto toDto(DecisionOutcomeData data) {
        if (data == null) {
            return new ConsequenceDto(
                "Hotarare lipsa",
                "Evenimentul nu are o consecinta definita corect.",
                -1, -1, -1, -1
            );
        }

        return new ConsequenceDto(
            data.title,
            data.text,
            data.religion,
            data.population,
            data.army,
            data.money
        );
    }
}
