package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lazar.StartGame;
import com.lazar.config.FontManager;
import com.lazar.config.LocalizationManager;
import com.lazar.logic.WarBattle;
import com.lazar.logic.WarBattle.Phase;
import com.lazar.logic.WarBattle.Unit;
import java.util.Locale;

/** Illustrated castle defense. Numbers always identify hand positions, never fixed units. */
public class WarScreen implements Screen {
    public interface Completion { void onComplete(int wins); }
    public interface Progress { void onSave(WarBattle.Snapshot saved); }
    private static final Color GOLD = Color.valueOf("D5B173");
    private static final Color INK = Color.valueOf("332214");
    private static final Color PAPER = Color.valueOf("EDDEBA");
    private static final Color GREEN = Color.valueOf("88B57D");
    private static final Color RED = Color.valueOf("DA826B");
    private static final String[] ART_NAMES = {"archer", "cavalry", "spearman", "shield"};
    private final StartGame game;
    private final Screen previousScreen;
    private final Completion completion;
    private final Progress progress;
    private final WarBattle battle = new WarBattle();
    private final Rectangle[] cards = new Rectangle[4];
    private final Rectangle action = new Rectangle(490, 344, 300, 48);
    private final Rectangle back = new Rectangle(40, 655, 162, 36);
    private final Vector3 pointer = new Vector3();
    private final Matrix4 originalTransform = new Matrix4();
    private final Matrix4 cardTransform = new Matrix4();
    private final Texture[] artwork = new Texture[4];
    private final Sound[] attacks = new Sound[4];
    private Texture background, white, smoke;
    private Sound successSound, failureSound;
    private SpriteBatch batch;
    private BitmapFont titleFont, bodyFont, smallFont, cardFont;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private InputAdapter input;
    private boolean paused, returned;
    private float elapsed;

    public WarScreen(StartGame game, Screen previousScreen) { this(game, previousScreen, null); }
    public WarScreen(StartGame game, Screen previousScreen, Completion completion) { this(game, previousScreen, completion, null, null); }
    public WarScreen(StartGame game, Screen previousScreen, Completion completion, WarBattle.Snapshot saved, Progress progress) {
        this.game = game; this.previousScreen = previousScreen; this.completion = completion; this.progress = progress;
        if (saved != null) battle.restore(saved);
    }
    private String t(String key) { return LocalizationManager.get("war." + key); }
    private String name(Unit unit) { return t("unit." + ART_NAMES[unit.ordinal()]); }

