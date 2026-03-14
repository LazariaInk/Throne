package com.lazar.engine;

import com.lazar.logic.GameStats;

public class GameOverEvaluator {

    public GameOverType evaluate(GameStats stats) {
        if (stats.getArmy() <= 0) return GameOverType.INVASION;
        if (stats.getArmy() >= 100) return GameOverType.MILITARY_COUP;

        if (stats.getMoney() <= 0) return GameOverType.BANKRUPTCY;
        if (stats.getMoney() >= 100) return GameOverType.OLIGARCHY;

        if (stats.getReligion() <= 0) return GameOverType.RELIGIOUS_REVOLT;
        if (stats.getReligion() >= 100) return GameOverType.THEOCRACY;

        if (stats.getPeople() <= 0) return GameOverType.DEAD_KINGDOM;
        if (stats.getPeople() >= 100) return GameOverType.OVERCROWDED_COLLAPSE;

        return null;
    }
}
