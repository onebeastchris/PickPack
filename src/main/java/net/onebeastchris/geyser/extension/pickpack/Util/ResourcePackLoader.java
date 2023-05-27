package net.onebeastchris.geyser.extension.pickpack.Util;

import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResourcePackLoader {

    ExtensionLogger logger;
    public ResourcePackLoader(ExtensionLogger logger) {
        this.logger = logger;
    }

    public Map<String, ResourcePack> OPT_IN = new HashMap<>();
    public Map<String, ResourcePack> OPT_OUT = new HashMap<>();
    public Map<String, String[]> PACKS_INFO = new HashMap<>();

    public void loadPacks(Path optout, Path optin) {
        try {
            OPT_OUT = loadFromFolder(optout);
            OPT_IN = loadFromFolder(optin);

            logger.info("Loaded " + OPT_OUT.size() + " opt-out packs!");
            logger.info("Loaded " + OPT_IN.size() + " opt-in packs!");
        } catch (Exception e) {
            logger.error("Failed to load packs!", e);
        }
    }

    public ResourcePack getPack(String packUUID) {
        if (OPT_IN.containsKey(packUUID)) {
            return OPT_IN.get(packUUID);
        } else return OPT_OUT.get(packUUID);
    }

    public HashMap<String, ResourcePack> loadFromFolder(Path path) {
        HashMap<String, ResourcePack> packs = new HashMap<>();
        for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
            try {
                ResourcePack pack = ResourcePack.fromPath(file.toPath());
                String uuid = pack.manifest().header().uuid().toString();
                packs.put(uuid, pack);

                PACKS_INFO.put(uuid, new String[] {
                        pack.manifest().header().name(),
                        pack.manifest().header().description(),
                        pack.manifest().header().versionString()
                });
            } catch (Exception e) {
                logger.error("Failed to load pack " + file.getName(), e);
                e.printStackTrace();
            }
        }
        return packs;
    }
}
