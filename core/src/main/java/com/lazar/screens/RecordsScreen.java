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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lazar.StartGame;
import com.lazar.data.RecordEntry;
import com.lazar.data.RecordsManager;

public class RecordsScreen implements Screen {

    private final StartGame game;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture whiteTexture;
    private TextureRegion whiteRegion;
    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private GlyphLayout layout;

    private Array<RecordEntry> records;

    public RecordsScreen(StartGame game) {
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

        titleFont = generateFont(32, new Color(0.14f, 0.08f, 0.04f, 1f));
        bodyFont = generateFont(20, new Color(0.18f, 0.11f, 0.06f, 1f));
        layout = new GlyphLayout();

        records = new RecordsManager().loadRecords();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                    return true;
                }
                return false;
            }
        });
    }

    private BitmapFont generateFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/medieval.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ĂÂÎȘȚăâîșț";
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    @Override
    public void render(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.setColor(0.92f, 0.86f, 0.74f, 1f);
        batch.draw(whiteRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        titleFont.draw(batch, "Recordurile Imperiului", 420, 650);

        float y = 580f;
        if (records.size == 0) {
            bodyFont.draw(batch, "Nu exista inca domnii inregistrate.", 380, y);
        } else {
            for (int i = 0; i < records.size; i++) {
                RecordEntry r = records.get(i);
                String line = (i + 1) + ". " + r.emperorName + " - " + r.yearsRuled + " ani - " + r.causeOfDeath;
                bodyFont.draw(batch, line, 220, y);
                y -= 34f;
                if (y < 80f) break;
            }
        }

        bodyFont.draw(batch, "ESC pentru inapoi", 520, 40);

        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (whiteTexture != null) whiteTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
    }
}
