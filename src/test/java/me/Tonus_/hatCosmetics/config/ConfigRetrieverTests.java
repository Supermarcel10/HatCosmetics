package me.Tonus_.hatCosmetics.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;


public class ConfigRetrieverTests {
    private final Logger logger = mock();
    private final FileConfiguration configuration = mock();
    private final ConfigRetriever sut = new ConfigRetriever(logger, configuration);

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
        verify(logger, never()).warn(anyString(), any(), any(), anyString());
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
        verify(logger).warn(anyString(), eq(expectedValue), eq(configReference.yamlPath), anyString());
    }

    @Test
    void getValue_whenValueNull_shouldReturnNull() {
        // Arrange
        var configReference = ConfigReference.VERSION;

        // Act
        var result = sut.getValue(configReference);

        // Assert
        assertNull(result);
        verify(logger, never()).warn(anyString(), any(), any(), anyString());
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
        verify(logger, never()).warn(anyString(), any(), any(), anyString());
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
        verify(logger, never()).warn(anyString(), any(), any(), anyString());
    }
}
