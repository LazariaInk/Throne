package com.lazar.ui.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.lazar.logic.GameStats;
import com.lazar.model.Consequence;
import com.lazar.model.EventCard;

public class CardPresenter {

    public static final float CARD_BASE_WIDTH = 320f;
    public static final float CARD_BASE_HEIGHT = 420f;

    private final Array<EventCard> deck;
    private final CardRenderer renderer;
    private final GameStats gameStats;

    private int currentCardIndex = 0;
    private int nextCardIndex = 1;

    private Texture currentEventImage;
    private Texture nextEventImage;

    private boolean isTransitioning = false;
    private float transitionTime = 0f;
    private final float transitionDuration = 0.60f;

    private boolean isFlipping = false;
    private float flipTime = 0f;
    private final float flipDuration = 0.55f;
    private boolean showingBackFace = false;

    private float blurAlpha = 0f;
    private float blurTargetAlpha = 0f;
    private final float blurFadeSpeed = 2.8f;

    private final CardTransform currentTransform = new CardTransform();
    private final CardTransform nextTransform = new CardTransform();

    private Consequence resolvedConsequence;
    private boolean waitingForBackend = false;

    public CardPresenter(
        Array<EventCard> deck,
        CardRenderer renderer
    ) {
        this.deck = new Array<>(deck);
        this.renderer = renderer;
        this.gameStats = new GameStats();

        loadCurrentAndNextImages();
    }

    public void update(float delta) {
        if (isTransitioning) {
            transitionTime += delta;
            if (transitionTime >= transitionDuration) {
                finishTransition();
            }
        }

        if (isFlipping) {
            flipTime += delta;
            if (flipTime >= flipDuration) {
                flipTime = flipDuration;
                isFlipping = false;
                showingBackFace = true;
            }
        }

        blurAlpha = MathUtils.lerp(blurAlpha, blurTargetAlpha, blurFadeSpeed * delta);
        if (Math.abs(blurAlpha - blurTargetAlpha) < 0.01f) {
            blurAlpha = blurTargetAlpha;
        }

        if (!isTransitioning && !isFlipping && !showingBackFace && blurTargetAlpha > 0f && blurAlpha <= 0.02f) {
            blurTargetAlpha = 0f;
        }
    }

    public void render(SpriteBatch batch, float worldWidth, float worldHeight) {
        float transitionProgress = isTransitioning
            ? MathUtils.clamp(transitionTime / transitionDuration, 0f, 1f)
            : 0f;

        float currentOut = Interpolation.pow3Out.apply(transitionProgress);
        float nextIn = Interpolation.smoother.apply(transitionProgress);

        if (isTransitioning && nextEventImage != null) {
            nextTransform.x = worldWidth / 2f;
            nextTransform.y = worldHeight / 2f - 10f * (1f - nextIn);
            nextTransform.width = CARD_BASE_WIDTH;
            nextTransform.height = CARD_BASE_HEIGHT;
            nextTransform.rotation = -2.5f * (1f - nextIn);
            nextTransform.alpha = 0.55f + 0.45f * nextIn;
            nextTransform.scale = 0.965f + 0.035f * nextIn;
            nextTransform.visualScaleX = 1f;

            renderer.drawFront(batch, deck.get(nextCardIndex), nextEventImage, nextTransform, false);
        }

        if (currentEventImage == null || deck.size == 0) {
            return;
        }

        if (isTransitioning) {
            currentTransform.x = worldWidth / 2f + 190f * currentOut;
            currentTransform.y = worldHeight / 2f + 120f * currentOut;
            currentTransform.width = CARD_BASE_WIDTH;
            currentTransform.height = CARD_BASE_HEIGHT;
            currentTransform.rotation = 9f * currentOut;
            currentTransform.alpha = 1f - currentOut;
            currentTransform.scale = 1f - 0.04f * currentOut;
            currentTransform.visualScaleX = 1f;

            drawCurrentCardFace(batch, currentTransform);
            return;
        }

        if (isFlipping) {
            float flipProgress = MathUtils.clamp(flipTime / flipDuration, 0f, 1f);
            float visualScaleX = Math.abs((float) Math.cos(flipProgress * MathUtils.PI));
            boolean drawBack = flipProgress >= 0.5f;

            currentTransform.x = worldWidth / 2f;
            currentTransform.y = worldHeight / 2f;
            currentTransform.width = CARD_BASE_WIDTH;
            currentTransform.height = CARD_BASE_HEIGHT;
            currentTransform.rotation = 0f;
            currentTransform.alpha = 1f;
            currentTransform.scale = 1f;
            currentTransform.visualScaleX = Math.max(0.04f, visualScaleX);

            if (drawBack) {
                renderer.drawBack(batch, deck.get(currentCardIndex), resolvedConsequence, currentTransform);
            } else {
                renderer.drawFront(batch, deck.get(currentCardIndex), currentEventImage, currentTransform, true);
            }
            return;
        }

        currentTransform.x = worldWidth / 2f;
        currentTransform.y = worldHeight / 2f;
        currentTransform.width = CARD_BASE_WIDTH;
        currentTransform.height = CARD_BASE_HEIGHT;
        currentTransform.rotation = 0f;
        currentTransform.alpha = 1f;
        currentTransform.scale = 1f;
        currentTransform.visualScaleX = 1f;

        if (showingBackFace) {
            renderer.drawBack(batch, deck.get(currentCardIndex), resolvedConsequence, currentTransform);
        } else {
            renderer.drawFront(batch, deck.get(currentCardIndex), currentEventImage, currentTransform, true);
        }
    }

