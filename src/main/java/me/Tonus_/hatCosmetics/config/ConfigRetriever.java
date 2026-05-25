package me.Tonus_.hatCosmetics.config;

import me.Tonus_.hatCosmetics.config.mapper.ITypeMapperRegistry;
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
    private final Plugin plugin;
    private final ITypeMapperRegistry typeMapperRegistry;
    private FileConfiguration config;

    public ConfigRetriever(@NotNull Plugin plugin, ITypeMapperRegistry typeMapperRegistry) {
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        this.config = loadConfigFile();
        this.typeMapperRegistry = typeMapperRegistry;
    }

    @TestOnly
    ConfigRetriever(Plugin plugin, Logger logger, FileConfiguration config, ITypeMapperRegistry typeMapperRegistry) {
        this.plugin = plugin;
        this.logger = logger;
        this.config = config;
        this.typeMapperRegistry = typeMapperRegistry;
    }

    @Override
    public void reload() {
        this.config = loadConfigFile();
        logger.info("Config reloaded.");
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getValue(@NotNull ConfigReference<T> configReference) {
        var value = config.get(configReference.getYamlPath());

        if (value != null && !configReference.getType().isInstance(value)) {
            if (value instanceof String strValue) {
                var mappedValue = mapValue(strValue, configReference.getType());
                if (mappedValue != null) return mappedValue;
            }

            var simpleName = value.getClass().getSimpleName();

            logger.warn(
                    "Config value ({}) at path '{}' is not of expected type. Found {}.",
                    value,
                    configReference.getYamlPath(),
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

    private <T> @Nullable T mapValue(@NotNull String value, @NotNull Class<T> desiredType) {
        var mapper = typeMapperRegistry.getMapper(desiredType);
        return mapper != null ? mapper.map(value) : null;
    }

    private FileConfiguration loadConfigFile() {
        var configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }
}
