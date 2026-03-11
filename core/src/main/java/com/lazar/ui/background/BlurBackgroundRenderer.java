package com.lazar.ui.background;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.Gdx;

public class BlurBackgroundRenderer {

    private final Texture background;
    private final TextureRegion whiteRegion;
    private final ShaderProgram blurShader;

    private FrameBuffer sceneFbo;
    private FrameBuffer blurFboA;
    private FrameBuffer blurFboB;

    public BlurBackgroundRenderer(Texture background, TextureRegion whiteRegion, ShaderProgram blurShader) {
        this.background = background;
        this.whiteRegion = whiteRegion;
        this.blurShader = blurShader;
        rebuildBuffers();
    }

    public void renderNormal(SpriteBatch batch, OrthographicCamera camera, Viewport viewport) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.setTransformMatrix(new Matrix4().idt());
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        drawBackgroundVignette(batch, viewport);
        batch.end();
    }

    public void renderBlurred(SpriteBatch batch, OrthographicCamera camera, Viewport viewport, float blurAlpha) {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        sceneFbo.begin();
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.setTransformMatrix(new Matrix4().idt());
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(background, 0, 0, worldWidth, worldHeight);
        drawBackgroundVignette(batch, viewport);
        batch.end();
        sceneFbo.end();

        Texture sceneTexture = sceneFbo.getColorBufferTexture();
        sceneTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        blurFboA.begin();
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setShader(blurShader);
        batch.setProjectionMatrix(createScreenProjection());
        batch.setTransformMatrix(new Matrix4().idt());
        batch.begin();
        blurShader.setUniformf("u_dir", 1f / Gdx.graphics.getBackBufferWidth(), 0f);
        batch.setColor(Color.WHITE);
        batch.draw(
            sceneTexture,
            0,
            Gdx.graphics.getBackBufferHeight(),
            Gdx.graphics.getBackBufferWidth(),
            -Gdx.graphics.getBackBufferHeight()
        );
        batch.end();
        blurFboA.end();

        Texture blurTextureA = blurFboA.getColorBufferTexture();
        blurTextureA.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        blurFboB.begin();
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(createScreenProjection());
        batch.setTransformMatrix(new Matrix4().idt());
        batch.begin();
        blurShader.setUniformf("u_dir", 0f, 1f / Gdx.graphics.getBackBufferHeight());
        batch.setColor(Color.WHITE);
        batch.draw(
            blurTextureA,
            0,
            Gdx.graphics.getBackBufferHeight(),
            Gdx.graphics.getBackBufferWidth(),
            -Gdx.graphics.getBackBufferHeight()
        );
        batch.end();
        blurFboB.end();

        batch.setShader(null);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Texture finalBlur = blurFboB.getColorBufferTexture();
        finalBlur.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        batch.setProjectionMatrix(createScreenProjection());
        batch.setTransformMatrix(new Matrix4().idt());
        batch.begin();

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(
            sceneTexture,
            0,
            Gdx.graphics.getBackBufferHeight(),
            Gdx.graphics.getBackBufferWidth(),
            -Gdx.graphics.getBackBufferHeight()
        );

        batch.setColor(1f, 1f, 1f, 0.90f * blurAlpha);
        batch.draw(
            finalBlur,
            0,
            Gdx.graphics.getBackBufferHeight(),
            Gdx.graphics.getBackBufferWidth(),
            -Gdx.graphics.getBackBufferHeight()
        );

        batch.setColor(0.10f, 0.07f, 0.04f, 0.16f * blurAlpha);
        batch.draw(whiteRegion, 0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        batch.end();
    }

    public void resize() {
        rebuildBuffers();
    }

    public void dispose() {
        if (sceneFbo != null) {
            sceneFbo.dispose();
            sceneFbo = null;
        }

        if (blurFboA != null) {
            blurFboA.dispose();
            blurFboA = null;
        }

        if (blurFboB != null) {
            blurFboB.dispose();
            blurFboB = null;
        }
    }

    private void rebuildBuffers() {
        dispose();

        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();

        sceneFbo = new FrameBuffer(Format.RGBA8888, width, height, false);
        blurFboA = new FrameBuffer(Format.RGBA8888, width, height, false);
        blurFboB = new FrameBuffer(Format.RGBA8888, width, height, false);
    }

    private Matrix4 createScreenProjection() {
        return new Matrix4().setToOrtho2D(
            0,
            0,
            Gdx.graphics.getBackBufferWidth(),
            Gdx.graphics.getBackBufferHeight()
        );
    }

    private void drawBackgroundVignette(SpriteBatch batch, Viewport viewport) {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        batch.setColor(0.08f, 0.05f, 0.03f, 0.10f);
        batch.draw(whiteRegion, 0, 0, worldWidth, 90f);
        batch.draw(whiteRegion, 0, worldHeight - 90f, worldWidth, 90f);

        batch.setColor(0.08f, 0.05f, 0.03f, 0.07f);
        batch.draw(whiteRegion, 0, 0, 90f, worldHeight);
        batch.draw(whiteRegion, worldWidth - 90f, 0, 90f, worldHeight);

        batch.setColor(Color.WHITE);
    }
}
