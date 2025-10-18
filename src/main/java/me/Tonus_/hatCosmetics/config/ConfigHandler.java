package me.Tonus_.hatCosmetics.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;


public class ConfigHandler {
	private final Plugin plugin;
	private static FileConfiguration config;
	private static File configFile;

	public ConfigHandler(Plugin plugin) {
		this.plugin = plugin;

		init();
	}

	private void init() {
		if (configFile == null) {
			configFile = new File(plugin.getDataFolder(), "config.yml");
		}

		if (!configFile.exists()) {
			plugin.saveResource("config.yml", false);
		}

		config = YamlConfiguration.loadConfiguration(configFile);
	}

	public void reload() {
		init();
	}

	public Material getMaterial(String path) {
		return Material.getMaterial(Objects.requireNonNull(getStringOrThrow(path).toUpperCase()));
	}

	public @NotNull String getStringOrDefault(String path, String defaultValue) {
		try {
			return Objects.requireNonNull(config.getString(path));
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Using default value for \"{}\"", path);
			return defaultValue;
		}
	}

	public @NotNull String getStringOrThrow(String path) {
		try {
			return Objects.requireNonNull(config.getString(path));
		} catch (Exception e) {
			plugin.getSLF4JLogger().error("Failed to get string from config: {}", path);
			throw e;
		}
	}

	public @Nullable String getString(String path) {
		return config.getString(path);
	}

	public int getInt(String path) {
		return config.getInt(path);
	}

	public boolean getBoolean(String path) {
		return config.getBoolean(path);
	}

	public @NotNull List<String> getStringList(String path) {
		return config.getStringList(path);
	}
}
