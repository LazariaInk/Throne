package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.lazar.StartGame;

public class MainMenuScreen extends BaseMenuScreen {

    private final MenuButton continueButton = new MenuButton("Continue");
    private final MenuButton newGameButton = new MenuButton("Joaca Noua");
    private final MenuButton recordsButton = new MenuButton("Recorduri");
    private final MenuButton optionsButton = new MenuButton("Options");
    private final MenuButton creditsButton = new MenuButton("Credits");
    private final MenuButton exitButton = new MenuButton("Exit");

    private final Screen continueScreen;
    private final boolean showContinue;

    public MainMenuScreen(StartGame game) {
        this(game, null);
    }

    public MainMenuScreen(StartGame game, Screen continueScreen) {
        super(game);
        this.continueScreen = continueScreen;
        this.showContinue = continueScreen != null;
    }

    @Override
    protected void onShow() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (showContinue && touched(continueButton.bounds, screenX, screenY)) {
                    game.setScreen(continueScreen);
                    dispose();
                    return true;
                }

                if (touched(newGameButton.bounds, screenX, screenY)) {
                    game.setScreen(new StartScreen(game));
                    dispose();
                    return true;
                }

                if (touched(recordsButton.bounds, screenX, screenY)) {
                    game.setScreen(new RecordsScreen(game));
                    dispose();
                    return true;
                }

                if (touched(optionsButton.bounds, screenX, screenY)) {
                    game.setScreen(new OptionsScreen(game));
                    dispose();
                    return true;
                }

                if (touched(creditsButton.bounds, screenX, screenY)) {
                    game.setScreen(new CreditsScreen(game));
                    dispose();
                    return true;
                }

                if (touched(exitButton.bounds, screenX, screenY)) {
                    Gdx.app.exit();
                    return true;
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (showContinue) {
                        game.setScreen(continueScreen);
                        dispose();
                    } else {
                        Gdx.app.exit();
                    }
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

        float panelW = 520f;
        float panelH = showContinue ? 530f : 470f;
        float panelX = ww / 2f - panelW / 2f;
        float panelY = wh / 2f - panelH / 2f;

        float btnW = 300f;
        float btnH = 54f;
        float btnX = ww / 2f - btnW / 2f;
        float startY = showContinue ? panelY + 330f : panelY + 270f;
        float gap = 64f;

        int row = 0;

        if (showContinue) {
            continueButton.set(btnX, startY - gap * row, btnW, btnH);
            row++;
        }

        newGameButton.set(btnX, startY - gap * row, btnW, btnH);
        row++;

        recordsButton.set(btnX, startY - gap * row, btnW, btnH);
        row++;

        optionsButton.set(btnX, startY - gap * row, btnW, btnH);
        row++;

        creditsButton.set(btnX, startY - gap * row, btnW, btnH);
        row++;

        exitButton.set(btnX, startY - gap * row, btnW, btnH);

        beginFrame();
        drawBackground();

        drawPanel(panelX, panelY, panelW, panelH);

        titleFont.setColor(TEXT_DARK);
        drawCenteredText(titleFont, "Throne", ww / 2f, panelY + panelH - 55f);

        smallFont.setColor(new com.badlogic.gdx.graphics.Color(0.30f, 0.22f, 0.13f, 0.85f));
        drawCenteredText(smallFont, "Meniu Principal", ww / 2f, panelY + panelH - 95f);

        if (showContinue) {
            drawButton(continueButton, isHovered(continueButton.bounds));
        }

        drawButton(newGameButton, isHovered(newGameButton.bounds));
        drawButton(recordsButton, isHovered(recordsButton.bounds));
        drawButton(optionsButton, isHovered(optionsButton.bounds));
        drawButton(creditsButton, isHovered(creditsButton.bounds));
        drawButton(exitButton, isHovered(exitButton.bounds));

        endFrame();
    }
}
