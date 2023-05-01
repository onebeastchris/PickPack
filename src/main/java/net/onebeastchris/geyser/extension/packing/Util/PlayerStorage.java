package net.onebeastchris.geyser.extension.packing.Util;

import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.onebeastchris.geyser.extension.packing.packing.packs;

public class PlayerStorage {
    public Map<String, Map<String, ResourcePack>> cache;
    ExtensionLogger logger;
    public PlayerStorage(ExtensionLogger logger) {
        this.logger = logger;
        cache = new HashMap<>();

        for (File file : Objects.requireNonNull(packing.storagePath.toFile().listFiles())) {
            logger.info("Loading " + file.getName());
            cache.put(file.getName().replace(".txt", ""), FileSaveUtil.load(file.toPath()));
        }
    }

    public void setPacks(String xuid, Map<String, ResourcePack> packs) {
        cache.remove(xuid);
        cache.put(xuid, packs);
        FileSaveUtil.save(packs, xuid);
    }

    public Map<String, ResourcePack> getPacks(String xuid) {
        logger.info("Getting packs for " + xuid);
        if (cache.containsKey(xuid)) {
            return cache.get(xuid);
        } else {
            return packs.OPT_OUT;
        }
    }
}
