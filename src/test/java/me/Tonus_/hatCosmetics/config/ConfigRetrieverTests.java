package me.Tonus_.hatCosmetics.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class ConfigRetrieverTests {
    private final Plugin plugin = mock();
    private final Logger logger = mock();
    private final FileConfiguration configuration = mock();
    private final ConfigRetriever sut = new ConfigRetriever(plugin, configuration);

    ConfigRetrieverTests() {
        doReturn(logger).when(plugin).getSLF4JLogger();
    }

    @Test
    void getValue_whenValueExists_shouldReturnValueCast() {
        // Arrange
        var configReference = ConfigReference.VERSION;

        var expectedValue = "1";
        doReturn(expectedValue).when(configuration).get(configReference.yamlPath);

        // Act
        var result = sut.getValue(configReference);

        // Assert
        assertEquals(expectedValue, result);
        assertEquals(expectedValue.getClass(), configReference.type);
        verify(logger, never()).warn(anyString(), any(Object.class));
    }

    @Test
    void getValue_whenValueExistsAndTypeInconsistent_shouldReturnNullWithWarning() {
        // Arrange
        var configReference = ConfigReference.VERSION;

        var expectedValue = 1;
        doReturn(expectedValue).when(configuration).get(configReference.yamlPath);

        // Act
        var result = sut.getValue(configReference);

        // Assert
        assertNull(result);
        assertLoggedWarningMessage(configReference, expectedValue);
    }

    @Test
    void getValue_whenValueNull_shouldReturnNull() {
        // Arrange
        var configReference = ConfigReference.VERSION;

        // Act
        var result = sut.getValue(configReference);

        // Assert
        assertNull(result);
        verify(logger, never()).warn(anyString(), any(Object.class));
    }

    @Test
    void getValueWithDefault_whenValuePresent_shouldReturnValue() {
        // Arrange
        var configReference = ConfigReference.VERSION;

        var expectedValue = "1";
        doReturn(expectedValue).when(configuration).get(configReference.yamlPath);

        // Act
        var result = sut.getValue(configReference, "2");

        // Assert
        assertEquals(expectedValue, result);
        verify(logger, never()).warn(anyString(), any(Object.class));
    }

    @Test
    void getValueWithDefault_whenValueNull_shouldReturnDefault() {
        // Arrange
        var configReference = ConfigReference.VERSION;

        var expectedValue = "1";

        // Act
        var result = sut.getValue(configReference, expectedValue);

        // Assert
        assertEquals(expectedValue, result);
        verify(logger, never()).warn(anyString(), any(Object.class));
    }

    @Test
    void getValueWithInvalidTypeDefault_whenValueNull_shouldReturnWarning() {
        // Arrange
        var configReference = ConfigReference.VERSION;

        // Act
        var result = sut.getValue(configReference, 1);

        // Assert
        assertNull(result);
        assertLoggedWarningMessage(configReference, null);
    }

    public void assertLoggedWarningMessage(@NotNull ConfigReference configReference, Object expectedValue) {
        var simpleName = expectedValue == null ? null : expectedValue.getClass().getSimpleName();

        verify(logger, times(1)).warn(
                "Config value ({}) at path '{}' is not of expected type {}. Found {}.",
                expectedValue,
                configReference.yamlPath,
                configReference.type.getSimpleName(),
                simpleName
        );
    }
}
