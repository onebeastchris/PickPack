package net.onebeastchris.geyser.extension.pickpack.Util;

import net.onebeastchris.geyser.extension.pickpack.PickPack;
import org.geysermc.geyser.api.pack.ResourcePack;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.onebeastchris.geyser.extension.pickpack.PickPack.loader;
import static net.onebeastchris.geyser.extension.pickpack.PickPack.logger;

public class FileSaveUtil {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void save(Map<String, ResourcePack> map, String xuid) {
        Path filepath = PickPack.storagePath.resolve(xuid + ".txt");
        if (filepath.toFile().exists()) {
            filepath.toFile().delete();
        }
        List<String> packUUIDs = new ArrayList<>();
        for (Map.Entry<String, ResourcePack> entry : map.entrySet()) {
            packUUIDs.add(entry.getKey());
        }
        saveToFile(packUUIDs, filepath);
    }

    public static Map<String, ResourcePack> load(Path filepath) {
        List<String> packUUIDs = readFromFile(filepath);
        Map<String, ResourcePack> map = new HashMap<>();
        for (String pack : packUUIDs) {
            ResourcePack resourcePack = loader.getPack(pack);
                if (resourcePack != null) {
                    map.put(pack, resourcePack);
                } else {
                    logger.debug("Could not find pack with UUID " + pack + " in either map! We are not loading it.");
                }
            }
        return map;
    }

    public static void saveToFile(List<String> packs, Path filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath.toFile()))) {
            for (String pack : packs) {
                writer.write(pack);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readFromFile(Path filepath) {
        List<String> packs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                packs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packs;
    }

    public static void makeDir(Path path, String name) {
        if (!path.toFile().exists()) {
            logger.info(name + " folder does not exist, creating...");
            if (!path.toFile().mkdirs()) {
                logger.error("Failed to create " + name + " folder");
            }
        }
    }
}

