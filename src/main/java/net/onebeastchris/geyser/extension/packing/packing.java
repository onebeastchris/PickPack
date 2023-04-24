package net.onebeastchris.geyser.extension.packing;

import net.onebeastchris.geyser.extension.packing.Util.Packs;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.bedrock.SessionInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.session.GeyserSession;

import java.nio.file.Path;

public class packing implements Extension {
    public Packs packs;

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

        if (!this.dataFolder().resolve("packs").toFile().exists()) {
            this.logger().info("Packs folder does not exist, creating...");
            if (!this.dataFolder().resolve("packs").toFile().mkdirs()) {
                this.logger().error("Failed to create packs folder");
            }
        }

        packs = new Packs(this.logger());
        packs.loadPacks(packsPath);

        //this.commands = new HashSet<>(((ArrayList<String>) new Yaml().loadAs(Files.newBufferedReader(commandsPath), LinkedHashMap.class).get("commands")));
    }

    //on player join: send packs if we have any for them
    @Subscribe
    public void onSessionInitialize(SessionInitializeEvent event) {
        GeyserSession session = (GeyserSession) event.connection();
        logger().info("Session initialized for player!");
        //send packs
        session.setPacks(this.packs.packsmap);
        logger().info("Sent packs to player!");
    }
}