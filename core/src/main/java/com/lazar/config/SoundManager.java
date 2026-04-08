package com.lazar.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.lazar.StartGame;

public class SoundManager {

    private static final int DEFAULT_MUSIC_VOLUME = 70;
    private static final int DEFAULT_EFFECTS_VOLUME = 70;

    private final Preferences prefs;

    private Music gameMusic;
    private Music menuMusic;
    private Music currentMusic;

    private Sound cardSwapSound;

    private int musicVolume;
    private int effectsVolume;

    public SoundManager() {
        this.prefs = Gdx.app.getPreferences(StartGame.SETTINGS_PREFS);
    }

    public void init() {
        if (gameMusic == null) {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/background-music.mp3"));
            gameMusic.setLooping(true);
        }

        if (menuMusic == null) {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/menu.mp3"));
            menuMusic.setLooping(true);
        }

        if (cardSwapSound == null) {
            cardSwapSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card-swap.mp3"));
        }

        loadSettings();
        applyVolumes();
    }

    public void loadSettings() {
        musicVolume = clamp(prefs.getInteger("musicVolume", DEFAULT_MUSIC_VOLUME));
        effectsVolume = clamp(prefs.getInteger("effectsVolume", DEFAULT_EFFECTS_VOLUME));
    }

    public void reloadSettings() {
        loadSettings();
        applyVolumes();
    }

    public void setMusicVolume(int volume) {
        musicVolume = clamp(volume);
        prefs.putInteger("musicVolume", musicVolume);
        prefs.flush();
        applyMusicVolume();
    }

    public void setEffectsVolume(int volume) {
        effectsVolume = clamp(volume);
        prefs.putInteger("effectsVolume", effectsVolume);
        prefs.flush();
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public int getEffectsVolume() {
        return effectsVolume;
    }

    public float getMusicVolumeFloat() {
        return musicVolume / 100f;
    }

    public float getEffectsVolumeFloat() {
        return effectsVolume / 100f;
    }

    public void playGameMusic() {
        if (gameMusic == null) {
            init();
        }
        switchMusic(gameMusic);
    }

    public void playMenuMusic() {
        if (menuMusic == null) {
            init();
        }
        switchMusic(menuMusic);
    }

    private void switchMusic(Music targetMusic) {
        if (targetMusic == null) return;

        if (currentMusic == targetMusic) {
            applyMusicVolume();
            if (musicVolume > 0 && !currentMusic.isPlaying()) {
                currentMusic.play();
            }
            return;
        }

        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }

        currentMusic = targetMusic;
        currentMusic.setLooping(true);
        currentMusic.setVolume(getMusicVolumeFloat());

        if (musicVolume > 0) {
            currentMusic.play();
        }
    }

    public void pauseBackgroundMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    public void stopBackgroundMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void playCardSwap() {
        if (cardSwapSound == null) {
            init();
        }

        if (effectsVolume <= 0) {
            return;
        }

        cardSwapSound.play(getEffectsVolumeFloat());
    }

    public void applyVolumes() {
        applyMusicVolume();
    }

    private void applyMusicVolume() {
        if (currentMusic != null) {
            currentMusic.setVolume(getMusicVolumeFloat());

            if (musicVolume <= 0 && currentMusic.isPlaying()) {
                currentMusic.pause();
            } else if (musicVolume > 0 && !currentMusic.isPlaying()) {
                currentMusic.play();
            }
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public void dispose() {
        if (gameMusic != null) {
            gameMusic.dispose();
            gameMusic = null;
        }

        if (menuMusic != null) {
            menuMusic.dispose();
            menuMusic = null;
        }

        currentMusic = null;

        if (cardSwapSound != null) {
            cardSwapSound.dispose();
            cardSwapSound = null;
        }
    }
}
