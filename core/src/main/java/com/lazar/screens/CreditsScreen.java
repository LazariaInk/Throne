package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.lazar.StartGame;

public class CreditsScreen extends BaseMenuScreen {

    private final MenuButton backButton = new MenuButton("Back");
    private final Screen continueScreen;

    public CreditsScreen(StartGame game, Screen continueScreen) {
        super(game);
        this.continueScreen = continueScreen;
    }

    private void goBackToMenu() {
        game.setScreen(new MainMenuScreen(game, continueScreen));
        dispose();
    }

    @Override
    protected void onShow() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (touched(backButton.bounds, screenX, screenY)) {
                    goBackToMenu();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    goBackToMenu();
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
        float panelH = 420f;
        float panelX = ww / 2f - panelW / 2f;
        float panelY = wh / 2f - panelH / 2f;
        backButton.set(ww / 2f - 90f, panelY + 30f, 180f, 52f);
        beginFrame();
        drawBackground();
        drawPanel(panelX, panelY, panelW, panelH);
        titleFont.setColor(TEXT_DARK);
        drawCenteredText(titleFont, "Credits", ww / 2f, panelY + panelH - 45f);
        bodyFont.setColor(TEXT_DARK);
        drawCenteredText(bodyFont, "Game Design & Programming", ww / 2f, panelY + 285f);
        drawCenteredText(bodyFont, "Lazar", ww / 2f, panelY + 245f);
        drawCenteredText(bodyFont, "Framework", ww / 2f, panelY + 185f);
        drawCenteredText(bodyFont, "LibGDX", ww / 2f, panelY + 145f);
        smallFont.setColor(new com.badlogic.gdx.graphics.Color(0.30f, 0.22f, 0.13f, 0.80f));
        drawCenteredText(smallFont, "Construit in stil medieval, cu meniu modular si ecrane separate.", ww / 2f, panelY + 105f);
        drawButton(backButton, isHovered(backButton.bounds));
        endFrame();
    }
}
