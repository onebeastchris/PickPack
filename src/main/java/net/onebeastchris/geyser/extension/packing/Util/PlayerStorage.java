package net.onebeastchris.geyser.extension.packing.Util;

import org.geysermc.geyser.api.packs.ResourcePack;

import java.util.HashMap;
import java.util.Map;

public class PlayerStorage {
    public Map<String, Map<String, ResourcePack>> cache;

    public PlayerStorage() {
        cache = new HashMap<>();
        //TODO: load from file
    }
}
