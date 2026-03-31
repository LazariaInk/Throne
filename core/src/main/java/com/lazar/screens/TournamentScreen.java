package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lazar.StartGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TournamentScreen implements Screen {
    private static class RingData {
        float radius, gapCenterDeg, gapSizeDeg;

        RingData(float r, float g, float s) {
            radius = r;
            gapCenterDeg = g;
            gapSizeDeg = s;
        }
    }

    private enum KnightState {IDLE, STANDING, GALLOP, DEATH}

    private static class KnightAnimator {
        Texture[] idle, standing, gallop, death;
        KnightState state = KnightState.IDLE;
        float frameTimer = 0f;
        int frameIndex = 0;

        static final float FPS_IDLE = 1f / 8f;
        static final float FPS_STANDING = 1f / 8f;
        static final float FPS_GALLOP = 1f / 10f;
        static final float FPS_DEATH = 1f / 8f;

        interface OnFinished {
            void run();
        }

        OnFinished onFinished;

        boolean finished = false;

        void setState(KnightState next) {
            if (state == next) return;
            state = next;
            frameIndex = 0;
            frameTimer = 0f;
            finished = false;
        }

        void update(float delta) {
            float fps;
            if (state == KnightState.IDLE) fps = FPS_IDLE;
            else if (state == KnightState.STANDING) fps = FPS_STANDING;
            else if (state == KnightState.GALLOP) fps = FPS_GALLOP;
            else fps = FPS_DEATH;
            Texture[] frames = framesFor(state);
            if (frames == null || frames.length == 0) return;

            frameTimer += delta;
            if (frameTimer >= fps) {
                frameTimer -= fps;
                boolean looping = (state == KnightState.IDLE);
                if (frameIndex < frames.length - 1) {
                    frameIndex++;
                } else if (looping) {
                    frameIndex = 0;
                } else if (!finished) {
                    finished = true;
                    if (onFinished != null) onFinished.run();
                }
            }
        }

        Texture currentFrame() {
            Texture[] frames = framesFor(state);
            if (frames == null || frames.length == 0) return null;
            return frames[Math.min(frameIndex, frames.length - 1)];
        }

        Texture[] framesFor(KnightState s) {
            if (s == KnightState.IDLE) return idle;
            if (s == KnightState.STANDING) return standing;
            if (s == KnightState.GALLOP) return gallop;
            return death;
        }

        void dispose() {
            disposeArr(idle);
            disposeArr(standing);
            disposeArr(gallop);
            disposeArr(death);
        }

        private void disposeArr(Texture[] arr) {
            if (arr == null) return;
            for (Texture t : arr) if (t != null) t.dispose();
        }
    }

    private final StartGame game;
    private final Screen previousScreen;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font, smallFont;
    private GlyphLayout layout;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture backgroundTexture;
    private KnightAnimator knightLeft;
    private KnightAnimator knightRight;
    private boolean playerWon = false;
    private int standingDoneCount = 0;
    private int gallopDoneCount = 0;
    private final List<RingData> rings = new ArrayList<>();
    private final Random random = new Random();
    private float worldWidth, worldHeight;
    private float cardX, cardY, cardW, cardH;
    private float mazeCenterX, mazeCenterY;
    private int currentRingIndex;
    private float ballAngleDeg;
    private final float baseAngularSpeedDeg = 92f;
    private float jumpTimer = 0f;
    private final float jumpDuration = 0.22f;
    private float jumpStrength = 0f;
    private float successFlashTimer = 0f;
    private float failFlashTimer = 0f;
    private float infoPulseTimer = 0f;
    private boolean mazeWon = false; // maze cleared
    private boolean started = true;

    public TournamentScreen(StartGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        smallFont = new BitmapFont();
        layout = new GlyphLayout();

        font.getData().setScale(1.15f);
        smallFont.getData().setScale(0.95f);
        font.setColor(new Color(0.20f, 0.13f, 0.07f, 1f));
        smallFont.setColor(new Color(0.95f, 0.92f, 0.84f, 1f));

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(640, 360, 0);
        camera.update();

        backgroundTexture = new Texture(Gdx.files.internal("images/tournament-bg.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        knightLeft = new KnightAnimator();
        knightRight = new KnightAnimator();

        loadKnightTextures(knightLeft);
        loadKnightTextures(knightRight);

        rebuildLayout();
        buildNewMaze();
    }

    private void loadKnightTextures(KnightAnimator k) {
        k.idle = loadFrames("images/knight/Idle/idle-", 5);
        k.standing = loadFrames("images/knight/Standings/standing-", 10);
        k.gallop = loadFrames("images/knight/Gallop/gallop-", 5);
        k.death = loadFrames("images/knight/Death/death-", 11);
    }

    private Texture[] loadFrames(String prefix, int count) {
        Texture[] arr = new Texture[count];
        for (int i = 0; i < count; i++) {
            arr[i] = new Texture(Gdx.files.internal(prefix + (i + 1) + ".png"));
            arr[i].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return arr;
    }

    private void rebuildLayout() {
        worldWidth = viewport.getWorldWidth();
        worldHeight = viewport.getWorldHeight();

        cardW = 480f;
        cardH = 240f;
        cardX = worldWidth * 0.27f - cardW / 2f;
        cardY = worldHeight / 2f - cardH / 2f;

        mazeCenterX = cardX + cardW + 200f;
        mazeCenterY = worldHeight / 2f;
    }

    private void buildNewMaze() {
        rings.clear();

        int ringCount = 5 + random.nextInt(2);
        float outerRadius = 120f;
        float spacing = 18f;

        float prevGapCenter = random.nextFloat() * 360f;

        for (int i = 0; i < ringCount; i++) {
            float radius = outerRadius - i * spacing;
            float gapSize = Math.max(22f, 38f - i * 3f + random.nextFloat() * 4f);

            float offset = 40f + random.nextFloat() * 140f;
            if (random.nextBoolean()) offset *= -1f;

            float gapCenter = normalizeAngle(prevGapCenter + offset);
            rings.add(new RingData(radius, gapCenter, gapSize));
            prevGapCenter = gapCenter;
        }

        currentRingIndex = 0;
        ballAngleDeg = normalizeAngle(rings.get(0).gapCenterDeg + 110f + random.nextFloat() * 80f);

        jumpTimer = 0f;
        jumpStrength = 0f;
        successFlashTimer = 0f;
        failFlashTimer = 0f;
        infoPulseTimer = 0f;
        mazeWon = false;
        playerWon = false;
        standingDoneCount = 0;
        gallopDoneCount = 0;
        started = true;

        knightLeft.setState(KnightState.IDLE);
        knightLeft.onFinished = null;
        knightRight.setState(KnightState.IDLE);
        knightRight.onFinished = null;
    }

    @Override
    public void render(float delta) {
        handleInput();
        update(delta);

        Gdx.gl.glClearColor(0.08f, 0.06f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        drawBackground();
        drawSpriteCard();
        drawMazeOnly();
        drawOverlayTexts();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(previousScreen);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            buildNewMaze();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            onSpacePressed();
        }
    }

    private void update(float delta) {
        infoPulseTimer += delta;
        if (successFlashTimer > 0f) successFlashTimer -= delta;
        if (failFlashTimer > 0f) failFlashTimer -= delta;
        if (jumpTimer > 0f) jumpTimer -= delta;

        if (!mazeWon && started && currentRingIndex < rings.size()) {
            float speed = baseAngularSpeedDeg + currentRingIndex * 12f;
            ballAngleDeg = normalizeAngle(ballAngleDeg - speed * delta);
        }

        knightLeft.update(delta);
        knightRight.update(delta);
    }

    private void onSpacePressed() {
        if (mazeWon) return;
        if (!started || currentRingIndex >= rings.size()) return;

        RingData ring = rings.get(currentRingIndex);
        float dist = angularDistance(ballAngleDeg, ring.gapCenterDeg);
        float allowed = ring.gapSizeDeg * 0.5f + 4f;

        if (dist <= allowed) {
            successFlashTimer = 0.20f;
            jumpTimer = 0.18f;
            jumpStrength = -10f;
            currentRingIndex++;

            if (currentRingIndex >= rings.size()) {
                mazeWon = true;
                playerWon = true;
                startPostMazeSequence();
            }
        } else {
            failFlashTimer = 0.28f;
            jumpTimer = jumpDuration;
            jumpStrength = 22f;

            float knock = 38f + random.nextFloat() * 42f;
            if (random.nextBoolean()) knock *= -1f;
            ballAngleDeg = normalizeAngle(ballAngleDeg + knock);
        }
    }

    private void startPostMazeSequence() {
        standingDoneCount = 0;
        gallopDoneCount = 0;

        knightLeft.setState(KnightState.STANDING);
        knightLeft.onFinished = this::onStandingDone;

        knightRight.setState(KnightState.STANDING);
        knightRight.onFinished = this::onStandingDone;
    }

    private void onStandingDone() {
        standingDoneCount++;
        if (standingDoneCount < 2) return;

        gallopDoneCount = 0;

        knightLeft.setState(KnightState.GALLOP);
        knightLeft.onFinished = this::onGallopDone;

        knightRight.setState(KnightState.GALLOP);
        knightRight.onFinished = this::onGallopDone;
    }

    private void onGallopDone() {
        gallopDoneCount++;
        if (gallopDoneCount < 2) return;

        KnightAnimator winner = playerWon ? knightLeft : knightRight;
        KnightAnimator loser = playerWon ? knightRight : knightLeft;

        winner.setState(KnightState.STANDING);
        loser.setState(KnightState.DEATH);

        final int[] doneCount = {0};
        Runnable bothDone = () -> {
            doneCount[0]++;
            if (doneCount[0] < 2) return;
            knightLeft.setState(KnightState.IDLE);
            knightLeft.onFinished = null;
            knightRight.setState(KnightState.IDLE);
            knightRight.onFinished = null;
        };

        winner.onFinished = bothDone::run;
        loser.onFinished = bothDone::run;
    }

    private void drawBackground() {
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

        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(0f, 0f, 0f, 0.18f);
        batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawSpriteCard() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0f, 0f, 0f, 0.22f);
        shapeRenderer.rect(cardX + 8f, cardY - 8f, cardW, cardH);

        shapeRenderer.setColor(new Color(0.23f, 0.15f, 0.08f, 0.96f));
        shapeRenderer.rect(cardX, cardY, cardW, cardH);

        shapeRenderer.setColor(new Color(0.93f, 0.87f, 0.73f, 0.97f));
        shapeRenderer.rect(cardX + 5f, cardY + 5f, cardW - 10f, cardH - 10f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.10f);
        shapeRenderer.rect(cardX + 14f, cardY + cardH - 14f, cardW - 28f, 4f);

        float divX = cardX + cardW / 2f;
        shapeRenderer.setColor(new Color(0.35f, 0.22f, 0.12f, 0.35f));
        shapeRenderer.rectLine(divX, cardY + 16f, divX, cardY + cardH - 16f, 2f);

        shapeRenderer.end();

        float halfW = (cardW - 10f) / 2f;
        float innerY = cardY + 5f;
        float innerH = cardH - 10f;

        float kH = innerH * 0.85f;
        float kW = kH;

        float leftKnightY = innerY + (innerH - kH) * 0.5f;

        float rightKnightX = divX + halfW * 0.5f - kW * 0.5f;
        float rightKnightY = innerY + (innerH - kH) * 0.5f;

        batch.begin();

        Texture leftFrame = knightLeft.currentFrame();
        if (leftFrame != null) {
            float aspect = (float) leftFrame.getWidth() / leftFrame.getHeight();
            float drawW = kH * aspect;
            float drawX = cardX + 5f + halfW * 0.5f - drawW * 0.5f;
            batch.draw(leftFrame, drawX, leftKnightY, drawW, kH);
        }

        Texture rightFrame = knightRight.currentFrame();
        if (rightFrame != null) {
            float aspect = (float) rightFrame.getWidth() / rightFrame.getHeight();
            float drawW = kH * aspect;
            batch.draw(rightFrame,
                divX + halfW * 0.5f + drawW * 0.5f,
                rightKnightY,
                -drawW, kH);
        }

        layout.setText(smallFont, "VS");
        smallFont.setColor(new Color(0.36f, 0.24f, 0.12f, 0.80f));
        smallFont.draw(batch, layout, divX - layout.width / 2f, cardY + 24f);

        batch.end();
    }

    private void drawMazeOnly() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float haloAlpha = mazeWon
            ? 0.18f + 0.05f * MathUtils.sin(infoPulseTimer * 3f)
            : 0.08f;
        shapeRenderer.setColor(0.75f, 0.58f, 0.22f, haloAlpha);
        shapeRenderer.circle(mazeCenterX, mazeCenterY, 148f, 80);

        for (int i = 0; i < rings.size(); i++) {
            RingData ring = rings.get(i);
            float thickness = (i == currentRingIndex && !mazeWon) ? 8f : 6f;
            Color ringColor = new Color(0.35f, 0.23f, 0.12f, 1f);

            if (i < currentRingIndex) ringColor = new Color(0.46f, 0.32f, 0.16f, 0.95f);
            if (successFlashTimer > 0f && i == currentRingIndex)
                ringColor = new Color(0.62f, 0.52f, 0.18f, 1f);

            drawRingWithGap(mazeCenterX, mazeCenterY,
                ring.radius, thickness,
                ring.gapCenterDeg, ring.gapSizeDeg,
                ringColor);
        }

        shapeRenderer.setColor(mazeWon
            ? new Color(0.78f, 0.62f, 0.20f, 1f)
            : new Color(0.42f, 0.28f, 0.15f, 0.9f));
        shapeRenderer.circle(mazeCenterX, mazeCenterY, 11f, 40);
        shapeRenderer.setColor(new Color(0.93f, 0.87f, 0.73f, 1f));
        shapeRenderer.circle(mazeCenterX, mazeCenterY, 5f, 32);

        if (!mazeWon && currentRingIndex < rings.size()) {
            float ballRadius = rings.get(currentRingIndex).radius;
            float hop = 0f;
            if (jumpTimer > 0f) {
                float t = MathUtils.clamp(1f - (jumpTimer / jumpDuration), 0f, 1f);
                hop = MathUtils.sin(t * MathUtils.PI) * jumpStrength;
            }
            float finalBallRadius = ballRadius + hop;
            float ballX = mazeCenterX + MathUtils.cosDeg(ballAngleDeg) * finalBallRadius;
            float ballY = mazeCenterY + MathUtils.sinDeg(ballAngleDeg) * finalBallRadius;

            Color ballColor = new Color(0.62f, 0.12f, 0.10f, 1f);
            if (successFlashTimer > 0f) ballColor = new Color(0.16f, 0.55f, 0.18f, 1f);
            if (failFlashTimer > 0f) ballColor = new Color(0.76f, 0.18f, 0.08f, 1f);

            shapeRenderer.setColor(0f, 0f, 0f, 0.20f);
            shapeRenderer.circle(ballX + 3f, ballY - 3f, 9f, 28);
            shapeRenderer.setColor(ballColor);
            shapeRenderer.circle(ballX, ballY, 9f, 28);
            shapeRenderer.setColor(new Color(1f, 0.92f, 0.92f, 0.36f));
            shapeRenderer.circle(ballX - 2f, ballY + 2f, 2.5f, 18);
        }

        shapeRenderer.end();
    }

    private void drawOverlayTexts() {
        batch.begin();

        smallFont.setColor(new Color(0.95f, 0.92f, 0.84f, 0.95f));
        smallFont.draw(batch, "SPACE • R • ESC", 30f, worldHeight - 24f);

        if (mazeWon) {
            layout.setText(font, "Victory!");
            font.setColor(new Color(0.95f, 0.88f, 0.55f, 1f));
            font.draw(batch, layout, mazeCenterX - layout.width / 2f, mazeCenterY + 175f);

            layout.setText(smallFont, "Press R to play again");
            smallFont.setColor(new Color(0.88f, 0.84f, 0.70f, 0.80f));
            smallFont.draw(batch, layout, mazeCenterX - layout.width / 2f, mazeCenterY - 155f);

        } else if (failFlashTimer > 0f) {
            layout.setText(smallFont, "Miss!");
            smallFont.setColor(new Color(0.88f, 0.24f, 0.14f, 0.95f));
            smallFont.draw(batch, layout, mazeCenterX - layout.width / 2f, mazeCenterY + 175f);

        } else if (successFlashTimer > 0f) {
            layout.setText(smallFont, "Good!");
            smallFont.setColor(new Color(0.20f, 0.60f, 0.20f, 0.95f));
            smallFont.draw(batch, layout, mazeCenterX - layout.width / 2f, mazeCenterY + 175f);
        }

        batch.end();
    }

    private void drawRingWithGap(float cx, float cy, float radius, float thickness,
                                 float gapCenterDeg, float gapSizeDeg, Color color) {
        shapeRenderer.setColor(color);

        float startGap = normalizeAngle(gapCenterDeg - gapSizeDeg / 2f);
        float endGap = normalizeAngle(gapCenterDeg + gapSizeDeg / 2f);
        float step = 3f;

        for (float a = 0; a < 360f; a += step) {
            float mid = normalizeAngle(a + step * 0.5f);
            if (isAngleInsideGap(mid, startGap, endGap)) continue;

            float x1 = cx + MathUtils.cosDeg(a) * radius;
            float y1 = cy + MathUtils.sinDeg(a) * radius;
            float x2 = cx + MathUtils.cosDeg(a + step) * radius;
            float y2 = cy + MathUtils.sinDeg(a + step) * radius;
            shapeRenderer.rectLine(x1, y1, x2, y2, thickness);
        }

        float gx1 = cx + MathUtils.cosDeg(startGap) * radius;
        float gy1 = cy + MathUtils.sinDeg(startGap) * radius;
        float gx2 = cx + MathUtils.cosDeg(endGap) * radius;
        float gy2 = cy + MathUtils.sinDeg(endGap) * radius;

        shapeRenderer.setColor(new Color(0.72f, 0.58f, 0.20f, 0.20f));
        shapeRenderer.circle(gx1, gy1, 2.5f, 16);
        shapeRenderer.circle(gx2, gy2, 2.5f, 16);
    }

    private boolean isAngleInsideGap(float angle, float start, float end) {
        angle = normalizeAngle(angle);
        start = normalizeAngle(start);
        end = normalizeAngle(end);
        if (start <= end) return angle >= start && angle <= end;
        return angle >= start || angle <= end;
    }

    private float normalizeAngle(float deg) {
        deg %= 360f;
        if (deg < 0f) deg += 360f;
        return deg;
    }

    private float angularDistance(float a, float b) {
        float diff = Math.abs(normalizeAngle(a) - normalizeAngle(b));
        return Math.min(diff, 360f - diff);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        rebuildLayout();
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
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (knightLeft != null) knightLeft.dispose();
        if (knightRight != null) knightRight.dispose();
    }
}
