package com.lazar.data;

import com.lazar.engine.GameOverType;

public class GameOverContent {

    public static String title(GameOverType type) {
        switch (type) {
            case INVASION:
                return "The Kingdom Falls";
            case MILITARY_COUP:
                return "The Crown Is Seized";
            case BANKRUPTCY:
                return "The Treasury Is Empty";
            case OLIGARCHY:
                return "Gold Rules the Realm";
            case RELIGIOUS_REVOLT:
                return "Faith Turns to Fury";
            case THEOCRACY:
                return "The Church Takes the Throne";
            case DEAD_KINGDOM:
                return "A Silent Kingdom";
            case OVERCROWDED_COLLAPSE:
                return "The Realm Cannot Bear More";
            default:
                return "Game Over";
        }
    }

    public static String body(GameOverType type) {
        switch (type) {
            case INVASION:
                return "Your armies withered until the borders lay open. "
                    + "Foreign banners now rise above your cities, and your name is remembered "
                    + "not as a ruler, but as the king who left the gates unguarded.";

            case MILITARY_COUP:
                return "The army grew too strong to obey the crown. "
                    + "Your generals no longer asked for orders — they gave them. "
                    + "Steel decided what law could not, and your reign ended beneath marching boots.";

            case BANKRUPTCY:
                return "The treasury ran dry. Debts swallowed promises, soldiers went unpaid, "
                    + "and merchants abandoned your roads. A kingdom may survive famine or war for a time, "
                    + "but not empty coffers forever.";

            case OLIGARCHY:
                return "Wealth gathered into too few hands, and the throne became a decoration. "
                    + "The rich now command the realm through coin, contract, and quiet threats. "
                    + "You still wore the crown, but power had already been sold.";

            case RELIGIOUS_REVOLT:
                return "Faith collapsed into anger. Temples turned into rallying grounds, "
                    + "priests into agitators, and the people rose against a ruler they believed abandoned by the divine. "
                    + "The kingdom burned with holy fury.";

            case THEOCRACY:
                return "The Church grew beyond counsel and beyond restraint. "
                    + "Scripture replaced decree, clergy replaced ministers, and the throne bowed before the altar. "
                    + "Your kingdom endured, but it no longer belonged to kings.";

            case DEAD_KINGDOM:
                return "Too many died, too many fled, and too few remained. "
                    + "Fields emptied, markets fell silent, and even victory lost meaning in a land without people. "
                    + "A crown rules nothing when the kingdom itself has faded away.";

            case OVERCROWDED_COLLAPSE:
                return "The realm swelled beyond its means. Hunger spread, streets overflowed, "
                    + "and order cracked under the weight of too many lives and too little structure. "
                    + "What once looked like prosperity became collapse.";

            default:
                return "Your reign has come to an end.";
        }
    }
}
