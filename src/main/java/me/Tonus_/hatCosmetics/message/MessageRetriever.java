package me.Tonus_.hatCosmetics.message;

import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.message.generics.IGenericsRetriever;
import me.Tonus_.hatCosmetics.message.translations.ITranslationRetriever;
import me.Tonus_.hatCosmetics.utility.string.IStringFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.util.*;


@RequiredArgsConstructor
public class MessageRetriever {
	private final Plugin plugin;
	private final IConfigRetriever configRetriever;
	private final IColorParser colorParser;
	private final IGenericsRetriever genericsRetriever;
	private final ITranslationRetriever translationRetriever;
	private final IStringFormatter stringFormatter;

	// TODO: Move template file somewhere else.
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
	 * Gets a message in the language of the server
	 * @param path Path to the message
	 * @return String message
	 */
	// TODO: See if this can be converted to private and consolidated into one
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

	/**
	 * Gets a message in the specified language
	 * @param language Language to get the message for, formatted in <a href="https://en.wikipedia.org/wiki/IETF_language_tag">BCP 47</a>
	 * @param path Path to the message
	 * @see <a href="https://www.rfc-editor.org/info/bcp47">BCP 47 - RFC</a>
	 * @return String message
	 */
	private @NotNull String getMessage(String language, String path) {
		// Check generics first
		String generic = genericsRetriever.getGeneric(path);
		if (generic != null) return generic;

		// Check server locale if forced
		if (isForcedLocale()) return getServerMessage(path);

		// Try language-specific locale
		var translation = getFormattedTranslation(language, path);
		if (translation != null) return translation;

		// Fallback to server locale
		return getServerMessage(path);
	}

	/**
	 * Gets a message in the server locale
	 * @param path Path to the message
	 * @return String message
	 */
	private @NotNull String getServerMessage(String path) {
		var fallback = "en_US";
		var language = configRetriever.getValue(ConfigReference.SERVER_LOCALE, fallback);

		// Try server locale
		var translation = getFormattedTranslation(language, path);
		if (translation != null) return translation;

		// Fallback to en_US
		plugin.getSLF4JLogger().warn("Missing message \"{}\" for language {}!", path, language);
		translation = getFormattedTranslation(fallback, path);
		return translation != null ? translation : "<MISSING TRANSLATION - REPORT THIS>";
	}

	/**
	 * Retrieves translation of a given message in a language
	 * @param language Language to get the message for, formatted in <a href="https://en.wikipedia.org/wiki/IETF_language_tag">BCP 47</a>
	 * @param path Path to the message
	 * @see <a href="https://www.rfc-editor.org/info/bcp47">BCP 47 - RFC</a>
	 * @return String message
	 */
	private @Nullable String getFormattedTranslation(String language, String path) {
		var msg = translationRetriever.tryGetTranslation(language, path);
		return msg != null ? colorParser.parse(msg) : null;
	}

	private boolean isForcedLocale() {
		return Boolean.TRUE.equals(configRetriever.getValue(ConfigReference.FORCED_LOCALE));
	}
}
