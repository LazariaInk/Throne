package com.lazar;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;
import com.lazar.screens.MainMenuScreen;

public class StartGame extends Game {

    public static final String SETTINGS_PREFS = "empire-settings";

    @Override
    public void create() {
        applySavedSettings();
        setScreen(new MainMenuScreen(this));
    }

    private void applySavedSettings() {
        Preferences prefs = Gdx.app.getPreferences(SETTINGS_PREFS);

        boolean fullscreen = prefs.getBoolean("fullscreen", false);
        boolean vsync = prefs.getBoolean("vsync", true);

        Gdx.graphics.setVSync(vsync);

        if (fullscreen) {
            DisplayMode displayMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(displayMode);
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
    }
}
