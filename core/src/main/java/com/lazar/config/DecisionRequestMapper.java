package com.lazar.config;

import com.lazar.dto.EventCardDto;
import com.lazar.dto.ResolveDecisionRequest;
import com.lazar.logic.EventDecisionSet;
import com.lazar.model.EventCard;

public class DecisionRequestMapper {

    public ResolveDecisionRequest toRequest(EventCard event, EventDecisionSet decisionSet, String playerInput) {
        return new ResolveDecisionRequest(
            new EventCardDto(
                event.id,
                event.title,
                event.description,
                event.imagePath
            ),
            decisionSet.getOptionA(),
            decisionSet.getOptionB(),
            decisionSet.getOptionC(),
            playerInput
        );
    }
}
