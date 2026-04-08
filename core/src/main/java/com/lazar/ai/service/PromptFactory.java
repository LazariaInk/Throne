package com.lazar.ai.service;

import com.lazar.ai.model.ConsequenceDto;
import com.lazar.ai.model.EventCardDto;

public class PromptFactory {

    public String buildSystemPrompt() {
        return "You are a strict classifier for a medieval kingdom decision game.\n\n"
            + "Your job is NOT to invent a new decision.\n"
            + "Your job is ONLY to classify the player's message as:\n"
            + "- A\n"
            + "- B\n"
            + "- C\n\n"
            + "Return ONLY valid JSON.\n"
            + "No markdown.\n"
            + "No explanations outside JSON.\n\n"
            + "Valid output format:\n"
            + "{\"decision\":\"A\",\"narrative\":\"...\",\"reason\":\"...\"}\n"
            + "or\n"
            + "{\"decision\":\"B\",\"narrative\":\"...\",\"reason\":\"...\"}\n"
            + "or\n"
            + "{\"decision\":\"C\",\"narrative\":\"...\",\"reason\":\"...\"}\n\n"
            + "Classification rules:\n"
            + "1. Choose A only if the player clearly supports option A.\n"
            + "2. Choose B only if the player clearly supports option B.\n"
            + "3. Choose C if the message is unclear, ambiguous, contradictory, absurd, random, off-topic, trolling, modern-unrelated, or outside the context of the event.\n"
            + "4. If unsure, choose C.\n"
            + "5. Do not reward vague diplomacy unless it clearly matches A or B.\n"
            + "6. narrative must be in Romanian, short, medieval in tone.\n"
            + "7. reason must be in Romanian and short, also in the medieval tone.\n"
            + "8. Never output anything except the JSON object.";
    }

    public String buildUserPrompt(
        EventCardDto event,
        ConsequenceDto optionA,
        ConsequenceDto optionB,
        ConsequenceDto optionC,
        String playerInput
    ) {
        return String.format(
            "Analizeaza raspunsul jucatorului pentru acest eveniment.\n\n"

                + "EVENIMENT\n"
                + "id: %s\n"
                + "titlu: %s\n"
                + "descriere: %s\n\n"

                + "OPTIUNEA A\n"
                + "titlu: %s\n"
                + "text: %s\n"
                + "efecte: religion=%+d, population=%+d, army=%+d, money=%+d\n\n"

                + "OPTIUNEA B\n"
                + "titlu: %s\n"
                + "text: %s\n"
                + "efecte: religion=%+d, population=%+d, army=%+d, money=%+d\n\n"

                + "OPTIUNEA C\n"
                + "titlu: %s\n"
                + "text: %s\n"
                + "efecte: religion=%+d, population=%+d, army=%+d, money=%+d\n\n"

                + "MESAJUL JUCATORULUI\n"
                + "%s\n\n"

                + "Alege strict intentia jucatorului:\n"
                + "- A = sustine clar prima directie\n"
                + "- B = sustine clar a doua directie\n"
                + "- C = raspuns neclar, ambiguu, in afara contextului, absurd sau fara legatura\n\n"
                + "Daca raspunsul nu este clar aliniat cu A sau B, alege C.",

            safe(event.id),
            safe(event.title),
            safe(event.description),

            safe(optionA.title),
            safe(optionA.text),
            optionA.religion,
            optionA.population,
            optionA.army,
            optionA.money,

            safe(optionB.title),
            safe(optionB.text),
            optionB.religion,
            optionB.population,
            optionB.army,
            optionB.money,

            safe(optionC.title),
            safe(optionC.text),
            optionC.religion,
            optionC.population,
            optionC.army,
            optionC.money,

            safe(playerInput)
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
