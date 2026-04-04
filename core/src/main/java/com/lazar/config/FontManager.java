package com.lazar.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private static final String FONT_PATH = "fonts/medieval.ttf";

    private static final String FONT_CHARS =
        FreeTypeFontGenerator.DEFAULT_CHARS +
            "ĂÂÎȘȚăâîșțŞŢşţ" +
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final Map<String, BitmapFont> cache = new HashMap<>();
    public static BitmapFont get(int size, Color color) {
        String key = size + "_" + color.toString();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.characters = FONT_CHARS;
        parameter.kerning = true;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        font.setUseIntegerPositions(false);
        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );
        }

        cache.put(key, font);
        return font;
    }

    public static void dispose() {
        for (BitmapFont font : cache.values()) {
            font.dispose();
        }
        cache.clear();
    }
}
