package com.lazar.config;

import com.lazar.dto.ConsequenceDto;
import com.lazar.dto.EventCardDto;
import com.lazar.dto.ResolveDecisionRequest;
import com.lazar.model.DecisionOutcomeData;
import com.lazar.model.EventCard;

public class DecisionRequestMapper {

    public ResolveDecisionRequest toRequest(EventCard event, String playerInput) {
        return new ResolveDecisionRequest(
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