    @Override public void show() {
        if (batch == null) {
            batch = new SpriteBatch();
            titleFont = FontManager.get(34, Color.WHITE); bodyFont = FontManager.get(23, Color.WHITE);
            smallFont = FontManager.get(17, Color.WHITE); cardFont = FontManager.get(18, Color.WHITE);
            background = texture("images/war/siege.png");
            for (int i = 0; i < artwork.length; i++) {
                artwork[i] = texture("images/war/" + ART_NAMES[i] + ".png");
                attacks[i] = Gdx.audio.newSound(Gdx.files.internal("sounds/war/" + ART_NAMES[i] + ".wav"));
                cards[i] = new Rectangle(244 + i * 204, 58, 180, 236);
            }
            successSound = Gdx.audio.newSound(Gdx.files.internal("sounds/war/success.wav"));
            failureSound = Gdx.audio.newSound(Gdx.files.internal("sounds/war/failure.wav"));
            Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888); pixel.setColor(Color.WHITE); pixel.fill();
            white = new Texture(pixel); pixel.dispose();
            Pixmap cloud = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
                float distance = Vector2.dst(x, y, 31.5f, 31.5f) / 32f;
                cloud.setColor(1, 1, 1, Math.max(0, (1 - distance) * (1 - distance)) * (distance < 1 ? 1 : 0));
                cloud.drawPixel(x, y);
            }
            smoke = new Texture(cloud); cloud.dispose();
            camera = new OrthographicCamera(); viewport = new FitViewport(1280, 720, camera);
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            input = new InputAdapter() {
                @Override public boolean keyDown(int key) {
                    if (key == Input.Keys.ESCAPE) { leave(); return true; }
                    if (key == Input.Keys.ENTER || key == Input.Keys.SPACE || key == Input.Keys.R) {
                        if (battle.getPhase() == Phase.READY || battle.getPhase() == Phase.FINISHED) startBattle();
                        return true;
                    }
                    for (int slot = 0; slot < 4; slot++) {
                        if (key == Input.Keys.NUM_1 + slot || key == Input.Keys.NUMPAD_1 + slot) { chooseSlot(slot); return true; }
                    }
                    return false;
                }
                @Override public boolean touchDown(int x, int y, int finger, int button) {
                    if (button != Input.Buttons.LEFT) return false;
                    viewport.unproject(pointer.set(x, y, 0));
                    if (back.contains(pointer.x, pointer.y)) { leave(); return true; }
                    if (battle.getPhase() == Phase.READY || battle.getPhase() == Phase.FINISHED) {
                        if (action.contains(pointer.x, pointer.y)) startBattle();
                    } else {
                        for (int slot = 0; slot < cards.length; slot++) if (cards[slot].contains(pointer.x, pointer.y)) { chooseSlot(slot); break; }
                    }
                    return true;
                }
            };
        }
        paused = false; Gdx.input.setInputProcessor(input);
        if (game.getSoundManager() != null) game.getSoundManager().playGameMusic();
        saveProgress();
    }

    private Texture texture(String path) {
        Texture result = new Texture(Gdx.files.internal(path));
        result.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); return result;
    }
    private void play(Sound sound, float scale) {
        if (sound != null && game.getSoundManager() != null) {
            float volume = game.getSoundManager().getEffectsVolumeFloat();
            if (volume > 0) sound.play(volume * scale);
        }
    }
    private void chooseSlot(int slot) {
        if (battle.getPhase() != Phase.CHOOSING) return;
        play(attacks[battle.getUnitAt(slot).ordinal()], 0.65f);
        battle.chooseSlot(slot); saveProgress();
    }
    private void saveProgress() { if (!returned && progress != null) progress.onSave(battle.snapshot()); }
    private void startBattle() {
        if (completion != null && battle.getPhase() == Phase.FINISHED) { leave(); return; }
        battle.start(); saveProgress();
        if (game.getSoundManager() != null) game.getSoundManager().playCardSwap();
    }
    private void leave() {
        if (returned) return;
        returned = true; game.setScreen(previousScreen);
        if (completion != null) completion.onComplete(battle.getPhase() == Phase.FINISHED ? battle.getWins() : 0);
        dispose();
    }

    @Override public void render(float delta) {
        if (!paused) {
            Phase oldPhase = battle.getPhase(); float oldTime = battle.getRemaining();
            battle.update(delta); elapsed += delta;
            if (oldPhase == Phase.RESULT && battle.getPhase() == Phase.RESULT && oldTime > 0.9f && battle.getRemaining() <= 0.9f) {
                play(battle.isCorrect() ? successSound : failureSound, 0.5f);
            }
            if (oldPhase != battle.getPhase()) {
                saveProgress();
                if (battle.getPhase() == Phase.REVEAL && game.getSoundManager() != null) game.getSoundManager().playCardSwap();
            }
        }
        viewport.apply(); camera.update();
        Gdx.gl.glClearColor(0.08f, 0.06f, 0.04f, 1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined); batch.begin();
        drawBackground(); drawHeader();
        if (battle.getPhase() == Phase.READY || battle.getPhase() == Phase.FINISHED) drawSummary();
        else drawCombat();
        batch.end();
    }

    private void drawBackground() {
        float scale = Math.max(1280f / background.getWidth(), 720f / background.getHeight()) * 1.04f;
        float w = background.getWidth() * scale, h = background.getHeight() * scale;
        batch.setColor(Color.WHITE);
        batch.draw(background, (1280 - w) / 2 + MathUtils.sin(elapsed * 0.14f) * 7, (720 - h) / 2, w, h);
        fill(0, 0, 1280, 720, new Color(0.1f, 0.07f, 0.035f, 0.22f));
        fill(0, 0, 1280, 320, new Color(0.08f, 0.055f, 0.025f, 0.48f));
        // Subtle moving smoke sits in the world, behind all readable UI.
        for (int i = 0; i < 7; i++) {
            float life = (elapsed * 0.035f + i * 0.143f) % 1f;
            batch.setColor(0.72f, 0.66f, 0.54f, (1 - life) * 0.13f);
            batch.draw(smoke, 70 + i * 170 + life * 50, 280 + life * 190, 130 + life * 100, 100 + life * 120);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawHeader() {
        panel(24, 645, 1232, 59, INK, GOLD, 0.96f);
        panel(back.x, back.y, back.width, back.height, INK, GOLD, 1);
        text(smallFont, t(completion != null && battle.getPhase() != Phase.FINISHED ? "retreat" : "back"), back.x, 679, back.width, GOLD);
        text(titleFont, t("title"), 250, 689, 780, PAPER);
        text(smallFont, t("score").replace("{wins}", String.valueOf(battle.getWins())), 1040, 679, 190, GOLD);
        if (battle.getPhase() != Phase.READY) {
            for (int i = 0; i < WarBattle.TOTAL_ROUNDS; i++) {
                Color color = battle.getResult(i) > 0 ? GREEN : battle.getResult(i) < 0 ? RED : GOLD;
                batch.setColor(color); batch.draw(white, 521 + i * 25, 626, 5, 5, 10, 10, 1, 1, 45, 0, 0, 1, 1, false, false);
            }
            batch.setColor(Color.WHITE);
        }
    }

    private void drawCombat() {
        Phase phase = battle.getPhase();
        float reveal = phase == Phase.REVEAL ? 1 - battle.getRemaining() / WarBattle.REVEAL_SECONDS : 1;
        float result = phase == Phase.RESULT ? WarBattle.RESULT_SECONDS - battle.getRemaining() : 0;
        float impact = MathUtils.clamp((result - 0.32f) / 0.3f, 0, 1);
        float enemyX = 550 - (battle.isCorrect() ? impact * 16 : 0);
        float enemyRotation = (1 - reveal) * -7 - (battle.isCorrect() ? impact * 8 : 0);
        drawCard(enemyX, 382 + (1 - reveal) * 30, 180, 236, battle.getEnemy(), enemyRotation,
            phase == Phase.RESULT && battle.isCorrect() ? 1 - impact * 0.4f : 1, GOLD, 0);
        text(smallFont, t("enemy"), 300, 616, 220, PAPER);
        text(smallFont, t("round").replace("{round}", String.valueOf(battle.getRound())), 760, 616, 220, PAPER);
        viewport.unproject(pointer.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        for (int slot = 0; slot < cards.length; slot++) {
            Rectangle card = cards[slot]; Unit unit = battle.getUnitAt(slot);
            Color border = GOLD;
            if (phase == Phase.RESULT && unit == battle.getEnemy().counter()) border = GREEN;
            else if (phase == Phase.RESULT && unit == battle.getChosen()) border = RED;
            boolean hover = phase == Phase.CHOOSING && card.contains(pointer.x, pointer.y);
            boolean chosen = phase == Phase.RESULT && unit == battle.getChosen();
            float deal = (1 - reveal) * (12 + slot * 5);
            drawCard(card.x, card.y + (hover ? 8 : 0) - deal, card.width, card.height, unit, 0, chosen ? 0.26f : 1,
                hover ? PAPER : border, slot + 1);
            if (chosen) {
                float advance = Interpolation.sineOut.apply(MathUtils.clamp(result / 0.4f, 0, 1));
                if (!battle.isCorrect()) advance *= 1 - MathUtils.clamp((result - 0.55f) / 0.65f, 0, 1) * 0.7f;
                drawCard(MathUtils.lerp(card.x, 674, advance), MathUtils.lerp(card.y, 371, advance), card.width, card.height,
                    unit, advance * 7, 1, border, 0);
            }
        }
        if (phase == Phase.CHOOSING || phase == Phase.REVEAL) {
            float fraction = phase == Phase.REVEAL ? 1 : battle.getRemaining() / battle.getRoundDuration();
            panel(404, 346, 472, 13, INK, GOLD, 1);
            fill(407, 349, 466 * MathUtils.clamp(fraction, 0, 1), 7, fraction < 0.28f ? RED : GOLD);
            String seconds = String.format(Locale.ROOT, "%.1f", phase == Phase.REVEAL ? battle.getRoundDuration() : battle.getRemaining());
            text(bodyFont, phase == Phase.REVEAL ? t("prepare") : t("choose").replace("{seconds}", seconds), 300, 378, 680, PAPER);
            text(smallFont, t("hand_hint"), 140, 330, 1000, PAPER);
        } else {
            String key = battle.isCorrect() ? "hit" : battle.getChosen() == null ? "timeout" : "miss";
            text(bodyFont, t(key), 250, 358, 780, battle.isCorrect() ? GREEN : RED);
            text(smallFont, t("correct").replace("{unit}", name(battle.getEnemy().counter())), 240, 327, 800, PAPER);
            if (battle.getChosen() != null && result > 0.32f && result < 0.9f) drawImpact(result, battle.isCorrect() ? GOLD : RED);
        }
        text(smallFont, t("controls"), 160, 38, 960, PAPER);
    }

    private void drawImpact(float time, Color color) {
        float life = (time - 0.32f) / 0.58f;
        for (int i = 0; i < 9; i++) {
            float angle = i * 40;
            batch.setColor(color.r, color.g, color.b, 1 - life);
            float x = 704 + MathUtils.cosDeg(angle) * life * 70;
            float y = 484 + MathUtils.sinDeg(angle) * life * 70;
            batch.draw(white, x, y, 0, 2, 12 * (1 - life) + 3, 3, 1, 1, angle, 0, 0, 1, 1, false, false);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawSummary() {
        boolean ready = battle.getPhase() == Phase.READY;
        for (int i = 0; i < cards.length; i++) {
            Rectangle card = cards[i]; drawCard(card.x, card.y, card.width, card.height, battle.getUnitAt(i), (i - 1.5f) * 1.5f, 1, GOLD, 0);
        }
        panel(236, 324, 808, 280, PAPER, GOLD, 0.98f);
        String heading = ready ? "intro_title" : battle.getWins() > 5 ? "victory" : battle.getWins() == 5 ? "draw" : "defeat";
        text(titleFont, t(heading), 260, 569, 760, INK);
        text(bodyFont, ready ? t("intro_timing") : t("final_score").replace("{wins}", String.valueOf(battle.getWins())), 270, 520, 740, INK);
        text(smallFont, ready ? t("intro_rules") : t(completion == null ? "training" : "campaign_result"), 270, 481, 740, INK);
        text(smallFont, ready ? t("intro_shuffle") : t(completion == null ? "restart_hint" : "return_hint"), 270, 451, 740, INK);
        panel(action.x, action.y, action.width, action.height, INK, GOLD, 1);
        text(bodyFont, t(ready ? "start" : completion == null ? "restart" : "return"), action.x, action.y + 33, action.width, PAPER);
        // Rules are taught before the timer starts, without revealing answers during combat.
        if (ready) {
            text(smallFont, t("rules_a"), 120, 315, 1040, PAPER);
        }
        text(smallFont, ready ? t("rules_b") : t(completion == null ? "restart_hint" : "return_hint"), 100, 38, 1080, PAPER);
    }

    private void drawCard(float x, float y, float w, float h, Unit unit, float rotation, float alpha, Color border, int slot) {
        originalTransform.set(batch.getTransformMatrix());
        cardTransform.set(originalTransform).translate(x + w / 2, y + h / 2, 0).rotate(0, 0, 1, rotation).translate(-w / 2, -h / 2, 0);
        batch.setTransformMatrix(cardTransform);
        fill(6, -6, w, h, new Color(0.06f, 0.035f, 0.01f, alpha * 0.5f));
        fill(0, 0, w, h, new Color(INK.r, INK.g, INK.b, alpha));
        fill(3, 3, w - 6, h - 6, new Color(border.r, border.g, border.b, alpha));
        fill(6, 6, w - 12, h - 12, new Color(PAPER.r, PAPER.g, PAPER.b, alpha));
        Texture art = artwork[unit.ordinal()];
        float artHeight = h - 49, artWidth = artHeight * art.getWidth() / art.getHeight();
        batch.setColor(1, 1, 1, alpha); batch.draw(art, (w - artWidth) / 2, 42, artWidth, artHeight);
        fill(7, 7, w - 14, 35, new Color(PAPER.r, PAPER.g, PAPER.b, alpha));
        cardFont.setColor(INK.r, INK.g, INK.b, alpha);
        cardFont.draw(batch, name(unit), 10, 32, w - 20, Align.center, true);
        if (slot > 0) {
            fill(10, h - 32, 24, 22, new Color(INK.r, INK.g, INK.b, alpha));
            smallFont.setColor(GOLD.r, GOLD.g, GOLD.b, alpha); smallFont.draw(batch, String.valueOf(slot), 10, h - 15, 24, Align.center, false);
        }
        batch.setColor(Color.WHITE); batch.setTransformMatrix(originalTransform);
    }

    private void panel(float x, float y, float w, float h, Color color, Color border, float alpha) {
        fill(x + 5, y - 5, w, h, new Color(0.07f, 0.04f, 0.02f, alpha * 0.4f));
        fill(x, y, w, h, new Color(border.r, border.g, border.b, alpha));
        fill(x + 2, y + 2, w - 4, h - 4, new Color(color.r, color.g, color.b, alpha));
    }
    private void fill(float x, float y, float w, float h, Color color) {
        batch.setColor(color); batch.draw(white, x, y, w, h); batch.setColor(Color.WHITE);
    }
    private void text(BitmapFont font, String value, float x, float y, float width, Color color) {
        if (color != INK) {
            font.setColor(0.08f, 0.045f, 0.015f, 0.8f);
            font.draw(batch, value, x + 1, y - 2, width, Align.center, false);
        }
        font.setColor(color); font.draw(batch, value, x, y, width, Align.center, false); font.setColor(Color.WHITE);
    }
    @Override public void resize(int width, int height) { if (viewport != null) viewport.update(width, height, true); }
    @Override public void pause() {
        saveProgress(); paused = true;
        if (game.getSoundManager() != null) game.getSoundManager().pauseBackgroundMusic();
    }
    @Override public void resume() {
        paused = false;
        if (game.getSoundManager() != null) game.getSoundManager().playGameMusic();
    }
    @Override public void hide() { saveProgress(); if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {
        saveProgress(); if (batch == null) return;
        batch.dispose(); background.dispose(); white.dispose(); smoke.dispose();
        for (Texture art : artwork) if (art != null) art.dispose();
        for (Sound sound : attacks) if (sound != null) sound.dispose();
        if (successSound != null) successSound.dispose(); if (failureSound != null) failureSound.dispose();
        batch = null;
    }
}
