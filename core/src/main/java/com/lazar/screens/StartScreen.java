package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lazar.StartGame;
import com.lazar.config.FontManager;
import com.lazar.config.LocalizationManager;

public class StartScreen implements Screen {
    private final StartGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture whiteTexture;
    private TextureRegion whiteRegion;
    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private GlyphLayout layout;
    private final StringBuilder emperorName = new StringBuilder();
    private final Rectangle startButton = new Rectangle();
    private final Rectangle recordsButton = new Rectangle();
    private final Vector3 touchPoint = new Vector3();

    public StartScreen(StartGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(640, 360, 0);
        camera.update();
        Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        whiteRegion = new TextureRegion(whiteTexture);
        game.getSoundManager().reloadSettings();
        game.getSoundManager().playMenuMusic();
        titleFont = FontManager.get(34, new Color(0.14f, 0.08f, 0.04f, 1f));
        bodyFont = FontManager.get(20, new Color(0.18f, 0.11f, 0.06f, 1f));
        layout = new GlyphLayout();
        installInput();
    }

    private void installInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (character == '\b') {
                    if (emperorName.length() > 0) {
                        emperorName.deleteCharAt(emperorName.length() - 1);
                    }
                    return true;
                }
                if (character == '\r' || character == '\n') {
                    startGame();
                    return true;
                }
                if (!Character.isISOControl(character) && emperorName.length() < 24) {
                    emperorName.append(character);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                viewport.unproject(touchPoint.set(screenX, screenY, 0f));
                if (startButton.contains(touchPoint.x, touchPoint.y)) {
                    startGame();
                    return true;
                }
                if (recordsButton.contains(touchPoint.x, touchPoint.y)) {
                    game.setScreen(new RecordsScreen(game));
                    dispose();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    startGame();
                    return true;
                }
                return false;
            }
        });
    }

    private void startGame() {
        String name = emperorName.toString().trim();
        if (name.isEmpty()) {
            name = LocalizationManager.get("start.default_emperor_name");
        }
        game.setScreen(new GameScreen(game, name));
        dispose();
    }

    @Override
    public void render(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0.92f, 0.86f, 0.74f, 1f);
        batch.draw(whiteRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.setColor(Color.WHITE);
        titleFont.draw(batch, LocalizationManager.get("start.title"), 390, 540);
        batch.setColor(0.25f, 0.16f, 0.08f, 1f);
        batch.draw(whiteRegion, 390, 420, 500, 56);
        batch.setColor(0.93f, 0.87f, 0.73f, 1f);
        batch.draw(whiteRegion, 393, 423, 494, 50);
        String shown = emperorName.length() == 0
            ? LocalizationManager.get("start.placeholder")
            : emperorName.toString();
        bodyFont.setColor(
            emperorName.length() == 0
                ? new Color(0.35f, 0.28f, 0.20f, 0.7f)
                : new Color(0.20f, 0.13f, 0.07f, 1f)
        );
        bodyFont.draw(batch, shown, 410, 456);
        startButton.set(430, 300, 180, 54);
        recordsButton.set(670, 300, 180, 54);
        drawButton(startButton, LocalizationManager.get("start.button.start"));
        drawButton(recordsButton, LocalizationManager.get("start.button.records"));
        batch.end();
    }

    private void drawButton(Rectangle bounds, String text) {
        batch.setColor(0.25f, 0.16f, 0.08f, 1f);
        batch.draw(whiteRegion, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(0.93f, 0.87f, 0.73f, 1f);
        batch.draw(whiteRegion, bounds.x + 3, bounds.y + 3, bounds.width - 6, bounds.height - 6);
        bodyFont.setColor(0.20f, 0.13f, 0.07f, 1f);
        layout.setText(bodyFont, text);
        bodyFont.draw(batch, text, bounds.x + (bounds.width - layout.width) / 2f, bounds.y + 34f);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
            batch = null;
        }

        if (whiteTexture != null) {
            whiteTexture.dispose();
            whiteTexture = null;
        }
    }
}
