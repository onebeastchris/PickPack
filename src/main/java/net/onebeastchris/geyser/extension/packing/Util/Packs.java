package net.onebeastchris.geyser.extension.packing.Util;

import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Packs {

    ExtensionLogger logger;
    public Packs(ExtensionLogger logger) {
        this.logger = logger;
    }

    public Map<String, ResourcePack> OPT_IN = new HashMap<>();
    public Map<String, ResourcePack> OPT_OUT = new HashMap<>();
    public Map<String, String[]> PACKS_INFO = new HashMap<>();

    public void loadPacks(Path path, boolean forcePacks) {
        Map<String, ResourcePack> packs;
        if (forcePacks) {
            packs = OPT_OUT;
        } else {
            packs = OPT_IN;
        }
        try {
            logger.info("Loaded " + packs.size() + " packs!");
            for (Map.Entry<String, ResourcePack> entry : packs.entrySet()) {
                PACKS_INFO.put(
                        entry.getKey(),
                        new String[] {
                                UTF8Loader(entry.getValue().getManifest().getHeader().getName()),
                                UTF8Loader(entry.getValue().getManifest().getHeader().getDescription()),
                                UTF8Loader(entry.getValue().getManifest().getHeader().getVersionString()),
                                String.valueOf(forcePacks)
                        }
                );
            }
        } catch (Exception e) {
            logger.error("Failed to load packs!", e);
        }
    }

    public String UTF8Loader(String weirdString) {
        byte[] utf8Bytes = weirdString.getBytes(StandardCharsets.UTF_8); // convert to UTF-8 byte array
        return new String(utf8Bytes, StandardCharsets.UTF_8); // parse the byte array as a UTF-8 encoded string
    }
}
