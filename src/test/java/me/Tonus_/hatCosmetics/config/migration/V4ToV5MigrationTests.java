package me.Tonus_.hatCosmetics.config.migration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

class V4ToV5MigrationTests {

    @TempDir
    Path tempDir;

    private Plugin plugin = mock(Plugin.class);
    private Logger logger = mock(Logger.class);
    private V4ToV5Migration sut = new V4ToV5Migration();

    @BeforeEach
    void setUp() {
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getResource("config.yml")).thenAnswer(inv ->
            loadTemplate()
        );
    }

    @Test
    void shouldMigrateFullConfigWithAllFields() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            item: FEATHER
            border: LIGHT_BLUE_STAINED_GLASS_PANE
            gui_rows: 2
            hide_hats: true
            hats:
              staffHat:
                data: 1000101
                name: "&fStaff Hat"
                description:
                  - "&7A fancy hat"
              coolHat:
                item: DIAMOND_HOE
                data: 2000202
                name: "&bCool Hat"
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        assertTrue(Files.exists(tempDir.resolve("config.yml.bak.v4")));

        var newConfig = YamlConfiguration.loadConfiguration(
            tempDir.resolve("config.yml").toFile()
        );

        assertEquals(5, newConfig.getInt("version"));
        assertEquals(2, newConfig.getInt("gui.rows"));
        assertEquals("LIGHT_BLUE_STAINED_GLASS_PANE", newConfig.getString("gui.items.border"));
        assertTrue(newConfig.getBoolean("gui.hide-hats"));

        var staffHat = YamlConfiguration.loadConfiguration(
            tempDir.resolve("cosmetics/staffHat.yml").toFile()
        );

        assertEquals("FEATHER", staffHat.getString("material"));
        assertEquals("1000101", staffHat.getString("custom-model-data"));
        assertNull(staffHat.get("permission"));
        assertEquals("&fStaff Hat", staffHat.getString("display.en_US.name"));
        assertEquals(List.of("&7A fancy hat"), staffHat.getStringList("display.en_US.description"));

        var coolHat = YamlConfiguration.loadConfiguration(
            tempDir.resolve("cosmetics/coolHat.yml").toFile()
        );

        assertEquals("DIAMOND_HOE", coolHat.getString("material"));
        assertEquals("2000202", coolHat.getString("custom-model-data"));
        assertEquals("&bCool Hat", coolHat.getString("display.en_US.name"));
    }

    @Test
    void shouldInheritGlobalItemWhenHatHasNoItemOverride() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            item: NETHER_STAR
            hats:
              glowHat:
                data: 3000303
                name: "&eGlow Hat"
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        var cosmetic = YamlConfiguration.loadConfiguration(
            tempDir.resolve("cosmetics/glowHat.yml").toFile()
        );

        assertEquals("NETHER_STAR", cosmetic.getString("material"));
    }

    @Test
    void shouldFallbackToFeatherWhenGlobalItemMissing() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            hats:
              simpleHat:
                data: 4000404
                name: "&aSimple Hat"
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        var cosmetic = YamlConfiguration.loadConfiguration(
            tempDir.resolve("cosmetics/simpleHat.yml").toFile()
        );

        assertEquals("FEATHER", cosmetic.getString("material"));
    }

    @Test
    void shouldCreateDisabledCosmeticWhenDataMissing() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            item: FEATHER
            hats:
              brokenHat:
                name: "&cBroken Hat"
                description:
                  - "&8No model data"
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        var disabledFile = tempDir.resolve("cosmetics/brokenHat.yml.disabled");
        assertTrue(Files.exists(disabledFile));

        var content = Files.readString(disabledFile);

        assertTrue(content.contains("This cosmetic was disabled during migration"));
        assertTrue(content.contains("&cBroken Hat"));

        var disabledConfig = YamlConfiguration.loadConfiguration(
            disabledFile.toFile()
        );

        assertEquals("0", disabledConfig.getString("custom-model-data"));
        assertEquals("&cBroken Hat", disabledConfig.getString("display.en_US.name"));
        assertEquals(List.of("&8No model data"), disabledConfig.getStringList("display.en_US.description"));

        var regularFile = tempDir.resolve("cosmetics/brokenHat.yml");
        assertFalse(Files.exists(regularFile));

        verify(logger).warn(
            eq("Disabled cosmetic '{}' saved as {} - no custom model data was defined."),
            eq("brokenHat"),
            anyString()
        );
    }

    @Test
    void shouldUseHatIdAsNameWhenNameMissing() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            item: FEATHER
            hats:
              unnamed:
                data: 5000505
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        var cosmetic = YamlConfiguration.loadConfiguration(
            tempDir.resolve("cosmetics/unnamed.yml").toFile()
        );

        assertEquals("unnamed", cosmetic.getString("display.en_US.name"));
    }

    @Test
    void shouldSkipExistingCosmeticFile() throws IOException {
        // Arrange
        Files.createDirectories(tempDir.resolve("cosmetics"));
        var existingContent = "material: STONE\ncustom-model-data: \"999\"\n";
        Files.writeString(
            tempDir.resolve("cosmetics/staffHat.yml"),
            existingContent
        );

        writeV4Config(
            """
            version: 4
            item: FEATHER
            hats:
              staffHat:
                data: 1000101
                name: "&fStaff Hat"
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        var content = Files.readString(
            tempDir.resolve("cosmetics/staffHat.yml")
        );

        assertEquals(existingContent, content);

        verify(logger).warn(
            eq("Cosmetic '{}' already exists at {}, skipping migration."),
            eq("staffHat"),
            anyString()
        );
    }

    @Test
    void shouldNumberBackupsWhenPreviousBackupExists() throws IOException {
        // Arrange
        Files.writeString(tempDir.resolve("config.yml.bak.v4"), "old-backup");

        writeV4Config(
            """
            version: 4
            item: FEATHER
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        assertTrue(Files.exists(tempDir.resolve("config.yml.bak.v4")));
        assertTrue(Files.exists(tempDir.resolve("config.yml.bak.v4_1")));
    }

    @Test
    void shouldPreserveGuiRowsMinusOne() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            item: FEATHER
            gui_rows: -1
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        var newConfig = YamlConfiguration.loadConfiguration(
            tempDir.resolve("config.yml").toFile()
        );

        assertEquals(-1, newConfig.getInt("gui.rows"));
    }

    @Test
    void shouldPreserveHideHatsFalse() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            item: FEATHER
            hide_hats: false
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        var newConfig = YamlConfiguration.loadConfiguration(
            tempDir.resolve("config.yml").toFile()
        );

        assertFalse(newConfig.getBoolean("gui.hide-hats"));
    }

    @Test
    void shouldHandleEmptyHatsSection() throws IOException {
        // Arrange
        writeV4Config(
            """
            version: 4
            item: FEATHER
            hats:
            """
        );

        // Act
        sut.run(plugin, logger);

        // Assert
        assertTrue(Files.exists(tempDir.resolve("config.yml.bak.v4")));
        var newConfig = YamlConfiguration.loadConfiguration(
            tempDir.resolve("config.yml").toFile()
        );

        assertEquals(5, newConfig.getInt("version"));
    }

    private void writeV4Config(String content) throws IOException {
        Files.writeString(tempDir.resolve("config.yml"), content.stripIndent());
    }

    private InputStream loadTemplate() {
        return getClass()
            .getClassLoader()
            .getResourceAsStream("config-v5-template.yml");
    }
}
