package net.onebeastchris.geyser.extension.pickpack.Util;

import net.onebeastchris.geyser.extension.pickpack.PickPack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LanguageManager {

    private static final String EN_US_PROPERTIES = "en_us.properties";
    public static String DEFAULT_LOCALE;
    public static Map<String, Properties> LOCALE_PROPERTIES = new HashMap<>();

    @SuppressWarnings("resource")
    public static void init(Path languageFolder) throws IOException {

        // Ensure it exists
        if (!languageFolder.toFile().exists()) {
            if (!languageFolder.toFile().mkdirs()) {
                throw new RuntimeException("Failed to create language folder!");
            }
        }

        List<Path> languageFiles;
        try {
            languageFiles = new ArrayList<>(Files.list(languageFolder).toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list language files!", e);
        }

        DEFAULT_LOCALE = PickPack.config.defaultLocale() == null ? EN_US_PROPERTIES : PickPack.config.defaultLocale() + ".properties";

        //Check: Does english exist?
        String currentDefaultLocale = DEFAULT_LOCALE;
        if (languageFiles.stream().noneMatch(path -> path.getFileName().toString().equalsIgnoreCase(currentDefaultLocale))) {
            // Check: Is default locale not english?
            if (!DEFAULT_LOCALE.equalsIgnoreCase(EN_US_PROPERTIES)) {
                PickPack.logger.warning("Default configured locale " + DEFAULT_LOCALE + " not found, falling back to en_us.properties");
                DEFAULT_LOCALE = EN_US_PROPERTIES;
            }

            try (InputStream input = PickPack.class.getClassLoader().getResourceAsStream(EN_US_PROPERTIES)) {
                assert input != null;
                Path defaultLocalePath = languageFolder.resolve(EN_US_PROPERTIES);
                Files.copy(input, defaultLocalePath);
                languageFiles.add(defaultLocalePath);
            }
        }

        for (Path languageFile : languageFiles) {
            if (!languageFile.toFile().isFile()) {
                continue;
            }

            String fileName = languageFile.getFileName().toString();
            if (!fileName.endsWith(".properties")) {
                continue;
            }

            // Load the locale
            try (InputStream localeStream = Files.newInputStream(languageFile)) {
                Properties localeProp = new Properties();
                try (InputStreamReader reader = new InputStreamReader(localeStream, StandardCharsets.UTF_8)) {
                    localeProp.load(reader);
                } catch (Exception e) {
                    throw new AssertionError("Failed to load locale " + fileName);
                }

                // Insert the locale into the mappings, all lowercase
                LOCALE_PROPERTIES.put(fileName.substring(0, 5).toLowerCase(), localeProp);
            }
        }
    }

    public static String getLocaleString(String locale, String key) {
        return LOCALE_PROPERTIES.getOrDefault(locale.toLowerCase(), LOCALE_PROPERTIES.get(DEFAULT_LOCALE)).getProperty(key);
    }
}
