package net.onebeastchris.geyser.extension.pickpack.Util;

import org.geysermc.geyser.api.pack.PackCodec;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.api.pack.ResourcePackManifest;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.onebeastchris.geyser.extension.pickpack.PickPack.logger;

public class ResourcePackLoader {
    public Map<String, ResourcePack> DEFAULT = new HashMap<>();
    public Map<String, ResourcePack> OPTIONAL = new HashMap<>();
    public Map<String, ResourcePackManifest> PACKS_INFO = new HashMap<>();

    private final Path optionalPacksPath;
    private final Path defaultPacksPath;
    public ResourcePackLoader(Path optionalPacksPath, Path defaultPacksPath) {
        this.optionalPacksPath = optionalPacksPath;
        this.defaultPacksPath = defaultPacksPath;
        loadPacks();
    }

    public void reload() {
        OPTIONAL.clear();
        DEFAULT.clear();
        PACKS_INFO.clear();
        loadPacks();
    }

    public void loadPacks() {
        try {
            OPTIONAL = loadFromFolder(optionalPacksPath);
            DEFAULT = loadFromFolder(defaultPacksPath);

            logger.info("Loaded " + DEFAULT.size() + " default packs!");
            logger.info("Loaded " + OPTIONAL.size() + " optional packs!");
        } catch (Exception e) {
            logger.error("Failed to load packs!", e);
        }
    }

    public ResourcePack getPack(String packId) {
        return DEFAULT.getOrDefault(packId, OPTIONAL.get(packId));
    }

    public HashMap<String, ResourcePack> loadFromFolder(Path path) {
        HashMap<String, ResourcePack> packs = new HashMap<>();

        for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
            try {
                ResourcePack pack = ResourcePack.create(PackCodec.path(file.toPath()));
                String uuid = pack.manifest().header().uuid().toString();
                packs.put(uuid, pack);
                PACKS_INFO.put(uuid, pack.manifest());
            } catch (Exception e) {
                logger.error("Failed to load pack " + file.getName(), e);
                e.printStackTrace();
            }
        }
        return packs;
    }
}
