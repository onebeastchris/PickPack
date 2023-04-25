package net.onebeastchris.geyser.extension.packing;

import net.onebeastchris.geyser.extension.packing.Util.Packs;
import net.onebeastchris.geyser.extension.packing.Util.PlayerStorage;
import net.onebeastchris.geyser.extension.packing.Util.choosePacksForm;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandExecutor;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.bedrock.SessionInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.command.GeyserCommandSource;
import org.geysermc.geyser.session.GeyserSession;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class packing implements Extension {
    public static Packs packs;

    public static int port = 19132; //TODO: config? or autograb?

    public static String address = "127.0.0.1"; //TODO: config? or autograb?

    public static PlayerStorage storage;

    @SuppressWarnings("unchecked")
    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        //this.saveDefaultConfig(configPath);
        //var config = ConfigLoader.load(this, packing.class, packking.class);
        Path packsPath = this.dataFolder().resolve("packs");

        if (!this.dataFolder().toFile().exists()) {
            this.logger().info("Data folder does not exist, creating...");
            if (!this.dataFolder().toFile().mkdirs()) {
                this.logger().error("Failed to create data folder");
            }
        }

        if (!this.dataFolder().resolve("packsToChoose").toFile().exists()) {
            this.logger().info("Packs folder does not exist, creating...");
            if (!this.dataFolder().resolve("packs").toFile().mkdirs()) {
                this.logger().error("Failed to create packs folder");
            }
        }

        packs = new Packs(this.logger());
        packs.loadPacks(packsPath);

        storage = new PlayerStorage();

        //this.commands = new HashSet<>(((ArrayList<String>) new Yaml().loadAs(Files.newBufferedReader(commandsPath), LinkedHashMap.class).get("commands")));
    }

    //on player join: send packs if we have any for them
    @Subscribe
    public void onSessionInitialize(SessionInitializeEvent event) {
        GeyserSession session = (GeyserSession) event.connection();
        event.addPacks(packs.packsmap);
        logger().info("Sent packs to player!");
        logger().info(session.getPreferencesCache().PACKS.toString());
    }


    GeyserCommandSource
    @Subscribe
    public void CommandEvent(GeyserDefineCommandsEvent commandsEvent) {
        commandsEvent.register(
                Command.builder(this)
                        .name("packs")
                        .aliases(List.of("rp"))
                        .bedrockOnly(true)
                        .description("Set your own packs")
                        .executableOnConsole(false)
                        .suggestedOpOnly(false)
                        .executor((source, command, args) -> {
                            choosePacksForm form = new choosePacksForm();
                            form.sendForm((GeyserConnection) source, ())
                        })
                        .build()
        );
    }
}