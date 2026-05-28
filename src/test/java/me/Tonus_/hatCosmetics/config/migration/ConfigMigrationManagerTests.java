package me.Tonus_.hatCosmetics.config.migration;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.mockito.Mockito.*;


class ConfigMigrationManagerTests {
    @TempDir
    Path tempDir;

    private Plugin plugin = mock(Plugin.class);
    private Logger logger = mock(Logger.class);

    @BeforeEach
    void setUp() {
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
    }

    @Test
    void shouldNotRunMigrationsWhenNoConfigFileExists() {
        // Arrange
        var migration = mock(IConfigMigration.class);
        when(migration.fromVersion()).thenReturn("4");
        when(migration.toVersion()).thenReturn("5");

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration, never()).run(any(), any());
    }

    @Test
    void shouldNotRunMigrationsWhenAlreadyAtTargetVersion() throws IOException {
        // Arrange
        writeConfig("version: 5");

        var migration = mock(IConfigMigration.class);
        when(migration.fromVersion()).thenReturn("4");
        when(migration.toVersion()).thenReturn("5");

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration, never()).run(any(), any());
    }

    @Test
    void shouldRunMigrationWhenVersionIs4() throws IOException {
        // Arrange
        writeConfig("version: 4\nitem: FEATHER");

        var migration = mock(IConfigMigration.class);
        when(migration.fromVersion()).thenReturn("4");
        when(migration.toVersion()).thenReturn("5");

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration).run(plugin, logger);
    }

    @Test
    void shouldDetectV4WhenVersionMissingButKeysPresent() throws IOException {
        // Arrange
        writeConfig("item: FEATHER\ngui_rows: 2");

        var migration = mock(IConfigMigration.class);
        when(migration.fromVersion()).thenReturn("4");
        when(migration.toVersion()).thenReturn("5");

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration).run(plugin, logger);
    }

    @Test
    void shouldDetectV4WhenHatsSectionPresent() throws IOException {
        // Arrange
        writeConfig("hats:\n  staffHat:\n    data: 1000101");

        var migration = mock(IConfigMigration.class);
        when(migration.fromVersion()).thenReturn("4");
        when(migration.toVersion()).thenReturn("5");

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration).run(plugin, logger);
    }

    @Test
    void shouldDetectVersionAsString() throws IOException {
        // Arrange
        writeConfig("version: \"4\"");

        var migration = mock(IConfigMigration.class);
        when(migration.fromVersion()).thenReturn("4");
        when(migration.toVersion()).thenReturn("5");

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration).run(plugin, logger);
    }

    @Test
    void shouldWarnWhenNoVersionDetectable() throws IOException {
        // Arrange
        writeConfig("some_unknown_key: true");

        var migration = mock(IConfigMigration.class);

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration, never()).run(any(), any());
        verify(logger).warn("Could not detect config version. Skipping migration.");
    }

    @Test
    void shouldChainMultipleMigrations() throws IOException {
        // Arrange
        writeConfig("version: 4\nitem: FEATHER");

        var migration4to5 = mock(IConfigMigration.class);
        when(migration4to5.fromVersion()).thenReturn("4");
        when(migration4to5.toVersion()).thenReturn("5");

        var migration5to6 = mock(IConfigMigration.class);
        when(migration5to6.fromVersion()).thenReturn("5");
        when(migration5to6.toVersion()).thenReturn("6");

        // Act
        new ConfigMigrationManager(
            plugin,
            logger,
            "6",
            List.of(migration4to5, migration5to6)
        ).runEligibleMigrations();

        // Assert
        var inOrder = inOrder(migration4to5, migration5to6);
        inOrder.verify(migration4to5).run(plugin, logger);
        inOrder.verify(migration5to6).run(plugin, logger);
    }

    @Test
    void shouldWarnWhenNoMigrationFoundForVersion() throws IOException {
        // Arrange
        writeConfig("version: 3\nitem: FEATHER");

        var migration = mock(IConfigMigration.class);
        when(migration.fromVersion()).thenReturn("4");
        when(migration.toVersion()).thenReturn("5");

        // Act
        new ConfigMigrationManager(plugin, logger, "5", List.of(migration)).runEligibleMigrations();

        // Assert
        verify(migration, never()).run(any(), any());
        verify(logger).warn("No migration found from config version {}.", "3");
    }

    private void writeConfig(String content) throws IOException {
        Files.writeString(tempDir.resolve("config.yml"), content.stripIndent());
    }
}
