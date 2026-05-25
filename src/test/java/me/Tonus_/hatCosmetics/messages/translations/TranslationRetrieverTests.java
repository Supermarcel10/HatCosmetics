package me.Tonus_.hatCosmetics.messages.translations;

import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.message.translations.TranslationRetriever;
import me.Tonus_.hatCosmetics.utility.jar.IJarAccessor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;


public class TranslationRetrieverTests {
    private final static MessageReference existingPath = MessageReference.VERSION;

    private final Plugin plugin = mock();
    private final IJarAccessor jarAccessor = mock();

    @Test
    void tryGetTranslation_whenLocaleValidAndKeyNotPresent_shouldReturnNull() {
        // Arrange
        var locale = new Locale("en", "us");

        var translations = new HashMap<String, FileConfiguration>();
        var yamlFile = mock(FileConfiguration.class);
        translations.put(locale.toString(), yamlFile);

        var sut = new TranslationRetriever(plugin, translations, jarAccessor);

        // Act
        var result = sut.tryGetTranslation(locale, existingPath);

        // Assert
        assertNull(result);
    }

    @Test
    void tryGetTranslation_whenLocaleAndKeyValid_shouldReturnTranslatedString() {
        // Arrange
        var locale = new Locale("en", "us");
        var expectedResult = "result";

        var translations = new HashMap<String, FileConfiguration>();
        var yamlFile = mock(FileConfiguration.class);
        doReturn(expectedResult).when(yamlFile).getString(existingPath.getYamlPath());
        translations.put(locale.toString(), yamlFile);

        var sut = new TranslationRetriever(plugin, translations, jarAccessor);

        // Act
        var result = sut.tryGetTranslation(locale, existingPath);

        // Assert
        assertEquals(expectedResult, result);
    }
}
