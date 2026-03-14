package com.lazar.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.lazar.model.EventDefinition;

public class EventRepository {

    private final Json json;

    public EventRepository() {
        this.json = new Json();
        this.json.setIgnoreUnknownFields(true);
    }

    public Array<EventDefinition> loadFromJson(String internalPath) {
        FileHandle file = Gdx.files.internal(internalPath);
        String raw = file.readString("UTF-8");

        EventDefinitionList list = json.fromJson(EventDefinitionList.class, raw);
        if (list == null || list.events == null) {
            return new Array<>();
        }

        return list.events;
    }
}
