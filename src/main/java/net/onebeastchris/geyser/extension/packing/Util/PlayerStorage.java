package net.onebeastchris.geyser.extension.packing.Util;

import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static net.onebeastchris.geyser.extension.packing.packing.loader;

public class PlayerStorage {
    public Map<String, Map<String, ResourcePack>> cache;
    ExtensionLogger logger;
    public PlayerStorage(ExtensionLogger logger) {
        this.logger = logger;
        cache = new HashMap<>();

        for (File file : Objects.requireNonNull(packing.storagePath.toFile().listFiles())) {
            cache.put(file.getName().replace(".txt", ""), FileSaveUtil.load(file.toPath()));
            logger.debug("Loading " + file.getName());
        }
    }

    public CompletableFuture<Void> setPacks(String xuid, Map<String, ResourcePack> packs) {
        cache.put(xuid, packs);
        for (ResourcePack pack : packs.values()) {
            logger.info("Packs added: " + pack.getManifest().getHeader().getName());
        }
        Executors.newSingleThreadExecutor().execute(() ->
                FileSaveUtil.save(packs, xuid)
        );
        return CompletableFuture.completedFuture(null);
    }

    public Map<String, ResourcePack> getPacks(String xuid) {
        if (cache.containsKey(xuid)) {
            return cache.get(xuid);
        } else {
            logger.info("No packs found for " + xuid);
            return loader.OPT_OUT;
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
