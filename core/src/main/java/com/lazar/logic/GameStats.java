package com.lazar.logic;

import com.badlogic.gdx.math.MathUtils;
import com.lazar.model.Consequence;

public class GameStats {

    private int religion = 50;
    private int people = 50;
    private int army = 50;
    private int money = 50;

    public int getReligion() {
        return religion;
    }

    public int getPeople() {
        return people;
    }

    public int getArmy() {
        return army;
    }

    public int getMoney() {
        return money;
    }

    public void apply(Consequence consequence) {
        if (consequence == null) {
            return;
        }

        religion = clampStat(religion + consequence.religion * 12);
        people = clampStat(people + consequence.population * 12);
        army = clampStat(army + consequence.army * 12);
        money = clampStat(money + consequence.money * 12);
    }

    private int clampStat(int value) {
        return MathUtils.clamp(value, 0, 100);
    }
}
