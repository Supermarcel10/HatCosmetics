package me.Tonus_.hatCosmetics.messages.translations;

import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.message.translations.TranslationRetriever;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;


public class TranslationRetrieverTests {
    private final static MessageReference existingPath = MessageReference.VERSION;

    private final Plugin plugin = mock();

    @Test
    void tryGetTranslation_whenLanguageInvalid_shouldReturnNull() {
        // Arrange
        var language = "DOES_NOT_EXIST";
        var nonExistentPath = MessageReference.createReference("NON_EXISTENT_PATH");

        var translations = new HashMap<String, FileConfiguration>();

        var sut = new TranslationRetriever(plugin, translations);

        // Act
        var result = sut.tryGetTranslation(language, nonExistentPath);

        // Assert
        assertNull(result);
    }

    @Test
    void tryGetTranslation_whenLanguageValidAndKeyNotPresent_shouldReturnNull() {
        // Arrange
        var language = "language";

        var translations = new HashMap<String, FileConfiguration>();
        var yamlFile = mock(FileConfiguration.class);
        translations.put(language, yamlFile);

        var sut = new TranslationRetriever(plugin, translations);

        // Act
        var result = sut.tryGetTranslation(language, existingPath);

        // Assert
        assertNull(result);
    }

    @Test
    void tryGetTranslation_whenLanguageAndKeyValid_shouldReturnTranslatedString() {
        // Arrange
        var language = "language";
        var expectedResult = "result";

        var translations = new HashMap<String, FileConfiguration>();
        var yamlFile = mock(FileConfiguration.class);
        doReturn(expectedResult).when(yamlFile).getString(existingPath.getYamlPath());
        translations.put(language, yamlFile);

        var sut = new TranslationRetriever(plugin, translations);

        // Act
        var result = sut.tryGetTranslation(language, existingPath);

        // Assert
        assertEquals(expectedResult, result);
    }
}
