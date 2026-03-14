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

        religion = clamp(religion + consequence.religion*5);
        people = clamp(people + consequence.population*5);
        army = clamp(army + consequence.army*5);
        money = clamp(money + consequence.money*5);
    }

    public boolean isGameOver() {
        return religion <= 0 || religion >= 100
            || people <= 0 || people >= 100
            || army <= 0 || army >= 100
            || money <= 0 || money >= 100;
    }

    public float dangerReligion() {
        return danger(religion);
    }

    public float dangerPeople() {
        return danger(people);
    }

    public float dangerArmy() {
        return danger(army);
    }

    public float dangerMoney() {
        return danger(money);
    }

    public boolean hasCriticalStat() {
        return isCritical(religion) || isCritical(people) || isCritical(army) || isCritical(money);
    }

    private boolean isCritical(int value) {
        return value <= 19 || value >= 81;
    }

    private float danger(int stat) {
        float normalized = Math.abs(stat - 50f) / 50f;
        return normalized * normalized;
    }

    private int clamp(int value) {
        return MathUtils.clamp(value, 0, 100);
    }
}
