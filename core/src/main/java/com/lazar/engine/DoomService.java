package com.lazar.engine;

import com.badlogic.gdx.math.MathUtils;
import com.lazar.model.Consequence;
import com.lazar.model.EventDefinition;
import com.lazar.model.EventRarity;

public class DoomService {

    public Consequence applyTurnScaling(GameRunState state, EventDefinition event, Consequence original) {
        if (original == null) {
            return null;
        }

        int religion = original.religion;
        int population = original.population;
        int army = original.army;
        int money = original.money;

        if (state.getTurn() >= 40) {
            religion = amplifyIfSmall(religion, 0.20f);
            population = amplifyIfSmall(population, 0.20f);
            army = amplifyIfSmall(army, 0.20f);
            money = amplifyIfSmall(money, 0.20f);
        }

        if (state.getTurn() >= 80 && event.rarity == EventRarity.CRISIS) {
            switch (event.category) {
                case CHURCH_FAITH:
                    religion += religion <= 0 ? -1 : 1;
                    break;
                case PEASANTRY_COMMON_FOLK:
                case PLAGUE_HEALTH:
                    population += population <= 0 ? -1 : 1;
                    break;
                case ARMY_WAR:
                    army += army <= 0 ? -1 : 1;
                    break;
                case TREASURY_TRADE:
                    money += money <= 0 ? -1 : 1;
                    break;
                default:
                    break;
            }
        }

        return new Consequence(
            original.title,
            original.text,
            religion,
            population,
            army,
            money
        );
    }

    private int amplifyIfSmall(int value, float chance) {
        if (value == 1 && MathUtils.randomBoolean(chance)) return 2;
        if (value == -1 && MathUtils.randomBoolean(chance)) return -2;
        return value;
    }
}
