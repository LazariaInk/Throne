package com.lazar.ui.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.lazar.logic.GameStats;
import com.lazar.model.Consequence;
import com.lazar.model.DecisionResolution;
import com.lazar.model.EventCard;

public class CardPresenter {

    public static final float CARD_BASE_WIDTH = 320f;
    public static final float CARD_BASE_HEIGHT = 420f;

    private final CardRenderer renderer;

    private final CardTransform currentTransform = new CardTransform();

    private EventCard currentEvent;
    private Texture currentEventImage;

    private Consequence resolvedConsequence;
    private GameStats currentStats;

    private boolean waitingForBackend = false;
    private boolean canAdvance = false;

    private boolean isFlipping = false;
    private float flipTime = 0f;
    private final float flipDuration = 0.55f;

    private boolean showingBackFace = false;

    private float blurAlpha = 0f;
    private float blurTargetAlpha = 0f;
    private final float blurFadeSpeed = 2.8f;

    public CardPresenter(CardRenderer renderer) {
        this.renderer = renderer;
    }

    public void showNewEvent(EventCard event, GameStats stats) {
        disposeCurrentImage();

        this.currentEvent = event;
        this.currentStats = stats;
        this.resolvedConsequence = null;
        this.waitingForBackend = false;
        this.canAdvance = false;
        this.isFlipping = false;
        this.flipTime = 0f;
        this.showingBackFace = false;
        this.blurTargetAlpha = 0f;

        if (event != null && event.imagePath != null) {
            currentEventImage = new Texture(Gdx.files.internal(event.imagePath));
            currentEventImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    public void update(float delta) {
        if (isFlipping) {
            flipTime += delta;
            if (flipTime >= flipDuration) {
                flipTime = flipDuration;
                isFlipping = false;
                showingBackFace = true;
                canAdvance = true;
            }
        }

        blurAlpha = MathUtils.lerp(blurAlpha, blurTargetAlpha, blurFadeSpeed * delta);
        if (Math.abs(blurAlpha - blurTargetAlpha) < 0.01f) {
            blurAlpha = blurTargetAlpha;
        }
    }

    public void render(SpriteBatch batch, float worldWidth, float worldHeight) {
        if (currentEvent == null) {
            return;
        }

        currentTransform.x = worldWidth / 2f;
        currentTransform.y = worldHeight / 2f;
        currentTransform.width = CARD_BASE_WIDTH;
        currentTransform.height = CARD_BASE_HEIGHT;
        currentTransform.rotation = 0f;
        currentTransform.alpha = 1f;
        currentTransform.scale = 1f;

        if (isFlipping) {
            float flipProgress = MathUtils.clamp(flipTime / flipDuration, 0f, 1f);
            float visualScaleX = Math.abs((float) Math.cos(flipProgress * MathUtils.PI));
            boolean drawBack = flipProgress >= 0.5f;

            currentTransform.visualScaleX = Math.max(0.04f, visualScaleX);

            if (drawBack) {
                renderer.drawBack(batch, currentEvent, resolvedConsequence, currentTransform);
            } else {
                renderer.drawFront(batch, currentEvent, currentEventImage, currentTransform, true);
            }
            return;
        }

        currentTransform.visualScaleX = 1f;

        if (showingBackFace) {
            renderer.drawBack(batch, currentEvent, resolvedConsequence, currentTransform);
        } else {
            renderer.drawFront(batch, currentEvent, currentEventImage, currentTransform, true);
        }
    }

    public boolean canTypeMessage() {
        return currentEvent != null
            && !waitingForBackend
            && !isFlipping
            && !showingBackFace
            && !canAdvance;
    }

    public boolean canAdvanceCard() {
        return canAdvance && !waitingForBackend && !isFlipping;
    }

    public void markSubmitting() {
        waitingForBackend = true;
        blurTargetAlpha = 1f;
    }

    public void resolveFromBackend(DecisionResolution resolution, GameStats updatedStats) {
        if (resolution == null || resolution.consequence == null) {
            waitingForBackend = false;
            blurTargetAlpha = 0f;
            return;
        }

        this.resolvedConsequence = resolution.consequence;
        this.currentStats = updatedStats;
        this.waitingForBackend = false;
        this.canAdvance = false;

        startFlipToBack();
    }

    public void cancelSubmitting() {
        waitingForBackend = false;
        blurTargetAlpha = 0f;
    }

    public boolean shouldRenderBlurredBackground() {
        return blurAlpha > 0.01f || waitingForBackend || isFlipping || showingBackFace;
    }

    public float getBlurAlpha() {
        return blurAlpha;
    }

    public boolean isWaitingForBackend() {
        return waitingForBackend;
    }

    public GameStats getGameStats() {
        return currentStats;
    }

    public EventCard getCurrentEvent() {
        return currentEvent;
    }

    public void dispose() {
        disposeCurrentImage();
    }

    private void startFlipToBack() {
        if (isFlipping || showingBackFace) {
            return;
        }

        isFlipping = true;
        flipTime = 0f;
        blurTargetAlpha = 1f;
    }

    private void disposeCurrentImage() {
        if (currentEventImage != null) {
            currentEventImage.dispose();
            currentEventImage = null;
        }
    }
}
