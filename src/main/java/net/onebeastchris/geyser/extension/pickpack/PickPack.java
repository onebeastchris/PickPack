package net.onebeastchris.geyser.extension.pickpack;

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
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.pack.ResourcePack;

import java.nio.file.Path;
import java.util.List;

public class PickPack implements Extension {
    public static ResourcePackLoader loader;
    public static PlayerStorage storage;
    public static Path storagePath;
    public static ExtensionLogger logger;

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        Path optInPath = this.dataFolder().resolve("optIn");
        Path optOutPath = this.dataFolder().resolve("optOut");
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
        logger.info("Sending packs to " + event.connection().xuid());
        List<ResourcePack> connectionPacks = storage.getPacks(event.connection().xuid());
        for (ResourcePack pack : connectionPacks) {
            event.register(pack);
        }
    }

    @Subscribe
    public void CommandEvent(GeyserDefineCommandsEvent commandsEvent) {
        logger().info("Registering commands");
        commandsEvent.register(Command.builder(this)
                .name("packs")
                .aliases(List.of("rp", "resourcepack", "pack"))
                .bedrockOnly(true)
                .source(GeyserConnection.class)
                .description("Choose your own packs")
                .executableOnConsole(false)
                .suggestedOpOnly(false)
                .permission("") //blank would be ideal, but those perms are currently broken on proxy setups
                .executor((source, command, args) -> {
                    Form form = new Form();
                    form.send((GeyserConnection) source, args);
                })
                .build());
    }
}
