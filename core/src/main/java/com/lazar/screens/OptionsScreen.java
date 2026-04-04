package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

import com.lazar.StartGame;
import com.lazar.config.LocalizationManager;
import com.lazar.config.SoundManager;

public class OptionsScreen extends BaseMenuScreen {
    private final MenuButton backButton = new MenuButton("");
    private final Rectangle fullscreenBox = new Rectangle();
    private final Rectangle vsyncBox = new Rectangle();
    private final Rectangle musicBarBounds = new Rectangle();
    private final Rectangle musicKnobBounds = new Rectangle();
    private final Rectangle effectsBarBounds = new Rectangle();
    private final Rectangle effectsKnobBounds = new Rectangle();
    private final Rectangle dropdownHeaderBounds = new Rectangle();
    private final Rectangle[] dropdownItemBounds = new Rectangle[LANGUAGES.length];
    private static final String[] LANGUAGES = {"en", "ru", "ro"};
    private static final String[] LANGUAGE_NAMES = {"English", "Русский", "Română"};
    private boolean dropdownOpen = false;
    private int selectedLanguageIndex = 0;
    private Preferences prefs;
    private boolean fullscreen;
    private boolean vsync;
    private int musicVolume;
    private int effectsVolume;
    private boolean draggingMusic = false;
    private boolean draggingEffects = false;
    private final Screen continueScreen;
    private SoundManager soundManager;

    public OptionsScreen(StartGame game, Screen continueScreen) {
        super(game);
        this.continueScreen = continueScreen;
        for (int i = 0; i < dropdownItemBounds.length; i++) {
            dropdownItemBounds[i] = new Rectangle();
        }
    }

    private int languageIndex(String code) {
        for (int i = 0; i < LANGUAGES.length; i++) {
            if (LANGUAGES[i].equals(code)) return i;
        }
        return 0;
    }

    private void applyLanguage(int index) {
        selectedLanguageIndex = index;
        String code = LANGUAGES[index];
        prefs.putString("language", code);
        prefs.flush();
        LocalizationManager.loadLanguage(code);
        backButton.text = LocalizationManager.get("button.back");
    }

