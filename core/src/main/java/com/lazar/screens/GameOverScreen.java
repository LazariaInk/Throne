package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lazar.StartGame;
import com.lazar.config.LocalizationManager;
import com.lazar.data.GameOverContent;
import com.lazar.engine.GameOverType;
import com.lazar.logic.GameOverAssets;
import com.lazar.ui.background.BlurBackgroundRenderer;
import com.lazar.config.FontManager; // 🔥 IMPORTANT

public class GameOverScreen implements Screen {

    private static final float GAME_OVER_TARGET_VOLUME = 0.28f;
    private static final float GAME_OVER_FADE_IN_SPEED = 0.22f;

    private final StartGame game;
    private final GameOverType type;
    private final String emperorName;
    private final String yearsRuled;

    private SpriteBatch batch;
    private Texture background;
    private Texture whiteTexture;
    private TextureRegion whiteRegion;
    private Music backgroundMusic;
    private float currentMusicVolume = 0f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private BitmapFont hintFont;

    private GlyphLayout layout;
    private BlurBackgroundRenderer backgroundRenderer;

    private final Rectangle restartBounds = new Rectangle();
    private final Rectangle menuBounds = new Rectangle();
    private final Vector3 touchPoint = new Vector3();

    public GameOverScreen(StartGame game, GameOverType type, String emperorName, String yearsRuled) {
        this.game = game;
        this.type = type;
        this.emperorName = emperorName;
        this.yearsRuled = yearsRuled;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        background = new Texture(Gdx.files.internal(GameOverAssets.getBackgroundPath(type)));

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(640, 360, 0);
        camera.update();

        whiteTexture = createWhiteTexture();
        whiteRegion = new TextureRegion(whiteTexture);


        titleFont = FontManager.get(30, new Color(0.14f, 0.08f, 0.04f, 1f));
        bodyFont = FontManager.get(20, new Color(0.18f, 0.11f, 0.06f, 1f));
        hintFont = FontManager.get(16, new Color(0.20f, 0.13f, 0.07f, 0.95f));
        layout = new GlyphLayout();
        backgroundRenderer = new BlurBackgroundRenderer(background, whiteRegion, null);
        loadAudio();
        startBackgroundMusic();
        installInputProcessor();
    }

