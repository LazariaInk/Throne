package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lazar.StartGame;

public abstract class BaseMenuScreen extends ScreenAdapter {

    protected final StartGame game;

    protected SpriteBatch batch;
    protected OrthographicCamera camera;
    protected Viewport viewport;

    protected Texture whiteTexture;
    protected TextureRegion whiteRegion;
    protected Texture backgroundTexture;

    protected BitmapFont titleFont;
    protected BitmapFont bodyFont;
    protected BitmapFont smallFont;
    protected GlyphLayout layout;

    protected final Vector3 touchPoint = new Vector3();

    protected static final Color BG_TINT = new Color(0.92f, 0.86f, 0.74f, 1f);
    protected static final Color PANEL_BORDER = new Color(0.23f, 0.15f, 0.08f, 0.96f);
    protected static final Color PANEL_FILL = new Color(0.93f, 0.87f, 0.73f, 0.97f);
    protected static final Color TEXT_DARK = new Color(0.20f, 0.13f, 0.07f, 1f);
    protected static final Color TEXT_SOFT = new Color(0.35f, 0.28f, 0.20f, 0.70f);
    protected static final Color BUTTON_HOVER = new Color(0.86f, 0.79f, 0.62f, 1f);

    protected static class MenuButton {
        public final Rectangle bounds = new Rectangle();
        public String text;

        public MenuButton(String text) {
            this.text = text;
        }

        public void set(float x, float y, float w, float h) {
            bounds.set(x, y, w, h);
        }
    }

    public BaseMenuScreen(StartGame game) {
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

        titleFont = generateFont(34, TEXT_DARK);
        bodyFont = generateFont(22, TEXT_DARK);
        smallFont = generateFont(18, TEXT_DARK);
        layout = new GlyphLayout();

        if (Gdx.files.internal("images/tournament-bg.png").exists()) {
            backgroundTexture = new Texture(Gdx.files.internal("images/tournament-bg.png"));
            backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        onShow();
    }

    protected void onShow() {
    }

    protected BitmapFont generateFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/medieval.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ĂÂÎȘȚăâîșț";

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    protected void beginFrame() {
        Gdx.gl.glClearColor(0.08f, 0.06f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
    }

    protected void endFrame() {
        batch.end();
    }

    protected void drawBackground() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        if (backgroundTexture == null) {
            batch.setColor(BG_TINT);
            batch.draw(whiteRegion, 0, 0, worldWidth, worldHeight);
            batch.setColor(Color.WHITE);
            return;
        }

        float bgAspect = (float) backgroundTexture.getWidth() / backgroundTexture.getHeight();
        float screenAspect = worldWidth / worldHeight;

        float drawWidth, drawHeight, drawX, drawY;
        if (screenAspect > bgAspect) {
            drawWidth = worldWidth;
            drawHeight = worldWidth / bgAspect;
            drawX = 0f;
            drawY = (worldHeight - drawHeight) / 2f;
        } else {
            drawHeight = worldHeight;
            drawWidth = worldHeight * bgAspect;
            drawX = (worldWidth - drawWidth) / 2f;
            drawY = 0f;
        }

        batch.setColor(Color.WHITE);
        batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);

        batch.setColor(0f, 0f, 0f, 0.35f);
        batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);

        batch.setColor(Color.WHITE);
    }

    protected void drawPanel(float x, float y, float w, float h) {
        batch.setColor(0f, 0f, 0f, 0.20f);
        batch.draw(whiteRegion, x + 8f, y - 8f, w, h);

        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, x, y, w, h);

        batch.setColor(PANEL_FILL);
        batch.draw(whiteRegion, x + 4f, y + 4f, w - 8f, h - 8f);

        batch.setColor(1f, 1f, 1f, 0.08f);
        batch.draw(whiteRegion, x + 12f, y + h - 14f, w - 24f, 4f);

        batch.setColor(Color.WHITE);
    }

    protected void drawCenteredText(BitmapFont font, String text, float centerX, float y) {
        layout.setText(font, text);
        font.draw(batch, text, centerX - layout.width / 2f, y);
    }

    protected void drawButton(MenuButton button, boolean hovered) {
        Rectangle b = button.bounds;

        batch.setColor(PANEL_BORDER);
        batch.draw(whiteRegion, b.x, b.y, b.width, b.height);

        batch.setColor(hovered ? BUTTON_HOVER : PANEL_FILL);
        batch.draw(whiteRegion, b.x + 3f, b.y + 3f, b.width - 6f, b.height - 6f);

        bodyFont.setColor(TEXT_DARK);
        layout.setText(bodyFont, button.text);
        bodyFont.draw(batch, button.text, b.x + (b.width - layout.width) / 2f, b.y + b.height / 2f + layout.height / 2f - 2f);

        batch.setColor(Color.WHITE);
    }

    protected boolean isHovered(Rectangle bounds) {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        viewport.unproject(touchPoint.set(x, y, 0f));
        return bounds.contains(touchPoint.x, touchPoint.y);
    }

    protected boolean touched(Rectangle bounds, int screenX, int screenY) {
        viewport.unproject(touchPoint.set(screenX, screenY, 0f));
        return bounds.contains(touchPoint.x, touchPoint.y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0f);
        camera.update();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (whiteTexture != null) whiteTexture.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}
