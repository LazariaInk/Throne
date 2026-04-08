package com.lazar.config;

import com.lazar.ai.model.ConsequenceDto;
import com.lazar.model.Consequence;

public class ConsequenceMapper {

    public Consequence fromDto(ConsequenceDto dto) {
        if (dto == null) {
            return null;
        }

        return new Consequence(
            dto.title,
            dto.text,
            dto.religion,
            dto.population,
            dto.army,
            dto.money
        );
    }
}
