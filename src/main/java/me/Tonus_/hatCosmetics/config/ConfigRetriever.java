package me.Tonus_.hatCosmetics.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import java.io.File;


public class ConfigRetriever implements IConfigRetriever {
    private final Logger logger;
    private final FileConfiguration config;

    public ConfigRetriever(@NotNull Plugin plugin) {
        var configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        this.logger = plugin.getSLF4JLogger();
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    @TestOnly
    ConfigRetriever(Logger logger, FileConfiguration config) {
        this.logger = logger;
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getValue(@NotNull ConfigReference<T> configReference) {
        var value = config.get(configReference.yamlPath);

        if (value != null && !configReference.getType().isInstance(value)) {
            var simpleName = value.getClass().getSimpleName();

            logger.warn(
                    "Config value ({}) at path '{}' is not of expected type. Found {}.",
                    value,
                    configReference.yamlPath,
                    simpleName
            );

            return null;
        }

        return (T) value;
    }

    public <T> T getValue(@NotNull ConfigReference<T> configReference, T defaultValue) {
        var value = getValue(configReference);
        return value != null ? value : defaultValue;
    }
}
