package me.Tonus_.hatCosmetics.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;


@RequiredArgsConstructor
public class YmlCosmeticStorage implements ICosmeticStorage {
    private static final String COSMETICS_DIR = "cosmetics";

    private final Plugin plugin;
    private final Logger logger;

    private List<Cosmetic> cached = null;

    @Override
    public List<Cosmetic> loadAll() {
        if (cached == null) {
            cached = loadFromDisk();
        }

        return cached;
    }

    @Override
    public void reload() {
        cached = null;
    }

    private List<Cosmetic> loadFromDisk() {
        var dir = new File(plugin.getDataFolder(), COSMETICS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            saveExampleFiles(dir);
        }

        var files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            logger.info("No cosmetics found in cosmetics/ directory.");
            return List.of();
        }

        var cosmetics = new ArrayList<Cosmetic>();
        for (var file : files) {
            var cosmetic = parseCosmetic(file);
            if (cosmetic != null) {
                cosmetics.add(cosmetic);
            }
        }

        logger.info(
            "Loaded {} cosmetics from cosmetics/ directory.",
            cosmetics.size()
        );

        return cosmetics;
    }

    private Cosmetic parseCosmetic(File file) {
        var config = YamlConfiguration.loadConfiguration(file);
        var name = file.getName().replace(".yml", "");

        var material = parseMaterial(config.getString("material"), name);
        if (material.isEmpty()) {
            return null;
        }

        var modelData = config.getString("custom-model-data");
        var permission = config.getString("permission");

        var displaySection = config.getConfigurationSection("display");
        var rawDisplay = parseDisplayables(displaySection, name);

        return new Cosmetic(
            name,
            material.get(),
            modelData,
            permission,
            rawDisplay
        );
    }

    private Optional<Material> parseMaterial(String materialStr, String cosmeticName) {
        if (materialStr == null) {
            logger.warn("Cosmetic '{}' has no material defined, skipping.", cosmeticName);
            return Optional.empty();
        }

        var material = Registry.MATERIAL.get(NamespacedKey.minecraft(materialStr.toLowerCase()));
        if (material == null) {
            logger.warn("Cosmetic '{}' has invalid material '{}', skipping.", cosmeticName, materialStr);
            return Optional.empty();
        }

        return Optional.of(material);
    }

    private Map<String, Cosmetic.DisplayData> parseDisplayables(
        ConfigurationSection displaySection,
        String defaultName
    ) {
        if (displaySection == null) {
            return Map.of();
        }

        var localeKeys = displaySection.getKeys(false);
        if (localeKeys.isEmpty()) {
            return Map.of();
        }

        var displays = new HashMap<String, Cosmetic.DisplayData>();
        for (var localeKey : localeKeys) {
            var localeElement = displaySection.getConfigurationSection(localeKey);
            if (localeElement != null) {
                var localeName = localeElement.getString("name", defaultName);
                var localeDesc = localeElement.getStringList("description");

                displays.put(
                    localeKey,
                    new Cosmetic.DisplayData(localeName, localeDesc)
                );
            }
        }

        return displays;
    }

    private void saveExampleFiles(File dir) {
        for (var example : discoverExampleResources()) {
            plugin.saveResource(example, false);
        }
    }

    private List<String> discoverExampleResources() {
        var prefix = COSMETICS_DIR + "/";

        var resources = new ArrayList<String>();
        var codeSource = plugin.getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return resources;
        }

        try (var jar = new JarFile(codeSource.getLocation().getPath())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var name = entries.nextElement().getName();
                if (name.startsWith(prefix) && name.endsWith(".yml") && !name.equals(prefix)) {
                    resources.add(name);
                }
            }
        } catch (IOException e) {
            logger.warn("Could not scan plugin JAR for example cosmetics.", e);
        }

        return resources;
    }
}
