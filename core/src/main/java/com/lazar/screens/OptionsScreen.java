package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.lazar.StartGame;

public class OptionsScreen extends BaseMenuScreen {

    private final MenuButton backButton = new MenuButton("Back");

    private final Rectangle fullscreenBox = new Rectangle();
    private final Rectangle vsyncBox = new Rectangle();

    private final Rectangle musicBarBounds = new Rectangle();
    private final Rectangle musicKnobBounds = new Rectangle();

    private final Rectangle effectsBarBounds = new Rectangle();
    private final Rectangle effectsKnobBounds = new Rectangle();

    private Preferences prefs;

    private boolean fullscreen;
    private boolean vsync;
    private int musicVolume;
    private int effectsVolume;

    private boolean draggingMusic = false;
    private boolean draggingEffects = false;

    public OptionsScreen(StartGame game) {
        super(game);
    }

    @Override
    protected void onShow() {
        prefs = Gdx.app.getPreferences(StartGame.SETTINGS_PREFS);

        fullscreen = prefs.getBoolean("fullscreen", false);
        vsync = prefs.getBoolean("vsync", true);
        musicVolume = prefs.getInteger("musicVolume", 70);
        effectsVolume = prefs.getInteger("effectsVolume", 70);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                viewport.unproject(touchPoint.set(screenX, screenY, 0f));

                if (fullscreenBox.contains(touchPoint.x, touchPoint.y)) {
                    fullscreen = !fullscreen;
                    applyDisplayMode();
                    save();
                    return true;
                }

                if (vsyncBox.contains(touchPoint.x, touchPoint.y)) {
                    vsync = !vsync;
                    Gdx.graphics.setVSync(vsync);
                    save();
                    return true;
                }

                if (musicBarBounds.contains(touchPoint.x, touchPoint.y)) {
                    updateMusicFromX(touchPoint.x);
                    draggingMusic = true;
                    save();
                    return true;
                }

                if (musicKnobBounds.contains(touchPoint.x, touchPoint.y)) {
                    draggingMusic = true;
                    updateMusicFromX(touchPoint.x);
                    save();
                    return true;
                }

                if (effectsBarBounds.contains(touchPoint.x, touchPoint.y)) {
                    updateEffectsFromX(touchPoint.x);
                    draggingEffects = true;
                    save();
                    return true;
                }

                if (effectsKnobBounds.contains(touchPoint.x, touchPoint.y)) {
                    draggingEffects = true;
                    updateEffectsFromX(touchPoint.x);
                    save();
                    return true;
                }

                if (touched(backButton.bounds, screenX, screenY)) {
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
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
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
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
        float percent = (clamped - musicBarBounds.x) / musicBarBounds.width;
        musicVolume = Math.round(percent * 100f);
    }

    private void updateEffectsFromX(float x) {
        float clamped = Math.max(effectsBarBounds.x, Math.min(x, effectsBarBounds.x + effectsBarBounds.width));
        float percent = (clamped - effectsBarBounds.x) / effectsBarBounds.width;
        effectsVolume = Math.round(percent * 100f);
    }

    private void save() {
        prefs.putBoolean("fullscreen", fullscreen);
        prefs.putBoolean("vsync", vsync);
        prefs.putInteger("musicVolume", musicVolume);
        prefs.putInteger("effectsVolume", effectsVolume);
        prefs.flush();
    }

    @Override
    public void render(float delta) {
        float ww = viewport.getWorldWidth();
        float wh = viewport.getWorldHeight();

        float panelW = 760f;
        float panelH = 520f;
        float panelX = ww / 2f - panelW / 2f;
        float panelY = wh / 2f - panelH / 2f;

        float leftLabelX = panelX + 80f;
        float rightControlX = panelX + 520f;

        float row1Y = panelY + 355f;
        float row2Y = panelY + 285f;
        float row3Y = panelY + 205f;
        float row4Y = panelY + 125f;

        float checkboxSize = 28f;

        fullscreenBox.set(rightControlX, row1Y - 22f, checkboxSize, checkboxSize);
        vsyncBox.set(rightControlX, row2Y - 22f, checkboxSize, checkboxSize);

        musicBarBounds.set(panelX + 360f, row3Y - 12f, 240f, 12f);
        effectsBarBounds.set(panelX + 360f, row4Y - 12f, 240f, 12f);

        float knobSize = 24f;

        float musicKnobCenterX = musicBarBounds.x + (musicVolume / 100f) * musicBarBounds.width;
        musicKnobBounds.set(
            musicKnobCenterX - knobSize / 2f,
            row3Y - knobSize / 2f - 6f,
            knobSize,
            knobSize
        );

        float effectsKnobCenterX = effectsBarBounds.x + (effectsVolume / 100f) * effectsBarBounds.width;
        effectsKnobBounds.set(
            effectsKnobCenterX - knobSize / 2f,
            row4Y - knobSize / 2f - 6f,
            knobSize,
            knobSize
        );

        backButton.set(ww / 2f - 90f, panelY + 30f, 180f, 52f);

        beginFrame();
        drawBackground();
        drawPanel(panelX, panelY, panelW, panelH);

        titleFont.setColor(TEXT_DARK);
        drawCenteredText(titleFont, "Options", ww / 2f, panelY + panelH - 45f);

        smallFont.setColor(new Color(0.30f, 0.22f, 0.13f, 0.82f));
        drawCenteredText(smallFont, "Setari video si audio", ww / 2f, panelY + panelH - 82f);

        drawOptionLabel("Fullscreen", leftLabelX, row1Y);
        drawOptionLabel("VSync", leftLabelX, row2Y);
        drawOptionLabel("Music Volume", leftLabelX, row3Y);
        drawOptionLabel("Effects Volume", leftLabelX, row4Y);

        drawCheckbox(fullscreenBox, fullscreen, isHovered(fullscreenBox));
        drawCheckbox(vsyncBox, vsync, isHovered(vsyncBox));

        drawSlider(musicBarBounds, musicKnobBounds, musicVolume, row3Y, draggingMusic);
        drawSlider(effectsBarBounds, effectsKnobBounds, effectsVolume, row4Y, draggingEffects);

        drawButton(backButton, isHovered(backButton.bounds));

        smallFont.setColor(new Color(0.30f, 0.22f, 0.13f, 0.72f));

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

    private void drawSlider(Rectangle barBounds, Rectangle knobBounds, int value, float rowY, boolean dragging) {
        float leftTextX = barBounds.x - 40f;
        float rightTextX = barBounds.x + barBounds.width + 15f;

        smallFont.setColor(TEXT_DARK);
        smallFont.draw(batch, "0", leftTextX, rowY + 6f);
        smallFont.draw(batch, "100", rightTextX, rowY + 6f);

        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, barBounds.x, barBounds.y, barBounds.width, barBounds.height);

        batch.setColor(new Color(0.84f, 0.78f, 0.64f, 1f));
        batch.draw(
            whiteRegion,
            barBounds.x + 2f,
            barBounds.y + 2f,
            barBounds.width - 4f,
            barBounds.height - 4f
        );

        float fillWidth = (value / 100f) * barBounds.width;
        batch.setColor(new Color(0.46f, 0.32f, 0.16f, 1f));
        batch.draw(
            whiteRegion,
            barBounds.x + 2f,
            barBounds.y + 2f,
            Math.max(0f, fillWidth - 4f),
            barBounds.height - 4f
        );

        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, knobBounds.x, knobBounds.y, knobBounds.width, knobBounds.height);

        batch.setColor(isHovered(knobBounds) || dragging ? BUTTON_HOVER : PANEL_FILL);
        batch.draw(
            whiteRegion,
            knobBounds.x + 3f,
            knobBounds.y + 3f,
            knobBounds.width - 6f,
            knobBounds.height - 6f
        );

        batch.setColor(Color.WHITE);
    }
}
