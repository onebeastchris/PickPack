package net.onebeastchris.geyser.extension.pickpack;

import net.onebeastchris.geyser.extension.pickpack.Util.ConfigLoader;
import net.onebeastchris.geyser.extension.pickpack.Util.FileSaveUtil;
import net.onebeastchris.geyser.extension.pickpack.Util.ResourcePackLoader;
import net.onebeastchris.geyser.extension.pickpack.Util.PlayerStorage;
import net.onebeastchris.geyser.extension.pickpack.Util.Form;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionLoadResourcePacksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.command.GeyserCommandSource;

import java.nio.file.Path;
import java.util.List;

public class PickPack implements Extension {
    public static ResourcePackLoader loader;
    public static PlayerStorage storage;
    public static Path storagePath;

    public static ConfigLoader.Config config;
    public static ExtensionLogger logger;

    private Path optInPath;
    private Path optOutPath;

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        // need to load config early so commands can get their translations
        config = ConfigLoader.load(this, this.getClass(), ConfigLoader.Config.class);
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        optInPath = this.dataFolder().resolve("optIn");
        optOutPath = this.dataFolder().resolve("optOut");
        storagePath = this.dataFolder().resolve("cache");

        logger = this.logger();

        FileSaveUtil.makeDir(optInPath, "opt-in-packs");
        FileSaveUtil.makeDir(optOutPath, "opt-out-packs");
        FileSaveUtil.makeDir(storagePath, "storage");

        loader = new ResourcePackLoader(optOutPath, optInPath);
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
    public void CommandEvent(GeyserDefineCommandsEvent commandsEvent) {
        commandsEvent.register(Command.builder(this)
                .name("menu")
                .aliases(List.of("list"))
                .bedrockOnly(true)
                .source(GeyserConnection.class)
                .description(config.translations().menuCommandDescription())
                .executableOnConsole(false)
                .suggestedOpOnly(false)
                .permission(config.menuPermission())
                .executor((source, command, args) -> {
                    Form form = new Form();
                    form.send((GeyserConnection) source, args);
                })
                .build());

        commandsEvent.register(Command.builder(this)
                .name("reset")
                .aliases(List.of("default"))
                .bedrockOnly(true)
                .source(GeyserConnection.class)
                .description(config.translations().resetCommandDescription())
                .executableOnConsole(false)
                .suggestedOpOnly(false)
                .permission(config.defaultPermission())
                .executor((source, command, args) -> {
                    Form form = new Form();
                    form.send((GeyserConnection) source, "clear");
                })
                .build());

        commandsEvent.register(Command.builder(this)
                .name("reload")
                .bedrockOnly(false)
                .source(GeyserCommandSource.class)
                .description(config.translations().reloadCommandDescription())
                .executableOnConsole(true)
                .suggestedOpOnly(true)
                .permission(config.reloadPermission())
                .executor((source, command, args) -> {
                    loader.reload(optOutPath, optInPath);
                    config = ConfigLoader.load(this, this.getClass(), ConfigLoader.Config.class);
                    source.sendMessage(config.translations().reloadCommandSuccess());
                })
                .build());
    }
}
