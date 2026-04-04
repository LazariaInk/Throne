package com.lazar;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;
import com.lazar.config.LocalizationManager;
import com.lazar.config.SoundManager;
import com.lazar.screens.MainMenuScreen;

public class StartGame extends Game {

    public static final String SETTINGS_PREFS = "empire-settings";
    private SoundManager soundManager;

    @Override
    public void create() {
        applySavedSettings();
        loadSavedLanguage();
        soundManager = new SoundManager();
        soundManager.init();
        setScreen(new MainMenuScreen(this));
    }

    public SoundManager getSoundManager() {
        return soundManager;
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

    private void loadSavedLanguage() {
        Preferences prefs = Gdx.app.getPreferences(SETTINGS_PREFS);
        String language = prefs.getString("language", "en");
        LocalizationManager.loadLanguage(language);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (screen != null) {
            screen.dispose();
        }
        if (soundManager != null) {
            soundManager.dispose();
        }
    }
}
