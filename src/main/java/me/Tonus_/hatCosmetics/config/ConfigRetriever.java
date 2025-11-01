package me.Tonus_.hatCosmetics.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;


public class ConfigRetriever implements IConfigRetriever {
    private final Plugin plugin;
    private final FileConfiguration config;

    public ConfigRetriever(@NotNull Plugin plugin) {
        var configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        this.plugin = plugin;
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    ConfigRetriever(@NotNull Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getValue(@NotNull ConfigReference configReference) {
        Object value = config.get(configReference.yamlPath);

        if (value == null) {
            return null;
        }

        if (!configReference.type.isInstance(value)) {
            logConfigMismatchWarning(configReference, value);
            return null;
        }

        return (T) configReference.type.cast(value);
    }

    public <T> @Nullable T getValue(@NotNull ConfigReference configReference, T defaultValue) {
        var isSameType = configReference.type.isInstance(defaultValue);

        if (isSameType) {
            T value = getValue(configReference);
            return value != null ? value : defaultValue;
        } else {
            logConfigMismatchWarning(configReference, null);
            return null;
        }
    }

    private void logConfigMismatchWarning(@NotNull ConfigReference configReference, @Nullable Object value) {
        var simpleName = value == null ? null : value.getClass().getSimpleName();

        plugin.getSLF4JLogger().warn(
                "Config value ({}) at path '{}' is not of expected type {}. Found {}.",
                value,
                configReference.yamlPath,
                configReference.type.getSimpleName(),
                simpleName
        );
    }
}