    private void loadAudio() {
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/game-over.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0f);
        currentMusicVolume = 0f;
    }

    private void startBackgroundMusic() {
        if (game.getSoundManager() != null) {
            game.getSoundManager().playGameMusic();
        }
    }

    private void updateMusicFadeIn(float delta) {
        if (backgroundMusic == null || !backgroundMusic.isPlaying()) return;
        if (currentMusicVolume < GAME_OVER_TARGET_VOLUME) {
            currentMusicVolume += delta * GAME_OVER_FADE_IN_SPEED;
            if (currentMusicVolume > GAME_OVER_TARGET_VOLUME) {
                currentMusicVolume = GAME_OVER_TARGET_VOLUME;
            }
            backgroundMusic.setVolume(currentMusicVolume);
        }
    }

    private Texture createWhiteTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void installInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) return false;
                viewport.unproject(touchPoint.set(screenX, screenY, 0f));
                if (restartBounds.contains(touchPoint.x, touchPoint.y)) {
                    game.setScreen(new GameScreen(game, emperorName));
                    dispose();
                    return true;
                }
                if (menuBounds.contains(touchPoint.x, touchPoint.y)) {
                    game.setScreen(new StartScreen(game));
                    dispose();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                    game.setScreen(new GameScreen(game, emperorName));
                    dispose();
                    return true;
                }

                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new StartScreen(game));
                    dispose();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        camera.update();
        updateMusicFadeIn(delta);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
        drawDarkOverlay();
        drawGameOverCard(viewport.getWorldWidth(), viewport.getWorldHeight());
        drawBottomHint(viewport.getWorldWidth());
        batch.end();
    }

    private void drawDarkOverlay() {
        batch.setColor(0f, 0f, 0f, 0.42f);
        batch.draw(whiteRegion, 0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.setColor(Color.WHITE);
    }

    private void drawGameOverCard(float worldWidth, float worldHeight) {
        float cardW = 620f;
        float cardH = 430f;
        float x = worldWidth / 2f - cardW / 2f;
        float y = worldHeight / 2f - cardH / 2f + 20f;
        drawCardBackground(x, y, cardW, cardH);
        String title = GameOverContent.title(type);
        String body = GameOverContent.body(type);
        String reignText = LocalizationManager.get("gameover.reign")
            .replace("{name}", emperorName)
            .replace("{years}", yearsRuled);
        titleFont.setColor(0.16f, 0.08f, 0.04f, 1f);
        layout.setText(titleFont, title, titleFont.getColor(), cardW - 80f, Align.center, true);
        titleFont.draw(batch, layout, x + 40f, y + cardH - 45f);
        bodyFont.setColor(0.22f, 0.13f, 0.07f, 1f);
        layout.setText(bodyFont, body, bodyFont.getColor(), cardW - 90f, Align.center, true);
        bodyFont.draw(batch, layout, x + 45f, y + cardH - 120f);
        bodyFont.setColor(0.30f, 0.18f, 0.09f, 1f);
        layout.setText(bodyFont, reignText, bodyFont.getColor(), cardW - 90f, Align.center, true);
        bodyFont.draw(batch, layout, x + 45f, y + 140f);
        float buttonW = 180f;
        float buttonH = 48f;
        float gap = 20f;
        float total = buttonW * 2f + gap;
        float startX = x + (cardW - total) / 2f;
        float buttonY = y + 34f;
        restartBounds.set(startX, buttonY, buttonW, buttonH);
        menuBounds.set(startX + buttonW + gap, buttonY, buttonW, buttonH);
        drawButton(restartBounds, LocalizationManager.get("gameover.play_again"));
        drawButton(menuBounds, LocalizationManager.get("gameover.menu"));
    }

    private void drawCardBackground(float x, float y, float w, float h) {
        batch.setColor(0f, 0f, 0f, 0.18f);
        batch.draw(whiteRegion, x + 8f, y - 8f, w, h);
        batch.setColor(0.24f, 0.16f, 0.08f, 0.98f);
        batch.draw(whiteRegion, x, y, w, h);
        batch.setColor(0.93f, 0.87f, 0.73f, 0.98f);
        batch.draw(whiteRegion, x + 6f, y + 6f, w - 12f, h - 12f);
        batch.setColor(Color.WHITE);
    }

    private void drawButton(Rectangle bounds, String text) {
        batch.setColor(0f, 0f, 0f, 0.14f);
        batch.draw(whiteRegion, bounds.x + 5f, bounds.y - 5f, bounds.width, bounds.height);
        batch.setColor(0.25f, 0.16f, 0.08f, 0.95f);
        batch.draw(whiteRegion, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(0.93f, 0.87f, 0.73f, 0.98f);
        batch.draw(whiteRegion, bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);
        layout.setText(hintFont, text);
        hintFont.draw(batch, text,
            bounds.x + (bounds.width - layout.width) / 2f,
            bounds.y + bounds.height / 2f + layout.height / 2f - 2f);

        batch.setColor(Color.WHITE);
    }

    private void drawBottomHint(float worldWidth) {
        String hint = LocalizationManager.get("gameover.hint");

        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint, (worldWidth - layout.width) / 2f, 38f);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override
    public void resume() {
        game.getSoundManager().reloadSettings();
        game.getSoundManager().playGameMusic();
    }
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (backgroundRenderer != null) backgroundRenderer.dispose();
        if (batch != null) batch.dispose();
        if (background != null) background.dispose();
        if (whiteTexture != null) whiteTexture.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}
