package com.lazar.model;

public class Consequence {
    public final String title;
    public final String text;
    public final int religion;
    public final int population;
    public final int army;
    public final int money;

    public Consequence(String title, String text, int religion, int population, int army, int money) {
        this.title = title;
        this.text = text;
        this.religion = religion;
        this.population = population;
        this.army = army;
        this.money = money;
    }
}
