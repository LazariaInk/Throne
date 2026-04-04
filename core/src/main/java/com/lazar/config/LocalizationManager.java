package com.lazar.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class LocalizationManager {
    private static ResourceBundle bundle;

    public static void loadLanguage(String languageCode) {
        String fileName = "i18n/strings_" + languageCode + ".properties";

        try (InputStream inputStream =
                 LocalizationManager.class.getClassLoader().getResourceAsStream(fileName)) {

            if (inputStream == null) {
                throw new RuntimeException("Nu gasesc fisierul: " + fileName);
            }

            bundle = new PropertyResourceBundle(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            throw new RuntimeException("Eroare la incarcarea limbii: " + fileName, e);
        }
    }

    public static String get(String key) {
        if (bundle == null) {
            throw new IllegalStateException("Limba nu a fost incarcata.");
        }
        return bundle.getString(key);
    }
}
