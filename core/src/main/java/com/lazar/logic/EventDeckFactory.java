package com.lazar.logic;

import com.badlogic.gdx.utils.Array;
import com.lazar.model.EventCard;

public class EventDeckFactory {

    public Array<EventCard> createDefaultDeck() {
        Array<EventCard> deck = new Array<>();

        deck.add(new EventCard(
            "event_001",
            "Turnir in piata cetatii",
            "Capitanul cere un turnir pentru moralul armatei, dar episcopul vrea post si rugaciune. Poporul asteapta hotararea ta, Majestate.",
            "images/events/tournament.png"
        ));

        deck.add(new EventCard(
            "event_002",
            "Negustorii cer scaderea taxelor",
            "Breslele sustin ca birurile sunt prea mari si incetinesc comertul. Vistiernicul spune ca aurul din tezaur scade de la o luna la alta.",
            "images/events/traders.png"
        ));

        deck.add(new EventCard(
            "event_003",
            "Seceta loveste satele din sud",
            "Taranii cer deschiderea granarelor regale. Sfetnicii te avertizeaza ca iarna viitoare poate fi si mai grea daca imparti prea mult acum.",
            "images/events/drought.png"
        ));

        return deck;
    }
}
