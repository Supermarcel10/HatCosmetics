package me.Tonus_.hatCosmetics.messages;

import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageRetriever;
import me.Tonus_.hatCosmetics.message.translations.ITranslationRetriever;
import me.Tonus_.hatCosmetics.utility.string.IStringFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class MessageRetrieverTests {
    private final static MessageReference existingPath = MessageReference.VERSION;
    private final static Locale FALLBACK_LOCALE = new Locale("en", "us");
    private final static Locale SERVER_LOCALE = new Locale("en", "uk");

    private final Plugin plugin = mock();
    private final IConfigRetriever configRetriever = mock();
    private final ITranslationRetriever translationRetriever = mock();
    private final IStringFormatter stringFormatter = mock();

    private final IMessageRetriever sut = new MessageRetriever(plugin, configRetriever, translationRetriever, stringFormatter);

    MessageRetrieverTests() {
        // Mock player logger
        doReturn(mock(Logger.class)).when(plugin).getSLF4JLogger();

        // Mock default responses
        doReturn(SERVER_LOCALE).when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, FALLBACK_LOCALE);
        doReturn(false).when(configRetriever).getValue(ConfigReference.FORCED_LOCALE);

        doReturn(null).when(translationRetriever).tryGetTranslation(any(Locale.class), any(MessageReference.class));
        doReturn("SERVER RESPONSE").when(translationRetriever).tryGetTranslation(eq(SERVER_LOCALE), any(MessageReference.class));
    }

    @Test
    void getMessage_whenPathDoesNotExist_shouldReturnMissingMessage() {
        // Arrange
        var nonExistentPath = MessageReference.createReference("DOES_NOT_EXIST");
        var expectedResult = "<MISSING TRANSLATION - REPORT THIS>";

        doReturn(null).when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, FALLBACK_LOCALE);

        // Act
        var result = sut.getMessage(nonExistentPath);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void getMessage_whenTranslationExists_shouldReturnTranslation() {
        // Arrange
        var expectedResult = "SERVER RESPONSE";

        // Act
        var result = sut.getMessage(existingPath);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, existingPath);
    }

    @Test
    void getMessage_whenServerLocaleTranslationMissing_shouldFallbackToEnglishUSLocale() {
        // Arrange
        var serverLocale = new Locale("fr", "fr");
        var expectedMessage = "Fallback Message";

        doReturn(serverLocale).when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, FALLBACK_LOCALE);
        doReturn(null).when(translationRetriever).tryGetTranslation(serverLocale, existingPath);
        doReturn(expectedMessage).when(translationRetriever).tryGetTranslation(FALLBACK_LOCALE, existingPath);

        // Act
        var result = sut.getMessage(existingPath);

        // Assert
        assertEquals(expectedMessage, result);
        verify(translationRetriever).tryGetTranslation(serverLocale, existingPath);
        verify(translationRetriever).tryGetTranslation(FALLBACK_LOCALE, existingPath);
    }

    @Test
    void getMessage_whenAllTranslationsMissing_shouldReturnMissingString() {
        // Arrange
        var serverLocale = new Locale("fr", "fr");

        doReturn(null).when(translationRetriever).tryGetTranslation(serverLocale, existingPath);
        doReturn(null).when(translationRetriever).tryGetTranslation(SERVER_LOCALE, existingPath);

        // Act
        var result = sut.getMessage(existingPath);

        // Assert
        assertEquals("<MISSING TRANSLATION - REPORT THIS>", result);
    }

    @Test
    void getMessage_whenServerLocaleSameAsFallbackAndPathNotPresent_shouldReturnMissingString() {
        // Arrange
        doReturn(new Locale("en", "us")).when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, FALLBACK_LOCALE);
        doReturn(null).when(translationRetriever).tryGetTranslation(SERVER_LOCALE, existingPath);

        // Act
        var result = sut.getMessage(existingPath);

        // Assert
        assertEquals("<MISSING TRANSLATION - REPORT THIS>", result);
    }

    @Test
    void getMessage_withPlayer_whenLanguageSpecificTranslationExists_shouldReturnTranslation() {
        // Arrange
        var playerLocale = new Locale("es", "es");
        var player = mockPlayer(playerLocale);
        var expectedResult = "TRANSLATED MESSAGE";

        doReturn(expectedResult).when(translationRetriever).tryGetTranslation(playerLocale, existingPath);

        // Act
        var result = sut.getMessage(player, existingPath);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(playerLocale, existingPath);
    }

    @Test
    void getMessage_withPlayer_whenLanguageSpecificTranslationMissing_shouldFallbackToServerLocale() {
        // Arrange
        var playerLocale = new Locale("es", "es");
        var player = mockPlayer(playerLocale);
        var expectedResult = "SERVER RESPONSE";

        // Act
        var result = sut.getMessage(player, existingPath);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(playerLocale, existingPath);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, existingPath);
    }

    @Test
    void getMessage_withPlayer_whenForcedLocale_shouldIgnorePlayerLocale() {
        // Arrange
        var playerLocale = new Locale("es", "es");
        var player = mockPlayer(playerLocale);
        var expectedResult = "SERVER RESPONSE";

        when(configRetriever.getValue(ConfigReference.FORCED_LOCALE)).thenReturn(true);

        // Act
        var result = sut.getMessage(player, existingPath);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, existingPath);
        verify(translationRetriever, never()).tryGetTranslation(playerLocale, existingPath);
    }

    @Test
    void getMessage_withNonPlayerSender_shouldUseServerLocale() {
        // Arrange
        var path = "path";
        var expectedTranslation = "SERVER RESPONSE";
        var sender = mock(CommandSender.class);

        // Act
        var result = sut.getMessage(sender, existingPath);

        // Assert
        assertEquals(expectedTranslation, result);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, existingPath);
    }

    @Test
    void sendMessage_withNoFormatting_shouldRetrieveAndSendMessage() {
        // Arrange
        var expectedMessage = "SERVER RESPONSE";
        var sender = mock(CommandSender.class);

        // Act
        sut.sendMessage(sender, existingPath);

        // Assert
        verify(sender).sendMessage(Component.text(expectedMessage));
    }

    @Test
    void sendMessage_withSingleFormatArg_shouldFormatMessage() {
        // Arrange
        var rawMessage = "Hello {name}";
        var formatArg = "John";
        var expectedResult = "Hello John";
        var sender = mock(CommandSender.class);

        doReturn(rawMessage).when(translationRetriever).tryGetTranslation(any(Locale.class), eq(existingPath));
        doReturn(expectedResult).when(stringFormatter).format(rawMessage, formatArg);

        // Act
        sut.sendMessage(sender, existingPath, formatArg);

        // Assert
        verify(sender).sendMessage(Component.text(expectedResult));
        verify(stringFormatter).format(rawMessage, formatArg);
    }

    @Test
    void sendMessage_withMultipleFormatArgs_shouldFormatMessage() {
        // Arrange
        var rawMessage = "Hello {name}, you have {count} cosmetics";
        var formatArgs = Map.of("name", "John", "count", "5");
        var expectedResult = "Hello John, you have 5 cosmetics";
        var sender = mock(CommandSender.class);

        doReturn(rawMessage).when(translationRetriever).tryGetTranslation(any(Locale.class), eq(existingPath));
        doReturn(expectedResult).when(stringFormatter).format(rawMessage, formatArgs);

        // Act
        sut.sendMessage(sender, existingPath, formatArgs);

        // Assert
        verify(sender).sendMessage(Component.text(expectedResult));
        verify(stringFormatter).format(rawMessage, formatArgs);
    }

    private Player mockPlayer(Locale locale) {
        var player = mock(Player.class);

        doReturn(locale).when(player).locale();

        return player;
    }
}
