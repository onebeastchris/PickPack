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
    /**
     * This is a map of XUIDs to a list of resource packs.
     */
    public Map<String, List<String>> cache;
    public PlayerStorage() {
        cache = new HashMap<>();

        for (File file : Objects.requireNonNull(PickPack.storagePath.toFile().listFiles())) {
            cache.put(file.getName().replace(".txt", ""), FileSaveUtil.load(file.toPath()));
            logger.debug("Loading " + file.getName());
        }
    }

    public CompletableFuture<Void> setPacks(String xuid, List<String> packs) {
        cache.put(xuid, packs);
        Executors.newSingleThreadExecutor().execute(() ->
                FileSaveUtil.save(packs, xuid)
        );
        return CompletableFuture.completedFuture(null);
    }

    public @NonNull List<ResourcePack> getPacks(String xuid) {
        if (cache.containsKey(xuid)) {
            List<ResourcePack> packs = new ArrayList<>();
            cache.get(xuid).forEach(pack -> {
                ResourcePack resourcePack = loader.getPack(pack);
                if (resourcePack != null) {
                    packs.add(resourcePack);
                }
            });
            return packs;
        } else {
            return new ArrayList<>(loader.DEFAULT.values());
        }
    }

    public @NonNull List<String> getPackIds(String xuid) {
        if (cache.containsKey(xuid)) {
            return cache.get(xuid);
        } else {
            return new ArrayList<>(loader.DEFAULT.keySet());
        }
    }

    public boolean hasSpecificPack(String xuid, String uuid) {
        if (cache.containsKey(xuid)) {
            for (String pack : cache.get(xuid)) {
                if (pack.equals(uuid)) {
                    return true;
                }
            }
            return false;
        } else {
            return loader.DEFAULT.containsKey(uuid);
        }
    }
}
