package me.Tonus_.hatCosmetics.utility;

import me.Tonus_.hatCosmetics.HatCosmetics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;


public class Configs {
	private static FileConfiguration config;
	private static File configFile;

	public static void init() {
		saveDefault();
		config = YamlConfiguration.loadConfiguration(configFile);
	}

	public static void reload() {
		init();
	}

	private static void saveDefault() {
		if (configFile == null) {
			configFile = new File(HatCosmetics.getInstance().getDataFolder(), "config.yml");
		}

		if (!configFile.exists()) {
			HatCosmetics.getInstance().saveResource("config.yml", false);
		}
	}

	public static String getString(String path) {
		return config.getString(path);
	}

	public static int getInt(String path) {
		return config.getInt(path);
	}

	public static boolean getBoolean(String path) {
		return config.getBoolean(path);
	}

	public static @NotNull List<String> getStringList(String path) {
		return config.getStringList(path);
	}
}
