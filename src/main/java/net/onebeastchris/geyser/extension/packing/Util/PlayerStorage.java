package net.onebeastchris.geyser.extension.packing.Util;

import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.util.HashMap;
import java.util.Map;

public class PlayerStorage {
    public Map<String, Map<String, ResourcePack>> cache;

    ExtensionLogger logger;
    public PlayerStorage(ExtensionLogger logger) {
        this.logger = logger;
        cache = new HashMap<>();
        //TODO: load from file
    }

    public void setPacks(String xuid, Map<String, ResourcePack> packs) {
        logger.info("Setting packs for " + xuid + " to " + packs.keySet().toString());
        cache.put(xuid, packs);
    }

    public Map<String, ResourcePack> getPacks(String xuid) {
        logger.info("Getting packs for " + xuid);
        return cache.get(xuid);
    }
}
