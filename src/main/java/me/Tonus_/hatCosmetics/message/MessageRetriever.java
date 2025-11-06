package me.Tonus_.hatCosmetics.message;

import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.message.generics.IGenericsRetriever;
import me.Tonus_.hatCosmetics.utility.string.IStringFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class MessageRetriever {
	private final Plugin plugin;
	private final Logger logger;
	private final IConfigRetriever configRetriever;
	private final IColorParser colorParser;
	private final IGenericsRetriever genericsRetriever;
	private final IStringFormatter stringFormatter;

	private final Map<String, FileConfiguration> translations = new HashMap<>();

	public MessageRetriever(
			Plugin plugin,
			IConfigRetriever configHandler,
			IColorParser colorParser,
			IGenericsRetriever genericsRetriever,
			IStringFormatter stringFormatter
	) {
		this.plugin = plugin;
		this.logger = plugin.getSLF4JLogger();
		this.configRetriever = configHandler;
		this.colorParser = colorParser;
		this.genericsRetriever = genericsRetriever;
		this.stringFormatter = stringFormatter;

		ensureTemplateExists();
		loadAllTranslations(); // TODO: Instead of loading all translations, load only the server locale, and then load the rest when needed (e.g. when a player joins)
		loadLocalTranslations();

		plugin.getSLF4JLogger().info("Loaded {} translations.", translations.size());
	}

	/**
	 * Ensures that the template file exists
	 */
	private void ensureTemplateExists() {
		// Check if the messages directory exists
		File messageDir = new File(plugin.getDataFolder(), "messages");
		if (!messageDir.exists()) {
			plugin.saveResource("messages/", false);
		}

		// Check if the template file exists
		File templateFile = new File(messageDir, "template.yml");
		if (!templateFile.exists()) {
			plugin.saveResource("messages/template.yml", false);
		}
	}

	/**
	 * Loads all local translations
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void loadLocalTranslations() {
		File messageDir = new File(plugin.getDataFolder(), "messages");
		if (!messageDir.exists()) {
			messageDir.mkdirs();
		}

		if (!messageDir.isDirectory()) {
			plugin.getSLF4JLogger().error("Messages is not a directory!");
			return;
		}

		File[] files = messageDir.listFiles();
		if (files == null) return;

		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".yml")) {
				translations.put(
						file.getName().replace(".yml", ""),
						YamlConfiguration.loadConfiguration(file)
				);
			}
		}
	}

	/**
	 * Loads all generic translations from the JAR file
	 */
	private void loadAllTranslations() {
		try (JarFile jarFile = new JarFile(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
			Enumeration<JarEntry> entries = jarFile.entries();

			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (isValidTranslationFile(entry)) loadTranslationFile(entry);
			}
		} catch (IOException e) {
			logger.error("Failed to load translations from JAR file! {}", e.toString());
		}
	}

	/**
	 * Loads the translation file into memory
	 * @param entry JarEntry to load
	 */
	private void loadTranslationFile(@NotNull JarEntry entry) {
		String entryName = entry.getName();

		try (InputStream inputStream = plugin.getClass().getResourceAsStream("/" + entryName)) {
			if (inputStream != null) {
				String locale = entry.getName().replace("messages/", "").replace(".yml", "");
				YamlConfiguration language = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
				translations.put(locale, language);
			}
		} catch (IOException e) {
            logger.error("Failed to load translation file {}. {}", entryName, e.toString());
		}
	}

	/**
	 * Checks if the entry is a valid translation file
	 * @param entry JarEntry to check
	 * @return boolean if the entry is a valid translation file
	 */
	private static boolean isValidTranslationFile(@NotNull JarEntry entry) {
		String entryName = entry.getName();
		return entryName.startsWith("messages/") &&
				!entry.isDirectory() &&
				entryName.endsWith(".yml") &&
				!entryName.equals("messages/template.yml");
	}

	/**
	 * Gets a message in the language of the server
	 * @param path Path to the message
	 * @return String message
	 */
	public @NotNull String getMessage(String path) {
		var serverLocale = configRetriever.getValue(ConfigReference.SERVER_LOCALE, "en_US");
		return getMessage(serverLocale, path);
	}

	/**
	 * Gets a message in the language of the player
	 * @param sender Player to get the message for
	 * @param path Path to the message
	 * @return String message
	 */
	public @NotNull String getMessage(@NotNull CommandSender sender, String path) {
		if (sender instanceof Player player) {
			return getMessage(player.locale().getLanguage(), path);
		}

		return getMessage(path);
	}

	/**
	 * Gets a message in the specified language
	 * @param language Language to get the message for, formatted in <a href="https://en.wikipedia.org/wiki/IETF_language_tag">BCP 47</a>
	 * @param path Path to the message
	 * @see <a href="https://www.rfc-editor.org/info/bcp47">BCP 47 - RFC</a>
	 * @return String message
	 */
	private @NotNull String getMessage(String language, String path) {
		// Check if server has a forced locale or if the language is not found
		// Use the default server locale if the language is not found
		var forcedLocale = configRetriever.getValue(ConfigReference.FORCED_LOCALE);
		if (Boolean.TRUE.equals(forcedLocale) || !translations.containsKey(language)) {
            language = configRetriever.getValue(ConfigReference.SERVER_LOCALE, "en_US");
		}

		var yaml = translations.get(language);
		if (yaml == null || !yaml.contains(path)) {
			var generic = genericsRetriever.getGeneric(path);
			if (generic != null) return generic;

			logger.warn("Missing message \"{}\" for language {}!", path, language);
			return "<MISSING MESSAGE - REPORT THIS>";
		}

		return colorParser.parse(yaml.getString(path));
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull String path) {
		sender.sendMessage(Component.text(getMessage(sender, path)));
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull String path, String formatArg) {
		var formattedMessage = stringFormatter.format(getMessage(sender, path), formatArg);
		sender.sendMessage(Component.text(formattedMessage));
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull String path, Map<String, String> formatArgs) {
		var formattedMessage = stringFormatter.format(getMessage(sender, path), formatArgs);
		sender.sendMessage(Component.text(formattedMessage));
	}
}
