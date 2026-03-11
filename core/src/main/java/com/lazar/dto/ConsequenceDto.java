package com.lazar.dto;

public class ConsequenceDto {
    public String title;
    public String text;
    public int religion;
    public int population;
    public int army;
    public int money;

    public ConsequenceDto() {
    }

    public ConsequenceDto(String title, String text, int religion, int population, int army, int money) {
        this.title = title;
        this.text = text;
        this.religion = religion;
        this.population = population;
        this.army = army;
        this.money = money;
    }
}
