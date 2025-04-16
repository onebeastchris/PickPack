package net.onebeastchris.geyser.extension.pickpack.util;

import org.checkerframework.checker.nullness.qual.Nullable;
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
    public final Map<String, ResourcePackManifest> PACKS_INFO = new HashMap<>();

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

    private List<String> cachedPackNames = new ArrayList<>();
    
    public void loadPacks() {
        try {
            // Clear existing data
            OPTIONAL.clear();
            DEFAULT.clear();
            PACKS_INFO.clear();
            cachedPackNames.clear();  // Reset cache
            
            // Load packs
            OPTIONAL = loadFromFolder(optionalPacksPath);
            DEFAULT = loadFromFolder(defaultPacksPath);
    
            // Build cache
            cachedPackNames = PACKS_INFO.values().stream()
                .map(manifest -> manifest.header().name())
                .collect(Collectors.toList());
    
            logger.info("Loaded " + DEFAULT.size() + " default packs!");
            logger.info("Loaded " + OPTIONAL.size() + " optional packs!");
            logger.debug("Cached pack names: " + String.join(", ", cachedPackNames)); // Debug log
    
        } catch (Exception e) {
            logger.error("Failed to load packs due to: " + (e.getMessage() != null ? e.getMessage() : e));
            if (logger.isDebug()) {
                e.printStackTrace();  // Stack trace cuma di mode debug
            }
            
            // Fallback: Minimal cache kosong kalo error rekk
            cachedPackNames.clear();
        }
    }

    public @Nullable ResourcePack getPack(String packId) {
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
                logger.error("Failed to load pack at " + file.getName() + " due to: " + (e.getMessage() != null ? e.getMessage() : e));
                if (logger.isDebug()) {
                    e.printStackTrace();
                }
            }
        }
        return packs;
    }
}
