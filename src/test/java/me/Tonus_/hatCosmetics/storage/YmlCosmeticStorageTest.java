package me.Tonus_.hatCosmetics.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import me.Tonus_.hatCosmetics.utility.jar.IJarAccessor;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;


class YmlCosmeticStorageTest {
    @TempDir
    Path tempDir;

    private Plugin plugin = mock(Plugin.class);
    private Logger logger = mock(Logger.class);
    private IJarAccessor jarAccessor = mock(IJarAccessor.class);

    private YmlCosmeticStorage sut = new YmlCosmeticStorage(
        plugin,
        logger,
        jarAccessor
    );

    @BeforeEach
    void setUp() throws IOException {
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        Files.createDirectories(tempDir.resolve("cosmetics"));
    }

    @Test
    void shouldSortCosmeticsByOrder() throws IOException {
        // Arrange
        writeCosmetic(
            "a.yml",
            """
            material: FEATHER
            order: 3
            """
        );

        writeCosmetic(
            "b.yml",
            """
            material: FEATHER
            order: 1
            """
        );

        writeCosmetic(
            "c.yml",
            """
            material: FEATHER
            order: 2
            """
        );

        // Act
        sut.reload();
        var result = sut.loadAll();

        // Assert
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).order());
        assertEquals(2, result.get(1).order());
        assertEquals(3, result.get(2).order());
    }

    @Test
    void shouldPlaceCosmeticsWithoutOrderLast() throws IOException {
        // Arrange
        writeCosmetic(
            "first.yml",
            """
            material: FEATHER
            order: 1
            """
        );

        writeCosmetic(
            "noOrderAlpha.yml",
            """
            material: FEATHER
            """
        );

        writeCosmetic(
            "second.yml",
            """
            material: FEATHER
            order: 2
            """
        );

        writeCosmetic(
            "noOrderBeta.yml",
            """
            material: FEATHER
            """
        );

        // Act
        sut.reload();
        var result = sut.loadAll();

        // Assert
        assertEquals(4, result.size());
        assertEquals(1, result.get(0).order());
        assertEquals(2, result.get(1).order());
        assertEquals(Integer.MAX_VALUE, result.get(2).order());
        assertEquals(Integer.MAX_VALUE, result.get(3).order());
    }

    @Test
    void shouldDefaultOrderToMaxValue() throws IOException {
        // Arrange
        writeCosmetic(
            "plain.yml",
            """
            material: FEATHER
            """
        );

        // Act
        sut.reload();
        var result = sut.loadAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(Integer.MAX_VALUE, result.get(0).order());
    }

    private void writeCosmetic(String fileName, String content) throws IOException {
        Files.writeString(
            tempDir.resolve("cosmetics").resolve(fileName),
            content
        );
    }
}
