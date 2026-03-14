package com.lazar.logic;

import com.lazar.engine.GameOverType;

public class GameOverAssets {

    public static String getBackgroundPath(GameOverType type) {
        switch (type) {
            case INVASION:
                return "images/gameover/invasion.png";
            case MILITARY_COUP:
                return "images/gameover/military_coup.png";
            case BANKRUPTCY:
                return "images/gameover/bankruptcy.png";
            case OLIGARCHY:
                return "images/gameover/oligarchy.png";
            case RELIGIOUS_REVOLT:
                return "images/gameover/religious_revolt.png";
            case THEOCRACY:
                return "images/gameover/theocracy.png";
            case DEAD_KINGDOM:
                return "images/gameover/dead_kingdom.png";
            case OVERCROWDED_COLLAPSE:
                return "images/gameover/overcrowded_collapse.png";
            default:
                return "images/background.png";
        }
    }
}
