package me.Tonus_.hatCosmetics.updates;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class PluginVersionRetrieverTests {
    private final Plugin plugin = mock();
    private final PluginDescriptionFile pdf = mock();
    private final PluginVersionRetriever sut = new PluginVersionRetriever(plugin);

    PluginVersionRetrieverTests() {
        doReturn(pdf).when(plugin).getDescription();
    }

    @ParameterizedTest
    @CsvSource({
            "0.0.0",
            "0.0.1",
            "0.1.2",
            "1.2.3"
    })
    void getVersion_whenValidPluginVersion_shouldReturnVersion(String expectedVersion) {
        // Arrange
        doReturn(expectedVersion).when(pdf).getVersion();

        // Act
        var result = sut.getVersion();

        // Assert
        assertEquals(expectedVersion, result);
    }
}
