package net.onebeastchris.geyser.extension.packing.Util;

import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;
import org.geysermc.geyser.pack.ResourcePackUtil;

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

    public void loadPacks(Path optout, Path optin) {
        Map<String, ResourcePack> packs;
        try {
            OPT_OUT = ResourcePackUtil.loadPacksToMap(optout);
            OPT_IN = ResourcePackUtil.loadPacksToMap(optin);

            loadInfo(OPT_OUT);
            loadInfo(OPT_IN);
        } catch (Exception e) {
            logger.error("Failed to load packs!", e);
        }
    }

    public ResourcePack getPack(String packUUID) {
        if (OPT_IN.containsKey(packUUID)) {
            return OPT_IN.get(packUUID);
        } else return OPT_OUT.get(packUUID);
    }

    public String UTF8Loader(String weirdString) {
        byte[] utf8Bytes = weirdString.getBytes(StandardCharsets.UTF_8); // convert to UTF-8 byte array
        return new String(utf8Bytes, StandardCharsets.UTF_8); // parse the byte array as a UTF-8 encoded string
    }

    public void loadInfo(Map<String, ResourcePack> map) {
        for (Map.Entry<String, ResourcePack> entry : map.entrySet()) {
            String uuid = entry.getKey();
            PACKS_INFO.put(
                    uuid,
                    new String[] {
                            UTF8Loader(entry.getValue().getManifest().getHeader().getName()),
                            UTF8Loader(entry.getValue().getManifest().getHeader().getDescription()),
                            UTF8Loader(entry.getValue().getManifest().getHeader().getVersionString())
                    }
            );
        }
    }
}