    public boolean canTypeMessage() {
        return !isTransitioning
            && !isFlipping
            && !showingBackFace
            && !waitingForBackend
            && deck.size > 0;
    }

    public boolean canAdvanceCard() {
        return !isTransitioning && !isFlipping && showingBackFace && deck.size > 1;
    }

    public void markSubmitting() {
        waitingForBackend = true;
        blurTargetAlpha = 1f;
    }

    public void resolveFromBackend(Consequence consequence) {
        if (consequence == null || deck.size == 0) {
            waitingForBackend = false;
            blurTargetAlpha = 0f;
            return;
        }

        waitingForBackend = false;
        resolvedConsequence = consequence;
        gameStats.apply(consequence);
        startFlipToBack();
    }

    public void cancelSubmitting() {
        waitingForBackend = false;
        blurTargetAlpha = 0f;
    }

    public void advanceCard() {
        if (deck.size <= 1 || isTransitioning || isFlipping || !showingBackFace) {
            return;
        }

        isTransitioning = true;
        transitionTime = 0f;
        blurTargetAlpha = 1f;
    }

    public boolean shouldRenderBlurredBackground() {
        return blurAlpha > 0.01f || isTransitioning || isFlipping || waitingForBackend;
    }

    public float getBlurAlpha() {
        return blurAlpha;
    }

    public boolean isShowingBackFace() {
        return showingBackFace;
    }

    public boolean isWaitingForBackend() {
        return waitingForBackend;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public EventCard getCurrentEvent() {
        if (deck.size == 0 || currentCardIndex < 0 || currentCardIndex >= deck.size) {
            return null;
        }
        return deck.get(currentCardIndex);
    }

    public void dispose() {
        disposeCardTextures();
    }

    private void drawCurrentCardFace(SpriteBatch batch, CardTransform transform) {
        if (showingBackFace) {
            renderer.drawBack(batch, deck.get(currentCardIndex), resolvedConsequence, transform);
        } else {
            renderer.drawFront(batch, deck.get(currentCardIndex), currentEventImage, transform, true);
        }
    }

    private void startFlipToBack() {
        if (isTransitioning || isFlipping || showingBackFace) {
            return;
        }

        isFlipping = true;
        flipTime = 0f;
        blurTargetAlpha = 1f;
    }

    private void finishTransition() {
        isTransitioning = false;
        transitionTime = 0f;

        currentCardIndex = nextCardIndex;
        nextCardIndex = (currentCardIndex + 1) % deck.size;

        showingBackFace = false;
        resolvedConsequence = null;

        loadCurrentAndNextImages();
        blurTargetAlpha = 0f;
    }

    private void loadCurrentAndNextImages() {
        disposeCardTextures();

        if (deck.size == 0) {
            return;
        }

        currentEventImage = new Texture(Gdx.files.internal(deck.get(currentCardIndex).imagePath));
        currentEventImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        if (deck.size > 1) {
            nextCardIndex = (currentCardIndex + 1) % deck.size;
            nextEventImage = new Texture(Gdx.files.internal(deck.get(nextCardIndex).imagePath));
            nextEventImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    private void disposeCardTextures() {
        if (currentEventImage != null) {
            currentEventImage.dispose();
            currentEventImage = null;
        }

        if (nextEventImage != null) {
            nextEventImage.dispose();
            nextEventImage = null;
        }
    }
}
