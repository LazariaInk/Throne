package com.lazar.ui.card;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.lazar.model.Consequence;
import com.lazar.model.EventCard;

public class CardRenderer {

    private final CardRenderResources resources;
    private final GlyphLayout layout = new GlyphLayout();

    public CardRenderer(CardRenderResources resources) {
        this.resources = resources;
    }

    public void drawFront(
        SpriteBatch batch,
        EventCard event,
        Texture image,
        CardTransform transform,
        boolean isFrontCard
    ) {
        Matrix4 oldTransform = applyCardTransform(batch, transform);

        drawCardFaceLocal(batch, transform.alpha, transform.width, transform.height);

        float cardWidth = transform.width;
        float cardHeight = transform.height;
        float cardX = -cardWidth / 2f;
        float cardY = -cardHeight / 2f;

        float paddingX = 22f;
        float paddingTop = 22f;
        float paddingBottom = 24f;

        float ovalW = Math.min(200f, cardWidth - 80f);
        float ovalH = 210f;
        float ovalX = -ovalW / 2f;
        float ovalY = cardY + cardHeight - paddingTop - ovalH;

        batch.setColor(0f, 0f, 0f, 0.10f * transform.alpha);
        batch.draw(resources.whiteRegion, ovalX + 4f, ovalY - 4f, ovalW, ovalH);

        batch.setColor(0.28f, 0.18f, 0.09f, 1f * transform.alpha);
        batch.draw(resources.whiteRegion, ovalX - 4f, ovalY - 4f, ovalW + 8f, ovalH + 8f);

        batch.setColor(0.83f, 0.76f, 0.62f, 1f * transform.alpha);
        batch.draw(resources.whiteRegion, ovalX, ovalY, ovalW, ovalH);

        drawOvalMaskedImage(batch, image, ovalX, ovalY, ovalW, ovalH, transform.alpha);

        if (isFrontCard) {
            batch.setColor(1f, 0.96f, 0.85f, 0.06f * transform.alpha);
            batch.draw(resources.whiteRegion, cardX + 12f, cardY + cardHeight - 58f, cardWidth - 24f, 26f);
        }

        float textWidth = cardWidth - paddingX * 2f;

        // TITLU
        float titleTopY = ovalY - 12f;
        float titleMaxHeight = 52f;

        resources.titleFont.setColor(0.14f, 0.08f, 0.04f, transform.alpha);
        drawFittedWrappedText(
            batch,
            resources.titleFont,
            event.title,
            cardX + paddingX,
            titleTopY,
            textWidth,
            titleMaxHeight,
            Align.center,
            transform.alpha,
            1.0f,
            0.72f
        );

        // BODY
        float bodyTopY = ovalY - 72f;
        float bodyMaxHeight = bodyTopY - (cardY + paddingBottom);

        resources.bodyFont.setColor(0.18f, 0.11f, 0.06f, transform.alpha);
        drawFittedWrappedText(
            batch,
            resources.bodyFont,
            event.description,
            cardX + paddingX,
            bodyTopY,
            textWidth,
            bodyMaxHeight,
            Align.left,
            transform.alpha,
            1.0f,
            0.68f
        );

        restoreTransform(batch, oldTransform);
    }

