package net.onebeastchris.geyser.extension.pickpack;

import net.onebeastchris.geyser.extension.pickpack.util.*;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionLoadResourcePacksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserRegisterPermissionsEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.api.util.TriState;
import org.geysermc.geyser.command.GeyserCommandSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class PickPack implements Extension {

    private static final String version = "(v1.2.3)";

    public static ResourcePackLoader loader;
    public static PlayerStorage storage;
    public static Path storagePath;
    public static ConfigLoader.Config config;
    public static ExtensionLogger logger;

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        logger = this.logger();

        // need to load config early so commands can get their translations
        config = ConfigLoader.load(this, this.getClass(), ConfigLoader.Config.class);

        if (config == null) {
            this.disable();
            throw new RuntimeException("Failed to load config!");
        }

        if (config.translations().outdatedConfigTest() != null) {
            this.logger().warning("Your config is outdated! Please let it regenerate. Changes include:");
            this.logger().warning("- Locale files instead of translations in the config");
            this.logger().warning("- New config option: default-locale");
            this.logger().warning("However, command descriptions are still in the config since those cannot " +
                    "be changed on a player by player basis.");
        }

        try {
            LanguageManager.init(this.dataFolder().resolve("translations"));
        } catch (Exception e) {
            e.printStackTrace();
            this.disable();
            throw new RuntimeException("Failed to load language files!", e);
        }

        logger.info("Started PickPack " + version);
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        Path optionalPacksPath = this.dataFolder().resolve("OptionalPacks");
        Path defaultPacksPath = this.dataFolder().resolve("DefaultPacks");
        storagePath = this.dataFolder().resolve("cache");

        try {
            // Replace opt-in and opt-out directories with new ones
            if (this.dataFolder().resolve("optIn").toFile().exists()) {
                Files.move(this.dataFolder().resolve("optIn"), optionalPacksPath, StandardCopyOption.REPLACE_EXISTING);
            }
            if (this.dataFolder().resolve("optOut").toFile().exists()) {
                Files.move(this.dataFolder().resolve("optOut"), defaultPacksPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.error("Unable to migrate optIn and optOut directories!" + e.getMessage());
            e.printStackTrace();
            this.disable();
        }

        FileSaveUtil.makeDir(optionalPacksPath, "optional packs directory");
        FileSaveUtil.makeDir(defaultPacksPath, "default packs directory");
        FileSaveUtil.makeDir(storagePath, "storage");

        loader = new ResourcePackLoader(optionalPacksPath, defaultPacksPath);
        storage = new PlayerStorage();

        logger.info("PickPack extension loaded!");
    }

    //on player join: send packs if we have any for them
    @Subscribe
    public void onPlayerResourcePackLoadEvent(SessionLoadResourcePacksEvent event) {
        logger.debug("Sending packs to " + event.connection().xuid());
        List<ResourcePack> connectionPacks = storage.getPacks(event.connection().xuid());
        for (ResourcePack pack : connectionPacks) {
            event.register(pack);
        }
    }

    @Subscribe
    public void onPermissionRegistration(GeyserRegisterPermissionsEvent event) {
        // menu permission
        event.register(config.menuPermission(), TriState.TRUE);
        // reset permission
        event.register(config.defaultPermission(), TriState.TRUE);
        // reload permission
        event.register(config.reloadPermission(), TriState.NOT_SET);
    }

    @Subscribe
    public void onCommandEvent(GeyserDefineCommandsEvent commandsEvent) {
        commandsEvent.register(Command.builder(this)
                .name("menu")
                .aliases(List.of("list"))
                .bedrockOnly(true)
                .playerOnly(true)
                .source(GeyserConnection.class)
                .description(config.translations().menuCommandDescription())
                .permission(config.menuPermission())
                .executor((source, command, args) -> {
                    Form form = new Form((GeyserConnection) source);
                    form.send(args);
                })
                .build());

        commandsEvent.register(Command.builder(this)
                .name("reset")
                .aliases(List.of("default"))
                .bedrockOnly(true)
                .playerOnly(true)
                .source(GeyserConnection.class)
                .description(config.translations().resetCommandDescription())
                .permission(config.defaultPermission())
                .executor((source, command, args) -> {
                    Form form = new Form((GeyserConnection) source);
                    form.send("clear");
                })
                .build());

        commandsEvent.register(Command.builder(this)
                .name("reload")
                .source(GeyserCommandSource.class)
                .description(config.translations().reloadCommandDescription())
                .permission(config.reloadPermission())
                .executor((source, command, args) -> {
                    loader.reload();
                    try {
                        config = ConfigLoader.load(this, this.getClass(), ConfigLoader.Config.class);
                        assert config != null;

                        LanguageManager.LOCALE_PROPERTIES.clear();
                        LanguageManager.init(this.dataFolder().resolve("translations"));
                    } catch (Exception e) {
                        source.sendMessage(LanguageManager.getLocaleString(LanguageManager.DEFAULT_LOCALE, "reload.error"));
                    } finally {
                        source.sendMessage(LanguageManager.getLocaleString(LanguageManager.DEFAULT_LOCALE, "reload.success"));
                    }
                })
                .build());
    }
}
