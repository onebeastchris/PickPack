package net.onebeastchris.geyser.extension.packing.Util;

import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.onebeastchris.geyser.extension.packing.packing.packs;

public class FileSaveUtil {

    public static void save(Map<String, ResourcePack> map, String xuid) {
        Path filepath = packing.storagePath.resolve(xuid + ".txt");
        if (filepath.toFile().exists()) {
            filepath.toFile().delete();
        }
        List<StringBoolPair> stringBoolPairs = new ArrayList<>();
        for (Map.Entry<String, ResourcePack> entry : map.entrySet()) {
            String uuid = entry.getKey();
            stringBoolPairs.add(new StringBoolPair(uuid, packs.PACKS_INFO.get(uuid)[3].equals("true")));
        }
        saveStringBoolPairsToFile(stringBoolPairs, filepath);
    }

    public static Map<String, ResourcePack> load(Path filepath) {
        List<StringBoolPair> loadedStringBoolPairs = loadStringBoolPairsFromFile(filepath);
        Map<String, ResourcePack> map = new HashMap<>();
        for (StringBoolPair pair : loadedStringBoolPairs) {
            String uuid = pair.getString();
            boolean bool = pair.getBool();
            ResourcePack pack = bool ? packs.OPT_OUT.get(uuid) : packs.OPT_IN.get(uuid);
            if (pack != null) {
                map.put(uuid, pack);
            } else {
                ResourcePack pack2 = !bool ? packs.OPT_OUT.get(uuid) : packs.OPT_IN.get(uuid);
                if (pack2 != null) {
                    map.put(uuid, pack2);
                    packing.storage.logger.debug("Found pack with UUID " + uuid + " in the wrong map! We are still loading it.");
                } else {
                    packing.storage.logger.debug("Could not find pack with UUID " + uuid + " in either map! We are not loading it.");
                }
            }
        }
        return map;
    }

    public static void saveStringBoolPairsToFile(List<StringBoolPair> pairs, Path filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath.toFile()))) {
            for (StringBoolPair pair : pairs) {
                writer.write(pair.getString());
                writer.write(",");
                writer.write(Boolean.toString(pair.getBool()));
                writer.newLine(); // add a newline between pairs
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<StringBoolPair> loadStringBoolPairsFromFile(Path filepath) {
        List<StringBoolPair> pairs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String str = parts[0];
                boolean bool = Boolean.parseBoolean(parts[1]);
                pairs.add(new StringBoolPair(str, bool));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pairs;
    }
}

class StringBoolPair {
    private final String str;
    private final boolean bool;

    public StringBoolPair(String str, boolean bool) {
        this.str = str;
        this.bool = bool;
    }

    public String getString() {
        return str;
    }

    public boolean getBool() {
        return bool;
    }
}
