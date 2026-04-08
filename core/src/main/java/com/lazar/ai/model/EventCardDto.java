package com.lazar.ai.model;


public class EventCardDto {
    public String id;
    public String title;
    public String description;
    public String imagePath;

    public EventCardDto() {
    }

    public EventCardDto(String id, String title, String description, String imagePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
    }
}
