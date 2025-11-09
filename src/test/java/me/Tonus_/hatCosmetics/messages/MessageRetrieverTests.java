package me.Tonus_.hatCosmetics.messages;

import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.message.color.IColorParser;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageRetriever;
import me.Tonus_.hatCosmetics.message.generics.IGenericsRetriever;
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
    private final static String SERVER_LOCALE = "SERVER LOCALE";

    private final Plugin plugin = mock();
    private final IConfigRetriever configRetriever = mock();
    private final IColorParser colorParser = mock();
    private final IGenericsRetriever genericsRetriever = mock();
    private final ITranslationRetriever translationRetriever = mock();
    private final IStringFormatter stringFormatter = mock();

    private final IMessageRetriever sut = new MessageRetriever(plugin, configRetriever, colorParser, genericsRetriever, translationRetriever, stringFormatter);

    MessageRetrieverTests() {
        // Mock player logger
        doReturn(mock(Logger.class)).when(plugin).getSLF4JLogger();

        // Mock default responses
        doAnswer(invocation -> invocation.getArgument(0)).when(colorParser).parse(anyString());

        doReturn(SERVER_LOCALE).when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, "en_US");
        doReturn(null).when(genericsRetriever).getGeneric(anyString());
        doReturn(false).when(configRetriever).getValue(ConfigReference.FORCED_LOCALE);

        doReturn(null).when(translationRetriever).tryGetTranslation(anyString(), anyString());
        doReturn("SERVER RESPONSE").when(translationRetriever).tryGetTranslation(eq(SERVER_LOCALE), anyString());
    }

    @Test
    void getMessage_whenPathDoesNotExist_shouldReturnMissingMessage() {
        // Arrange
        var path = "DOES_NOT_EXIST";
        var expectedResult = "<MISSING TRANSLATION - REPORT THIS>";

        doReturn(null).when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, "en_US");

        // Act
        var result = sut.getMessage(path);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void getMessage_whenGenericExists_shouldReturnGeneric() {
        // Arrange
        var path = "path";
        var expectedResult = "GENERIC MESSAGE";

        doReturn(expectedResult).when(genericsRetriever).getGeneric(path);

        // Act
        var result = sut.getMessage(path);

        // Assert
        assertEquals(expectedResult, result);
        verify(genericsRetriever).getGeneric(path);
    }

    @Test
    void getMessage_whenTranslationExists_shouldReturnTranslation() {
        // Arrange
        var path = "path";
        var expectedResult = "SERVER RESPONSE";

        // Act
        var result = sut.getMessage(path);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, path);
    }

    @Test
    void getMessage_whenServerLocaleTranslationMissing_shouldFallbackToEnglishUSLocale() {
        // Arrange
        var path = "path";
        var serverLocale = "fr_FR";
        var fallbackLocale = "en_US";
        var expectedMessage = "Fallback Message";

        doReturn(serverLocale).when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, "en_US");
        doReturn(null).when(translationRetriever).tryGetTranslation(serverLocale, path);
        doReturn(expectedMessage).when(translationRetriever).tryGetTranslation(fallbackLocale, path);

        // Act
        var result = sut.getMessage(path);

        // Assert
        assertEquals(expectedMessage, result);
        verify(translationRetriever).tryGetTranslation(serverLocale, path);
        verify(translationRetriever).tryGetTranslation(fallbackLocale, path);
    }

    @Test
    void getMessage_whenAllTranslationsMissing_shouldReturnMissingString() {
        // Arrange
        var path = "path";
        var serverLocale = "fr_FR";

        doReturn(null).when(translationRetriever).tryGetTranslation(serverLocale, path);
        doReturn(null).when(translationRetriever).tryGetTranslation(SERVER_LOCALE, path);

        // Act
        var result = sut.getMessage(path);

        // Assert
        assertEquals("<MISSING TRANSLATION - REPORT THIS>", result);
    }

    @Test
    void getMessage_whenServerLocaleSameAsFallbackAndPathNotPresent_shouldReturnMissingString() {
        // Arrange
        var path = "path";

        doReturn("en_US").when(configRetriever).getValue(ConfigReference.SERVER_LOCALE, "en_US");
        doReturn(null).when(translationRetriever).tryGetTranslation(SERVER_LOCALE, path);

        // Act
        var result = sut.getMessage(path);

        // Assert
        assertEquals("<MISSING TRANSLATION - REPORT THIS>", result);
    }

    @Test
    void getMessage_withPlayer_whenGenericExists_shouldReturnGeneric() {
        // Arrange
        var path = "path";
        var playerLocale = "es_ES";
        var player = mockPlayer(playerLocale);
        var expectedResult = "GENERIC RESPONSE";

        doReturn(expectedResult).when(genericsRetriever).getGeneric(path);

        // Act
        var result = sut.getMessage(player, path);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever, never()).tryGetTranslation(playerLocale, path);
    }

    @Test
    void getMessage_withPlayer_whenLanguageSpecificTranslationExists_shouldReturnTranslation() {
        // Arrange
        var path = "path";
        var playerLocale = "es_ES";
        var player = mockPlayer(playerLocale);
        var expectedResult = "TRANSLATED MESSAGE";

        doReturn(expectedResult).when(translationRetriever).tryGetTranslation(playerLocale, path);

        // Act
        var result = sut.getMessage(player, path);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(playerLocale, path);
    }

    @Test
    void getMessage_withPlayer_whenLanguageSpecificTranslationMissing_shouldFallbackToServerLocale() {
        // Arrange
        var path = "path";
        var playerLocale = "es_ES";
        var player = mockPlayer(playerLocale);
        var expectedResult = "SERVER RESPONSE";

        // Act
        var result = sut.getMessage(player, path);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(playerLocale, path);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, path);
    }

    @Test
    void getMessage_withPlayer_whenForcedLocale_shouldIgnorePlayerLocale() {
        // Arrange
        var path = "path";
        var playerLocale = "es_ES";
        var player = mockPlayer(playerLocale);
        var expectedResult = "SERVER RESPONSE";

        when(configRetriever.getValue(ConfigReference.FORCED_LOCALE)).thenReturn(true);

        // Act
        var result = sut.getMessage(player, path);

        // Assert
        assertEquals(expectedResult, result);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, path);
        verify(translationRetriever, never()).tryGetTranslation(playerLocale, path);
    }

    @Test
    void getMessage_withNonPlayerSender_shouldUseServerLocale() {
        // Arrange
        var path = "path";
        var expectedTranslation = "SERVER RESPONSE";
        var sender = mock(CommandSender.class);

        // Act
        var result = sut.getMessage(sender, path);

        // Assert
        assertEquals(expectedTranslation, result);
        verify(translationRetriever).tryGetTranslation(SERVER_LOCALE, path);
    }

    @Test
    void sendMessage_withNoFormatting_shouldRetrieveAndSendMessage() {
        // Arrange
        var path = "path";
        var expectedMessage = "SERVER RESPONSE";
        var sender = mock(CommandSender.class);

        // Act
        sut.sendMessage(sender, path);

        // Assert
        verify(sender).sendMessage(Component.text(expectedMessage));
    }

    @Test
    void sendMessage_withSingleFormatArg_shouldFormatMessage() {
        // Arrange
        var path = "path";
        var rawMessage = "Hello {name}";
        var formatArg = "John";
        var expectedResult = "Hello John";
        var sender = mock(CommandSender.class);

        doReturn(rawMessage).when(translationRetriever).tryGetTranslation(anyString(), eq(path));
        doReturn(expectedResult).when(stringFormatter).format(rawMessage, formatArg);

        // Act
        sut.sendMessage(sender, path, formatArg);

        // Assert
        verify(sender).sendMessage(Component.text(expectedResult));
        verify(stringFormatter).format(rawMessage, formatArg);
    }

    @Test
    void sendMessage_withMultipleFormatArgs_shouldFormatMessage() {
        // Arrange
        var path = "path";
        var rawMessage = "Hello {name}, you have {count} cosmetics";
        var formatArgs = Map.of("name", "John", "count", "5");
        var expectedResult = "Hello John, you have 5 cosmetics";
        var sender = mock(CommandSender.class);

        doReturn(rawMessage).when(translationRetriever).tryGetTranslation(anyString(), eq(path));
        doReturn(expectedResult).when(stringFormatter).format(rawMessage, formatArgs);

        // Act
        sut.sendMessage(sender, path, formatArgs);

        // Assert
        verify(sender).sendMessage(Component.text(expectedResult));
        verify(stringFormatter).format(rawMessage, formatArgs);
    }

    private Player mockPlayer(String language) {
        var locale = mock(Locale.class);
        var player = mock(Player.class);

        doReturn(locale).when(player).locale();
        doReturn(language).when(locale).getLanguage();

        return player;
    }
}
