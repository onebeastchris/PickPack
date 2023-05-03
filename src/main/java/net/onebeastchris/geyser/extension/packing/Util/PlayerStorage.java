package net.onebeastchris.geyser.extension.packing.Util;

import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

import static net.onebeastchris.geyser.extension.packing.packing.packs;

public class PlayerStorage {
    public Map<String, Map<String, ResourcePack>> cache;
    ExtensionLogger logger;
    public PlayerStorage(ExtensionLogger logger) {
        this.logger = logger;
        cache = new HashMap<>();

        for (File file : Objects.requireNonNull(packing.storagePath.toFile().listFiles())) {
            cache.put(file.getName().replace(".txt", ""), FileSaveUtil.load(file.toPath()));
            logger.info("Loading " + file.getName());
        }
    }

    public void setPacks(String xuid, Map<String, ResourcePack> packs) {
        logger.info("Setting packs for " + xuid);
        cache.put(xuid, packs);

        for (ResourcePack pack : packs.values()) {
            logger.info("Pack added: " + pack.getManifest().getHeader().getName());
        }

        Executors.newSingleThreadExecutor().execute(() ->
                FileSaveUtil.save(packs, xuid)
        );
    }

    public Map<String, ResourcePack> getPacks(String xuid) {
        if (cache.containsKey(xuid)) {
            return cache.get(xuid);
        } else {
            logger.info("No packs found for " + xuid);
            return packs.OPT_OUT;
        }
    }

    public boolean hasSpecificPack(String xuid, String uuid) {
        if (cache.containsKey(xuid)) {
            return cache.get(xuid).containsKey(uuid);
        } else {
            logger.debug("No packs found for " + xuid);
            return false;
        }
    }
}
