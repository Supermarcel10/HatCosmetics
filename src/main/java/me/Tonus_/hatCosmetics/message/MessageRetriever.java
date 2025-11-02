package me.Tonus_.hatCosmetics.message;

import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.ConfigRetriever;
import me.Tonus_.hatCosmetics.message.generics.IGenericsRetriever;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class MessageRetriever {
	private final Plugin plugin;
	private final ConfigRetriever configRetriever;
	private final IGenericsRetriever genericsRetriever;

	private final Map<String, FileConfiguration> translations = new HashMap<>();
	private String serverLocale;

	public MessageRetriever(
			Plugin plugin,
			ConfigRetriever configHandler,
			IGenericsRetriever genericsRetriever
	) {
		this.plugin = plugin;
		this.configRetriever = configHandler;
		this.genericsRetriever = genericsRetriever;

		init();
	}

	private void init() {
		// Default to en_US if the locale is not found
		serverLocale = configRetriever.getValue(ConfigReference.SERVER_LOCALE, "en_US");

		ensureTemplateExists();
		loadAllTranslations(); // TODO: Instead of loading all translations, load only the server locale, and then load the rest when needed (e.g. when a player joins)
		loadLocalTranslations();

        plugin.getSLF4JLogger().info("Loaded {} translations.", translations.size());
	}

	/**
	 * Reloads all translations
	 */
	public void reload() {
		translations.clear();
		init();
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
			plugin.getSLF4JLogger().error("Failed to load translations from JAR file! {}", e.toString());
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
            plugin.getSLF4JLogger().error("Failed to load translation file {}. {}", entryName, e.toString());
		}
	}

	/**
	 * Gets a message in the language of the server
	 * @param path Path to the message
	 * @return String message
	 */
	public @NotNull String getMessage(String path) {
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
		if (plugin.getConfig().getBoolean("force-locale") || !translations.containsKey(language)) {
			language = serverLocale;
		}

		FileConfiguration yaml = translations.get(language);
		if (yaml == null || !yaml.contains(path)) {
			var generic = genericsRetriever.getGeneric(path);
			if (generic != null) return parseColorCodes(generic);

			plugin.getSLF4JLogger().warn("Missing message ({}) for language {}!", path, language);
			return "MISSING MESSAGE";
		}

		return parseColorCodes(yaml.getString(path));
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull String path) {
		sender.sendMessage(Component.text(getMessage(sender, path)));
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull String path, String formatArg) {
		if (formatArg == null) {
			sendMessage(sender, path);
		} else {
			sender.sendMessage(Component.text(format(getMessage(sender, path), formatArg)));
		}
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull String path, Map<String, String> formatArgs) {
		if (formatArgs == null) {
			sendMessage(sender, path);
		} else {
			sender.sendMessage(Component.text(format(getMessage(sender, path), formatArgs)));
		}
	}

	public @NotNull String format(@NotNull String format, String formatArg) {
		StringBuilder result = new StringBuilder(format.length());
		int start = 0;
		int openBrace = format.indexOf('{', start);

		while (openBrace != -1) {
			int closeBrace = format.indexOf('}', openBrace);
			if (closeBrace == -1) {
				break;
			}

			result.append(format, start, openBrace);
			result.append(formatArg);
			start = closeBrace + 1;
			openBrace = format.indexOf('{', start);
		}

		// Append any remaining part of the template
		result.append(format, start, format.length());

		return result.toString();
	}

	public @NotNull String format(@NotNull String format, Map<String, String> formatArgs) {
		StringBuilder result = new StringBuilder(format.length());
		StringBuilder placeholder = new StringBuilder();
		boolean inPlaceholder = false;

		for (int i = 0; i < format.length(); i++) {
			char c = format.charAt(i);

			if (c == '{') {
				inPlaceholder = true;
				placeholder.setLength(0);
			} else if (c == '}' && inPlaceholder) {
				inPlaceholder = false;
				String key = placeholder.toString();
				if (formatArgs.containsKey(key)) {
					result.append(formatArgs.get(key));
				} else {
					logFormatWarning(key, format);
					result.append('{').append(key).append('}');
				}
			} else if (inPlaceholder) {
				placeholder.append(c);
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	private void logFormatWarning(String placeholder, String format) {
		plugin.getSLF4JLogger().warn(String.format("Value for placeholder \"%s\" is not defined in the template: %s", placeholder, format));

		if (plugin.getSLF4JLogger().isDebugEnabled()) {
			plugin.getSLF4JLogger().warn("Stacktrace:", new Exception("Placeholder not defined"));
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
	 * Formats a message for color by replacing all '&' with '§'
	 * @param msg Message to format
	 * @return Formatted message
	 */
	@Contract(pure = true)
	public static @NotNull String parseColorCodes(@NotNull String msg) {
		return msg.replaceAll("&([1-9a-eA-EKkLlMmNnOoRr])", "§$1");
	}
}
