package net.onebeastchris.geyser.extension.pickpack.Util;

import net.onebeastchris.geyser.extension.pickpack.PickPack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.api.pack.ResourcePack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static net.onebeastchris.geyser.extension.pickpack.PickPack.loader;
import static net.onebeastchris.geyser.extension.pickpack.PickPack.logger;

public class PlayerStorage {
    public Map<String, List<ResourcePack>> cache;
    public PlayerStorage() {
        cache = new HashMap<>();

        for (File file : Objects.requireNonNull(PickPack.storagePath.toFile().listFiles())) {
            cache.put(file.getName().replace(".txt", ""), FileSaveUtil.load(file.toPath()));
            logger.debug("Loading " + file.getName());
        }
    }

    public CompletableFuture<Void> setPacks(String xuid, List<ResourcePack> packs) {
        cache.put(xuid, packs);
        StringBuilder packsString = new StringBuilder();
        for (ResourcePack pack : packs) {
            packsString.append(pack.manifest().header().name()).append(" ");
        }
        logger.debug("Saving packs for " + xuid + ": " + packsString);
        Executors.newSingleThreadExecutor().execute(() ->
                FileSaveUtil.save(packs, xuid)
        );
        return CompletableFuture.completedFuture(null);
    }

    public @NonNull List<ResourcePack> getPacks(String xuid) {
        if (cache.containsKey(xuid)) {
            return cache.get(xuid);
        } else {
            return new ArrayList<>(loader.OPT_OUT.values());
        }
    }

    public boolean hasSpecificPack(String xuid, String uuid) {
        if (cache.containsKey(xuid)) {
            boolean hasPack = false;
            for (ResourcePack pack : cache.get(xuid)) {
                if (pack.manifest().header().uuid().toString().equals(uuid)) {
                    hasPack = true;
                    break;
                }
            }
            return hasPack;
        } else {
            return loader.OPT_OUT.containsKey(uuid);
        }
    }
}
