package com.lazar.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {

    private static final String FONT_CHARS =
        FreeTypeFontGenerator.DEFAULT_CHARS +
            "ĂÂÎȘȚăâîșțŞŢşţ" +   // română complet
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" + // rusă mare
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"; // rusă mică

    public static BitmapFont createFont(String path, int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = size;
        parameter.color = color;
        parameter.characters = FONT_CHARS;

        // 🔥 IMPORTANT (evită buguri cu glyph-uri lipsă)
        parameter.incremental = false;

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }
}
