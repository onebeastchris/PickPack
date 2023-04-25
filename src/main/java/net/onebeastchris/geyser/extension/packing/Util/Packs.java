package net.onebeastchris.geyser.extension.packing.Util;

import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;
import org.geysermc.geyser.pack.ResourcePackUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Packs {

    ExtensionLogger logger;
    public Packs(ExtensionLogger logger) {
        this.logger = logger;
    }

    public Map<String, ResourcePack> packsmap = new HashMap<>();

    public Map<String, String> packNamesMap = new HashMap<>();
    public void loadPacks(Path packsPath) {
        try {
            packsmap = ResourcePackUtil.loadPacksToMap(packsPath);
            logger.info("Loaded " + packsmap.size() + " packs!");
            logger.info("Packs: " + packsmap.toString());

            for (Map.Entry<String, ResourcePack> entry : packsmap.entrySet()) {
                packNamesMap.put(entry.getValue().getManifest().getHeader().getName(), entry.getKey());
            }
        } catch (Exception e) {
            logger.error("Failed to load packs!", e);
        }
    }
}
