package me.Tonus_.hatCosmetics.message;

import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.message.color.IColorParser;
import me.Tonus_.hatCosmetics.message.translations.ITranslationRetriever;
import me.Tonus_.hatCosmetics.utility.string.IStringFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;


@RequiredArgsConstructor
public class MessageRetriever implements IMessageRetriever {
	private static final Locale FALLBACK_LOCALE = new Locale("en", "us");
	private static final String MISSING_STRING = "<MISSING TRANSLATION - REPORT THIS>";

	private final Plugin plugin;
	private final IConfigRetriever configRetriever;
	private final IColorParser colorParser;
	private final ITranslationRetriever translationRetriever;
	private final IStringFormatter stringFormatter;

	/**
	 * Gets a message in the locale of the server
	 * @param messageReference Path to the message
	 * @return String message
	 */
	public @NotNull String getMessage(MessageReference messageReference) {
        var locale = configRetriever.getValue(ConfigReference.SERVER_LOCALE, FALLBACK_LOCALE);

        // Try server locale
        var translation = getFormattedTranslation(locale, messageReference);
        if (translation != null) return translation;

        // Short circuit if locale same as fallback & failed
        plugin.getSLF4JLogger().warn("Missing message \"{}\" for locale {}!", messageReference.getYamlPath(), locale);
        if (Objects.equals(locale, FALLBACK_LOCALE)) return MISSING_STRING;

        // Fallback to en_US
        translation = getFormattedTranslation(FALLBACK_LOCALE, messageReference);
        if (translation != null) return translation;

        // Display fail message
        plugin.getSLF4JLogger().error("Missing message \"{}\" for locale {}!", messageReference, FALLBACK_LOCALE);
        return MISSING_STRING;
	}

	/**
	 * Gets a message in the locale of the player
	 * @param sender Player to get the message for
	 * @param messageReference Path to the message
	 * @return String message
	 */
	public @NotNull String getMessage(@NotNull CommandSender sender, MessageReference messageReference) {
		if (sender instanceof Player player) {
			return getMessage(player.locale(), messageReference);
		}

		return getMessage(messageReference);
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull MessageReference messageReference) {
		sender.sendMessage(Component.text(getMessage(sender, messageReference)));
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull MessageReference messageReference, String formatArg) {
		var message = getMessage(sender, messageReference);
        var formattedMessage = stringFormatter.format(message, formatArg);
		sender.sendMessage(Component.text(formattedMessage));
	}

	public void sendMessage(@NotNull CommandSender sender, @NotNull MessageReference messageReference, Map<String, String> formatArgs) {
		var message = getMessage(sender, messageReference);
        var formattedMessage = stringFormatter.format(message, formatArgs);
		sender.sendMessage(Component.text(formattedMessage));
	}

	/**
	 * Gets a message in the specified locale
	 * @param locale Locale to get the message for, formatted in <a href="https://en.wikipedia.org/wiki/IETF_locale_tag">BCP 47</a>
	 * @param messageReference Path to the message
	 * @see <a href="https://www.rfc-editor.org/info/bcp47">BCP 47 - RFC</a>
	 * @return String message
	 */
	private @NotNull String getMessage(Locale locale, MessageReference messageReference) {
		// Check server locale if forced
		if (isForcedLocale()) return getMessage(messageReference);

		// Try locale-specific locale
		var translation = getFormattedTranslation(locale, messageReference);
		if (translation != null) return translation;

		// Fallback to server locale
		return getMessage(messageReference);
	}

	/**
	 * Retrieves translation of a given message in a locale
	 * @param locale Locale to get the message for, formatted in <a href="https://en.wikipedia.org/wiki/IETF_locale_tag">BCP 47</a>
	 * @param messageReference Path to the message
	 * @see <a href="https://www.rfc-editor.org/info/bcp47">BCP 47 - RFC</a>
	 * @return String message
	 */
	private @Nullable String getFormattedTranslation(Locale locale, MessageReference messageReference) {
		var msg = translationRetriever.tryGetTranslation(locale, messageReference);
		return msg != null ? colorParser.parse(msg) : null;
	}

	private boolean isForcedLocale() {
		return Boolean.TRUE.equals(configRetriever.getValue(ConfigReference.FORCED_LOCALE));
	}
}
