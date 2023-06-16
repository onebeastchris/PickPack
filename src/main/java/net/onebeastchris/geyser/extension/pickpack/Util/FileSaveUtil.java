package net.onebeastchris.geyser.extension.pickpack.Util;

import net.onebeastchris.geyser.extension.pickpack.PickPack;
import org.geysermc.geyser.api.pack.ResourcePack;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.onebeastchris.geyser.extension.pickpack.PickPack.loader;
import static net.onebeastchris.geyser.extension.pickpack.PickPack.logger;

public class FileSaveUtil {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void save(List<ResourcePack> list, String xuid) {
        Path filepath = PickPack.storagePath.resolve(xuid + ".txt");
        if (filepath.toFile().exists()) {
            filepath.toFile().delete();
        }
        List<String> packUUIDs = new ArrayList<>();
        for (ResourcePack pack : list) {
            packUUIDs.add(pack.manifest().header().uuid().toString());
        }
        saveToFile(packUUIDs, filepath);
    }

    public static List<ResourcePack> load(Path filepath) {
        List<String> packUUIDs = readFromFile(filepath);
        List<ResourcePack> list = new ArrayList<>();
        for (String pack : packUUIDs) {
            ResourcePack resourcePack = loader.getPack(pack);
                if (resourcePack != null) {
                    list.add(resourcePack);
                } else {
                    logger.debug("Could not find pack with UUID " + pack + " in either folder! We are not loading it.");
                }
            }
        return list;
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

