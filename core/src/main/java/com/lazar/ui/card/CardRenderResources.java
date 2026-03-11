package com.lazar.ui.card;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


public class CardRenderResources {
    public final TextureRegion whiteRegion;
    public final BitmapFont titleFont;
    public final BitmapFont bodyFont;
    public final ShaderProgram ovalMaskShader;

    public CardRenderResources(
        TextureRegion whiteRegion,
        BitmapFont titleFont,
        BitmapFont bodyFont,
        ShaderProgram ovalMaskShader
    ) {
        this.whiteRegion = whiteRegion;
        this.titleFont = titleFont;
        this.bodyFont = bodyFont;
        this.ovalMaskShader = ovalMaskShader;
    }
}