    public void drawBack(
        SpriteBatch batch,
        EventCard event,
        Consequence consequence,
        CardTransform transform
    ) {
        Matrix4 oldTransform = applyCardTransform(batch, transform);

        drawCardFaceLocal(batch, transform.alpha, transform.width, transform.height);

        float cardWidth = transform.width;
        float cardHeight = transform.height;
        float cardX = -cardWidth / 2f;
        float cardY = -cardHeight / 2f;

        float paddingX = 24f;
        float topY = cardY + cardHeight;

        String title = consequence != null && consequence.title != null && !consequence.title.isEmpty()
            ? consequence.title
            : "Consecinta";
        String body = consequence != null && consequence.text != null && !consequence.text.isEmpty()
            ? consequence.text
            : "Consilierii delibereaza asupra hotararii tale.";
        //String statsLine = buildStatsLine(consequence);

        batch.setColor(0.32f, 0.21f, 0.11f, 0.12f * transform.alpha);
        batch.draw(resources.whiteRegion, cardX + 20f, topY - 96f, cardWidth - 40f, 54f);

        batch.setColor(0.55f, 0.40f, 0.22f, 0.16f * transform.alpha);
        batch.draw(resources.whiteRegion, cardX + 28f, topY - 124f, cardWidth - 56f, 2f);

        // TITLU
        resources.titleFont.setColor(0.14f, 0.08f, 0.04f, transform.alpha);
        drawFittedWrappedText(
            batch,
            resources.titleFont,
            title,
            cardX + 22f,
            topY - 42f,
            cardWidth - 44f,
            48f,
            Align.center,
            transform.alpha,
            1.0f,
            0.72f
        );

        // BOX FIX JOS
        float boxHeight = 84f;
        float boxY = cardY + 32f;

        batch.setColor(0.60f, 0.48f, 0.31f, 0.18f * transform.alpha);
        batch.draw(resources.whiteRegion, cardX + 28f, boxY, cardWidth - 56f, boxHeight);

        batch.setColor(0.42f, 0.28f, 0.14f, 0.22f * transform.alpha);
        batch.draw(resources.whiteRegion, cardX + 28f, boxY + boxHeight - 2f, cardWidth - 56f, 2f);

        resources.bodyFont.setColor(0.16f, 0.10f, 0.05f, transform.alpha);
        layout.setText(
            resources.bodyFont,
            "Urmarile asupra regatului",
            resources.bodyFont.getColor(),
            cardWidth - 72f,
            Align.center,
            true
        );
        resources.bodyFont.draw(batch, layout, cardX + 36f, boxY + boxHeight - 12f);

        layout.setText(
            resources.bodyFont,
            "",
            resources.bodyFont.getColor(),
            cardWidth - 80f,
            Align.center,
            true
        );
        resources.bodyFont.draw(batch, layout, cardX + 40f, boxY + boxHeight - 42f);

        float bodyWidth = cardWidth - 52f;
        float bodyTopY = topY - 132f;
        float bodyBottomLimit = boxY + boxHeight + 18f;
        float bodyMaxHeight = bodyTopY - bodyBottomLimit;

        resources.bodyFont.setColor(0.22f, 0.14f, 0.08f, transform.alpha);
        drawFittedWrappedText(
            batch,
            resources.bodyFont,
            body,
            cardX + paddingX,
            bodyTopY,
            bodyWidth,
            bodyMaxHeight,
            Align.left,
            transform.alpha,
            1.0f,
            0.65f
        );

        batch.setColor(0.42f, 0.28f, 0.14f, 0.20f * transform.alpha);
        batch.draw(resources.whiteRegion, -32f, -2f, 64f, 4f);
        batch.draw(resources.whiteRegion, -2f, -32f, 4f, 64f);

        restoreTransform(batch, oldTransform);
    }

    private void drawFittedWrappedText(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        float x,
        float topY,
        float width,
        float maxHeight,
        int align,
        float alpha,
        float maxScale,
        float minScale
    ) {
        if (text == null || text.isEmpty() || maxHeight <= 0f) {
            return;
        }

        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;

        float scale = maxScale;
        boolean fits = false;

        while (scale >= minScale) {
            font.getData().setScale(scale);
            layout.setText(font, text, font.getColor(), width, align, true);

            if (layout.height <= maxHeight) {
                fits = true;
                break;
            }

            scale -= 0.04f;
        }

        if (!fits) {
            font.getData().setScale(minScale);
            layout.setText(font, text, font.getColor(), width, align, true);
        }

        font.setColor(font.getColor().r, font.getColor().g, font.getColor().b, alpha);
        font.draw(batch, layout, x, topY);

        font.getData().setScale(originalScaleX, originalScaleY);
    }

