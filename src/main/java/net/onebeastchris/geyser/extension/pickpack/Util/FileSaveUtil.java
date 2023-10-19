package net.onebeastchris.geyser.extension.pickpack.Util;

import net.onebeastchris.geyser.extension.pickpack.PickPack;
import org.geysermc.geyser.api.pack.ResourcePack;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.onebeastchris.geyser.extension.pickpack.PickPack.loader;
import static net.onebeastchris.geyser.extension.pickpack.PickPack.logger;

public class FileSaveUtil {

    public static void save(List<String> list, String xuid) {
        Path filepath = PickPack.storagePath.resolve(xuid + ".txt");
        saveToFile(list, filepath);
    }

    public static List<String> load(Path filepath) {
        List<String> packs = readFromFile(filepath);
        AtomicBoolean changed = new AtomicBoolean(false);
        packs.forEach(packId -> {
            ResourcePack pack = loader.getPack(packId);
            if (pack == null) {
                logger.debug("Could not find pack with UUID " + packId + " in cache, removing from file");
                packs.remove(packId);
                changed.set(true);
            }
        });
        if (changed.get()) {
            saveToFile(packs, filepath);
        }
        return packs;
    }

    public static void saveToFile(List<String> packs, Path filepath) {
        if (filepath.toFile().exists()) {
            if (!filepath.toFile().delete()) {
                logger.error("Failed to delete " + filepath.getFileName());
                return;
            }
        }
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

