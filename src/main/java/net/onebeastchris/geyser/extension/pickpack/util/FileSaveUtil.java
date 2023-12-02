package net.onebeastchris.geyser.extension.pickpack.util;

import net.onebeastchris.geyser.extension.pickpack.PickPack;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.onebeastchris.geyser.extension.pickpack.PickPack.loader;
import static net.onebeastchris.geyser.extension.pickpack.PickPack.logger;

public class FileSaveUtil {

    public static void save(List<String> list, String xuid) {
        Path filepath = PickPack.storagePath.resolve(xuid + ".txt");
        saveToFile(list, filepath);
    }

    public static List<String> load(Path filepath) {
        List<String> packs = readFromFile(filepath);

        // Remove packs that no longer exist
        if (packs.removeIf(packId -> loader.getPack(packId) == null)) {
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

