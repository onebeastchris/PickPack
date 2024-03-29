package net.onebeastchris.geyser.extension.pickpack.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.geysermc.geyser.api.extension.Extension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;

public class ConfigLoader {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static <T> T load(Extension extension, Class<?> extensionClass, Class<T> configClass) {
        File configFile = extension.dataFolder().resolve("config.yml").toFile();

        // Ensure the data folder exists
        if (!extension.dataFolder().toFile().exists()) {
            if (!extension.dataFolder().toFile().mkdirs()) {
                extension.logger().error("Failed to create data folder");
                return null;
            }
        }

        // Create the config file if it doesn't exist
        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(new File(extensionClass.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath(), Collections.emptyMap())) {
                    try (InputStream input = Files.newInputStream(fileSystem.getPath("config.yml"))) {
                        byte[] bytes = new byte[input.available()];
                        input.read(bytes);
                        writer.write(new String(bytes).toCharArray());
                        writer.flush();
                    }
                }
            } catch (IOException | URISyntaxException e) {
                extension.logger().error("Failed to create config", e);
                return null;
            }
        }

        // Load the config file
        try {
            return new ObjectMapper(new YAMLFactory())
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
                    .disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
                    .readValue(configFile, configClass);
        } catch (IOException e) {
            extension.logger().error("Failed to load config", e);
            return null;
        }
    }

    public record Config(
            @JsonProperty("address") String address,
            @JsonProperty("port") int port,
            @JsonProperty("show-pack-descriptions") boolean showPackDescription,
            @JsonProperty("use-transfer-packet") boolean useTransferPacket,
            @JsonProperty("menu-permission") String menuPermission,
            @JsonProperty("default-permission") String defaultPermission,
            @JsonProperty("default-locale") String defaultLocale,
            @JsonProperty("reload-permission") String reloadPermission,
            @JsonProperty("translations") Translations translations
    ) {
        public record Translations(
                @JsonProperty("menu-command-description") String menuCommandDescription,
                @JsonProperty("reset-command-description") String resetCommandDescription,
                @JsonProperty("reload-command-description") String reloadCommandDescription,

                // Test for outdated config
                @JsonProperty("main-menu-title") String outdatedConfigTest
        ) {}
    }
}