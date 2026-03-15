package com.lazar.model;

public class EventCard {
    public String id;
    public String title;
    public String description;
    public String imagePath;
    public EventDecisionData decisions;

    public EventCard() {
    }

    public EventCard(String id, String title, String description, String imagePath) {
        this(id, title, description, imagePath, null);
    }

    public EventCard(String id, String title, String description, String imagePath, EventDecisionData decisions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.decisions = decisions;
    }
}
