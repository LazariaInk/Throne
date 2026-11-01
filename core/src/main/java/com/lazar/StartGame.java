package com.lazar;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;
import com.lazar.config.LocalizationManager;
import com.lazar.config.SoundManager;
import com.lazar.ai.service.AiSettings;
import com.lazar.screens.MainMenuScreen;

public class StartGame extends Game {

    public static final String SETTINGS_PREFS = "empire-settings";
    private SoundManager soundManager;
    private AiSettings aiSettings;

    @Override
    public void create() {
        applySavedSettings();
        loadSavedLanguage();
        aiSettings = new AiSettings(Gdx.app.getPreferences(AiSettings.PREFS_NAME),
            Gdx.files.internal("ai-config.json").readString("UTF-8"), System.getenv("OPENAI_API_KEY"));
        soundManager = new SoundManager();
        soundManager.init();
        setScreen(new MainMenuScreen(this));
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public AiSettings getAiSettings() { return aiSettings; }

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
        if (aiSettings != null) aiSettings.clearSessionKey();
        super.dispose();
        if (screen != null) {
            screen.dispose();
        }
        if (soundManager != null) {
            soundManager.dispose();
        }
    }
}
