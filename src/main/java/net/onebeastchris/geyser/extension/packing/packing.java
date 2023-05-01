package net.onebeastchris.geyser.extension.packing;

import net.onebeastchris.geyser.extension.packing.Util.Packs;
import net.onebeastchris.geyser.extension.packing.Util.PlayerStorage;
import net.onebeastchris.geyser.extension.packing.Util.choosePacksForm;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.PlayerResourcePackLoadEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;

import java.nio.file.Path;
import java.util.List;

public class packing implements Extension {
    public static Packs packs;
    public static int port = 19132; //TODO: config? or autograb?
    public static String address = "127.0.0.1"; //TODO: config? or autograb?
    public static PlayerStorage storage;
    public static Path storagePath;

    public ExtensionLogger logger;

    @SuppressWarnings("unchecked")
    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        //this.saveDefaultConfig(configPath);
        //var config = ConfigLoader.load(this, packing.class, packking.class);
        Path optInPath = this.dataFolder().resolve("optIn");
        Path optOutPath = this.dataFolder().resolve("optOut");
        storagePath = this.dataFolder().resolve("cache");

        logger = this.logger();

        makeDir(optInPath, "opt-in-packs");
        makeDir(optOutPath, "opt-out-packs");
        makeDir(storagePath, "storage");

        packs = new Packs(this.logger());
        packs.loadPacks(optInPath, false);
        packs.loadPacks(optOutPath, true);

        storage = new PlayerStorage(this.logger());
    }

    //on player join: send packs if we have any for them
    @Subscribe
    public void onPlayerResourcePackLoadEvent(PlayerResourcePackLoadEvent event) {
        this.logger.info("Player joined!");
        event.setPacks(storage.getPacks(event.connection().xuid()));
        logger().info("Sent packs to player!");
    }

    @Subscribe
    public void CommandEvent(GeyserDefineCommandsEvent commandsEvent) {
        logger().info("Registering commands");
        commandsEvent.register(getCommand());
    }
    private Command getCommand() {
        return Command.builder(this)
                .name("packs")
                .aliases(List.of("rp", "resourcepack", "pack"))
                .bedrockOnly(true)
                .source(GeyserConnection.class)
                .description("Choose your own packs")
                .executableOnConsole(false)
                .suggestedOpOnly(false)
                .permission("packking.command.packs")
                .executor((source, command, args) -> {
                    choosePacksForm form = new choosePacksForm(this.logger());
                    form.sendForm((GeyserConnection) source);
                })
                .build();
    }


    private void makeDir(Path path, String name) {
        if (!path.toFile().exists()) {
            this.logger().info(name + " folder does not exist, creating...");
            if (!path.toFile().mkdirs()) {
                this.logger().error("Failed to create " + name + " folder");
            }
        }
    }
}
