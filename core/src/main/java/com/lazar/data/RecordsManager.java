package com.lazar.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class RecordsManager {

    private static final String PREFS_NAME = "empire_records";
    private static final String KEY_RECORDS = "records";

    private final Preferences prefs;
    private final Json json;

    public RecordsManager() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.json = new Json();
    }

    public Array<RecordEntry> loadRecords() {
        String raw = prefs.getString(KEY_RECORDS, "");
        if (raw == null || raw.isEmpty()) {
            return new Array<>();
        }

        try {
            return json.fromJson(Array.class, RecordEntry.class, raw);
        } catch (Exception e) {
            return new Array<>();
        }
    }

    public void saveRecord(RecordEntry entry) {
        Array<RecordEntry> records = loadRecords();
        records.add(entry);

        records.sort((a, b) -> {
            float ay = Float.parseFloat(a.yearsRuled);
            float by = Float.parseFloat(b.yearsRuled);
            return Float.compare(by, ay);
        });

        while (records.size > 20) {
            records.pop();
        }

        prefs.putString(KEY_RECORDS, json.toJson(records));
        prefs.flush();
    }
}