    private String buildStatsLine(Consequence consequence) {
        if (consequence == null) {
            return "Religie 0 | Popor 0 | Armata 0 | Aur 0";
        }

        return "Religie " + formatDelta(consequence.religion)
            + " | Popor " + formatDelta(consequence.population)
            + " | Armata " + formatDelta(consequence.army)
            + " | Aur " + formatDelta(consequence.money);
    }

    private String formatDelta(int value) {
        if (value > 0) {
            return "+" + value;
        }
        return String.valueOf(value);
    }

    private void drawCardFaceLocal(SpriteBatch batch, float alpha, float cardWidth, float cardHeight) {
        float cardX = -cardWidth / 2f;
        float cardY = -cardHeight / 2f;
        float originX = cardWidth / 2f;
        float originY = cardHeight / 2f;

        batch.setColor(0f, 0f, 0f, 0.16f * alpha);
        batch.draw(
            resources.whiteRegion,
            cardX + 10f, cardY - 12f,
            originX, originY,
            cardWidth, cardHeight,
            1f, 1f,
            0f
        );

        batch.setColor(0.20f, 0.13f, 0.07f, 0.92f * alpha);
        batch.draw(
            resources.whiteRegion,
            cardX - 4f, cardY - 4f,
            originX + 4f, originY + 4f,
            cardWidth + 8f, cardHeight + 8f,
            1f, 1f,
            0f
        );

        batch.setColor(0.95f, 0.89f, 0.76f, 0.98f * alpha);
        batch.draw(
            resources.whiteRegion,
            cardX, cardY,
            originX, originY,
            cardWidth, cardHeight,
            1f, 1f,
            0f
        );

        batch.setColor(0.91f, 0.84f, 0.69f, 0.88f * alpha);
        batch.draw(
            resources.whiteRegion,
            cardX + 10f, cardY + 10f,
            originX - 10f, originY - 10f,
            cardWidth - 20f, cardHeight - 20f,
            1f, 1f,
            0f
        );
    }

    private Matrix4 applyCardTransform(SpriteBatch batch, CardTransform transform) {
        Matrix4 oldTransform = new Matrix4(batch.getTransformMatrix());

        Matrix4 matrix = new Matrix4()
            .idt()
            .translate(transform.x, transform.y, 0f)
            .rotate(0f, 0f, 1f, transform.rotation)
            .scale(transform.visualScaleX * transform.scale, transform.scale, 1f);

        batch.flush();
        batch.setTransformMatrix(matrix);
        return oldTransform;
    }

    private void restoreTransform(SpriteBatch batch, Matrix4 oldTransform) {
        batch.flush();
        batch.setTransformMatrix(oldTransform);
    }

    private void drawOvalMaskedImage(
        SpriteBatch batch,
        Texture texture,
        float x,
        float y,
        float width,
        float height,
        float alpha
    ) {
        if (texture == null) {
            return;
        }

        Texture.TextureFilter min = texture.getMinFilter();
        Texture.TextureFilter mag = texture.getMagFilter();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        ShaderProgram oldShader = batch.getShader();
        batch.flush();
        batch.setShader(resources.ovalMaskShader);

        resources.ovalMaskShader.setUniformf("u_center", 0.5f, 0.5f);
        resources.ovalMaskShader.setUniformf("u_radius", 0.48f, 0.48f);
        resources.ovalMaskShader.setUniformf("u_softness", 0.025f);

        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();

        float scale = Math.max(width / textureWidth, height / textureHeight);
        float drawWidth = textureWidth * scale;
        float drawHeight = textureHeight * scale;
        float drawX = x + (width - drawWidth) / 2f;
        float drawY = y + (height - drawHeight) / 2f;

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, drawX, drawY, drawWidth, drawHeight);

        batch.flush();
        batch.setShader(oldShader);
        texture.setFilter(min, mag);
    }
}
