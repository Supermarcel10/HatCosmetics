package me.Tonus_.hatCosmetics.config.migration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;


public class V4ToV5Migration implements IConfigMigration {
    private static final String COSMETICS_DIR = "cosmetics";
    private static final String BACKUP_BASE = "config.yml.bak.v4";

    private static final String DEFAULT_MATERIAL = "FEATHER";
    private static final String DEFAULT_BORDER = "LIGHT_BLUE_STAINED_GLASS_PANE";
    private static final int DEFAULT_GUI_ROWS = 1;

    @Override
    public String fromVersion() {
        return "4";
    }

    @Override
    public String toVersion() {
        return "5";
    }

    @Override
    public void run(Plugin plugin, Logger logger) {
        var dataFolder = plugin.getDataFolder();
        var configFile = new File(dataFolder, "config.yml");
        var oldConfig = YamlConfiguration.loadConfiguration(configFile);

        backupConfig(configFile, logger);

        var globalItem = oldConfig.getString("item", DEFAULT_MATERIAL);
        var border = oldConfig.getString("border", DEFAULT_BORDER);
        var guiRows = oldConfig.getInt("gui_rows", DEFAULT_GUI_ROWS);
        var hideHats = oldConfig.getBoolean("hide_hats", false);

        var cosmeticsDir = new File(dataFolder, COSMETICS_DIR);
        if (!cosmeticsDir.exists()) {
            cosmeticsDir.mkdirs();
        }

        var hatsSection = oldConfig.getConfigurationSection("hats");
        if (hatsSection != null) {
            migrateCosmetics(hatsSection, cosmeticsDir, globalItem, logger);
        }

        writeNewConfig(plugin, configFile, guiRows, border, hideHats, logger);
    }

    private void migrateCosmetics(
        ConfigurationSection hatsSection,
        File cosmeticsDir,
        String globalItem,
        Logger logger
    ) {
        for (var hatId : hatsSection.getKeys(false)) {
            var hatSection = hatsSection.getConfigurationSection(hatId);
            if (hatSection == null) continue;

            var data = hatSection.get("data");
            var name = hatSection.getString("name", hatId);
            var description = hatSection.getStringList("description");
            var hatItem = hatSection.getString("item", globalItem);

            if (data == null) {
                createDisabledCosmetic(cosmeticsDir, hatId, hatItem, name, description, logger);
            } else {
                createCosmetic(cosmeticsDir, hatId, hatItem, String.valueOf(data), name, description, logger);
            }
        }
    }

    private void backupConfig(File configFile, Logger logger) {
        var backupFile = new File(configFile.getParent(), BACKUP_BASE);
        int counter = 1;

        while (backupFile.exists()) {
            backupFile = new File(configFile.getParent(), BACKUP_BASE + "_" + counter);
            counter++;
        }

        try {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            logger.info("Backed up old config to {}.", backupFile.getName());
        } catch (IOException e) {
            logger.warn("Failed to back up config: {}", e.getMessage());
        }
    }

    private void createCosmetic(
        File dir,
        String id,
        String material,
        String modelData,
        String name,
        List<String> description,
        Logger logger
    ) {
        var file = new File(dir, id + ".yml");
        if (file.exists()) {
            logger.warn("Cosmetic '{}' already exists at {}, skipping migration.", id, file.getPath());
            return;
        }

        var config = new YamlConfiguration();
        config.set("material", material);
        config.set("custom-model-data", modelData);
        config.set("permission", null);
        config.set("display.en_US.name", name);
        if (!description.isEmpty()) {
            config.set("display.en_US.description", description);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            logger.warn("Failed to save cosmetic '{}': {}", id, e.getMessage());
        }
    }

    private void createDisabledCosmetic(
        File dir,
        String id,
        String material,
        String name,
        List<String> description,
        Logger logger
    ) {
        var file = new File(dir, id + ".yml.disabled");
        if (file.exists()) {
            logger.warn("Disabled cosmetic '{}' already exists, skipping.", id);
            return;
        }

        var comment = """
        # This cosmetic was disabled during migration from v4 to v5 because
        # no custom model data ('data' field) was defined in the old config.
        # Please set a value for custom-model-data below, then rename this file
        # to remove the '.disabled' extension.
        """;

        var config = new YamlConfiguration();
        config.set("material", material);
        config.set("custom-model-data", "0");
        config.set("permission", null);
        config.set("display.en_US.name", name);
        if (!description.isEmpty()) {
            config.set("display.en_US.description", description);
        }

        try {
            Files.writeString(file.toPath(), comment + config.saveToString());
            logger.warn("Disabled cosmetic '{}' saved as {} - no custom model data was defined.", id, file.getName());
        } catch (IOException e) {
            logger.warn("Failed to save disabled cosmetic '{}': {}", id, e.getMessage());
        }
    }

    private void writeNewConfig(
        Plugin plugin,
        File configFile,
        int guiRows,
        String border,
        boolean hideHats,
        Logger logger
    ) {
        var templateStream = plugin.getResource("config.yml");
        if (templateStream == null) {
            logger.error("Could not load v5 config template from plugin JAR. Config migration aborted.");
            return;
        }

        YamlConfiguration newConfig;
        try (var reader = new InputStreamReader(templateStream)) {
            newConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            logger.error("Failed to read v5 config template: {}", e.getMessage());
            return;
        }

        newConfig.set("version", 5);
        newConfig.set("gui.rows", guiRows);
        newConfig.set("gui.items.border", border);
        newConfig.set("gui.hide-hats", hideHats);

        try {
            newConfig.save(configFile);
        } catch (IOException e) {
            logger.error("Failed to save migrated config: {}", e.getMessage());
        }
    }
}