    private void applyDisplayMode() {
        if (fullscreen) {
            DisplayMode displayMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(displayMode);
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
    }

    private void updateMusicFromX(float x) {
        float clamped = Math.max(musicBarBounds.x, Math.min(x, musicBarBounds.x + musicBarBounds.width));
        musicVolume = Math.round(((clamped - musicBarBounds.x) / musicBarBounds.width) * 100f);
    }

    private void updateEffectsFromX(float x) {
        float clamped = Math.max(effectsBarBounds.x, Math.min(x, effectsBarBounds.x + effectsBarBounds.width));
        effectsVolume = Math.round(((clamped - effectsBarBounds.x) / effectsBarBounds.width) * 100f);
    }

    private void save() {
        prefs.putBoolean("fullscreen", fullscreen);
        prefs.putBoolean("vsync", vsync);
        prefs.putInteger("musicVolume", musicVolume);
        prefs.putInteger("effectsVolume", effectsVolume);
        prefs.flush();

        if (soundManager != null) {
            soundManager.setMusicVolume(musicVolume);
            soundManager.setEffectsVolume(effectsVolume);
        }
    }

    private void goBackToMenu() {
        game.setScreen(new MainMenuScreen(game, continueScreen));
        dispose();
    }

    @Override
    protected void onShow() {
        prefs = Gdx.app.getPreferences(StartGame.SETTINGS_PREFS);
        soundManager = game.getSoundManager();

        fullscreen = prefs.getBoolean("fullscreen", false);
        vsync = prefs.getBoolean("vsync", true);
        musicVolume = prefs.getInteger("musicVolume", 70);
        effectsVolume = prefs.getInteger("effectsVolume", 70);

        String currentLanguage = prefs.getString("language", "en");
        LocalizationManager.loadLanguage(currentLanguage);
        selectedLanguageIndex = languageIndex(currentLanguage);
        backButton.text = LocalizationManager.get("button.back");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                viewport.unproject(touchPoint.set(screenX, screenY, 0f));
                float wx = touchPoint.x;
                float wy = touchPoint.y;

                if (dropdownOpen) {
                    for (int i = 0; i < dropdownItemBounds.length; i++) {
                        if (dropdownItemBounds[i].contains(wx, wy)) {
                            applyLanguage(i);
                            dropdownOpen = false;
                            return true;
                        }
                    }
                    dropdownOpen = false;
                    return true;
                }

                if (dropdownHeaderBounds.contains(wx, wy)) {
                    dropdownOpen = !dropdownOpen;
                    return true;
                }

                if (fullscreenBox.contains(wx, wy)) {
                    fullscreen = !fullscreen;
                    applyDisplayMode();
                    save();
                    return true;
                }

                if (vsyncBox.contains(wx, wy)) {
                    vsync = !vsync;
                    Gdx.graphics.setVSync(vsync);
                    save();
                    return true;
                }

                if (musicBarBounds.contains(wx, wy) || musicKnobBounds.contains(wx, wy)) {
                    draggingMusic = true;
                    updateMusicFromX(wx);
                    save();
                    return true;
                }

                if (effectsBarBounds.contains(wx, wy) || effectsKnobBounds.contains(wx, wy)) {
                    draggingEffects = true;
                    updateEffectsFromX(wx);
                    save();
                    return true;
                }

                if (touched(backButton.bounds, screenX, screenY)) {
                    goBackToMenu();
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                viewport.unproject(touchPoint.set(screenX, screenY, 0f));

                if (draggingMusic) {
                    updateMusicFromX(touchPoint.x);
                    save();
                    return true;
                }

                if (draggingEffects) {
                    updateEffectsFromX(touchPoint.x);
                    save();
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                draggingMusic = false;
                draggingEffects = false;
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (dropdownOpen) {
                        dropdownOpen = false;
                        return true;
                    }
                    goBackToMenu();
                    return true;
                }

                if (keycode == Input.Keys.LEFT) {
                    musicVolume = Math.max(0, musicVolume - 5);
                    save();
                    return true;
                }

                if (keycode == Input.Keys.RIGHT) {
                    musicVolume = Math.min(100, musicVolume + 5);
                    save();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        float ww = viewport.getWorldWidth();
        float wh = viewport.getWorldHeight();
        float panelW = 760f;
        float panelH = 560f;
        float panelX = ww / 2f - panelW / 2f;
        float panelY = wh / 2f - panelH / 2f;
        float leftLabelX = panelX + 80f;
        float rightCtrlX = panelX + 400f;
        float ctrlWidth = 240f;
        float row1Y = panelY + 390f;
        float row2Y = panelY + 315f;
        float row3Y = panelY + 240f;
        float row4Y = panelY + 165f;
        float row5Y = panelY + 100f;
        float checkboxSize = 28f;
        fullscreenBox.set(rightCtrlX, row1Y - 22f, checkboxSize, checkboxSize);
        vsyncBox.set(rightCtrlX, row2Y - 22f, checkboxSize, checkboxSize);
        musicBarBounds.set(rightCtrlX, row3Y - 12f, ctrlWidth, 12f);
        effectsBarBounds.set(rightCtrlX, row4Y - 12f, ctrlWidth, 12f);
        float knobSize = 24f;
        float musicKnobCX = musicBarBounds.x + (musicVolume / 100f) * musicBarBounds.width;
        musicKnobBounds.set(musicKnobCX - knobSize / 2f, row3Y - knobSize / 2f - 6f, knobSize, knobSize);
        float effectsKnobCX = effectsBarBounds.x + (effectsVolume / 100f) * effectsBarBounds.width;
        effectsKnobBounds.set(effectsKnobCX - knobSize / 2f, row4Y - knobSize / 2f - 6f, knobSize, knobSize);
        float dropdownH = 38f;
        dropdownHeaderBounds.set(rightCtrlX, row5Y - dropdownH / 2f, ctrlWidth, dropdownH);
        float itemH = 36f;
        for (int i = 0; i < dropdownItemBounds.length; i++) {
            dropdownItemBounds[i].set(dropdownHeaderBounds.x, dropdownHeaderBounds.y - (i + 1) * itemH, ctrlWidth, itemH);
        }
        backButton.set(ww / 2f - 90f, panelY + 22f, 180f, 52f);
        beginFrame();
        drawBackground();
        drawPanel(panelX, panelY, panelW, panelH);
        titleFont.setColor(TEXT_DARK);
        drawCenteredText(titleFont, LocalizationManager.get("options.title"), ww / 2f, panelY + panelH - 40f);
        smallFont.setColor(new Color(0.30f, 0.22f, 0.13f, 0.82f));
        drawCenteredText(smallFont, LocalizationManager.get("options.subtitle"), ww / 2f, panelY + panelH - 76f);
        drawOptionLabel(LocalizationManager.get("options.fullscreen"), leftLabelX, row1Y);
        drawOptionLabel(LocalizationManager.get("options.vsync"), leftLabelX, row2Y);
        drawOptionLabel(LocalizationManager.get("options.music"), leftLabelX, row3Y);
        drawOptionLabel(LocalizationManager.get("options.effects"), leftLabelX, row4Y);
        drawOptionLabel(LocalizationManager.get("language.label"), leftLabelX, row5Y + 6f);
        drawCheckbox(fullscreenBox, fullscreen, isHovered(fullscreenBox));
        drawCheckbox(vsyncBox, vsync, isHovered(vsyncBox));
        drawSlider(musicBarBounds, musicKnobBounds, musicVolume, row3Y, draggingMusic);
        drawSlider(effectsBarBounds, effectsKnobBounds, effectsVolume, row4Y, draggingEffects);
        drawButton(backButton, isHovered(backButton.bounds));
        drawDropdown();
        endFrame();
    }

    private void drawOptionLabel(String text, float x, float y) {
        bodyFont.setColor(TEXT_DARK);
        bodyFont.draw(batch, text, x, y);
    }

    private void drawCheckbox(Rectangle box, boolean checked, boolean hovered) {
        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, box.x, box.y, box.width, box.height);
        batch.setColor(hovered ? BUTTON_HOVER : PANEL_FILL);
        batch.draw(whiteRegion, box.x + 3f, box.y + 3f, box.width - 6f, box.height - 6f);
        if (checked) {
            batch.setColor(new Color(0.24f, 0.45f, 0.18f, 1f));
            batch.draw(whiteRegion, box.x + 7f, box.y + 7f, box.width - 14f, box.height - 14f);
            batch.setColor(new Color(0.90f, 0.95f, 0.85f, 1f));
            batch.draw(whiteRegion, box.x + 11f, box.y + 11f, box.width - 22f, box.height - 22f);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawSlider(Rectangle bar, Rectangle knob, int value, float rowY, boolean dragging) {
        smallFont.setColor(TEXT_DARK);
        smallFont.draw(batch, "0", bar.x - 24f, rowY + 6f);
        smallFont.draw(batch, "100", bar.x + bar.width + 8f, rowY + 6f);
        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, bar.x, bar.y, bar.width, bar.height);
        batch.setColor(new Color(0.84f, 0.78f, 0.64f, 1f));
        batch.draw(whiteRegion, bar.x + 2f, bar.y + 2f, bar.width - 4f, bar.height - 4f);
        float fillW = (value / 100f) * bar.width;
        batch.setColor(new Color(0.46f, 0.32f, 0.16f, 1f));
        batch.draw(whiteRegion, bar.x + 2f, bar.y + 2f, Math.max(0f, fillW - 4f), bar.height - 4f);
        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, knob.x, knob.y, knob.width, knob.height);
        batch.setColor(isHovered(knob) || dragging ? BUTTON_HOVER : PANEL_FILL);
        batch.draw(whiteRegion, knob.x + 3f, knob.y + 3f, knob.width - 6f, knob.height - 6f);
        batch.setColor(Color.WHITE);
    }

    private void drawDropdown() {
        Rectangle hdr = dropdownHeaderBounds;
        boolean hdrHovered = isHovered(hdr) && !dropdownOpen;
        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, hdr.x, hdr.y, hdr.width, hdr.height);
        batch.setColor(hdrHovered ? BUTTON_HOVER : PANEL_FILL);
        batch.draw(whiteRegion, hdr.x + 2f, hdr.y + 2f, hdr.width - 4f, hdr.height - 4f);
        bodyFont.setColor(TEXT_DARK);
        bodyFont.draw(batch, LANGUAGE_NAMES[selectedLanguageIndex] + "  ▾", hdr.x + 12f, hdr.y + hdr.height - 10f);
        if (!dropdownOpen) return;
        for (int i = 0; i < dropdownItemBounds.length; i++) {
            Rectangle item = dropdownItemBounds[i];
            boolean itemHovered = isHovered(item);
            boolean isSelected = (i == selectedLanguageIndex);
            batch.setColor(PANEL_BORDER);
            batch.draw(whiteRegion, item.x, item.y, item.width, item.height);
            if (isSelected) {
                batch.setColor(new Color(0.46f, 0.32f, 0.16f, 0.35f));
            } else {
                batch.setColor(itemHovered ? BUTTON_HOVER : PANEL_FILL);
            }
            batch.draw(whiteRegion, item.x + 2f, item.y + 2f, item.width - 4f, item.height - 4f);
            bodyFont.setColor(TEXT_DARK);
            bodyFont.draw(batch, LANGUAGE_NAMES[i], item.x + 12f, item.y + item.height - 10f);
        }
        batch.setColor(Color.WHITE);
    }
}
