package me.Tonus_.hatCosmetics.config.migration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import lombok.RequiredArgsConstructor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class ConfigMigrationManager {
    private final Plugin plugin;
    private final Logger logger;
    private final String targetVersion;
    private List<IConfigMigration> migrations;

    @TestOnly
    ConfigMigrationManager(
        @NotNull Plugin plugin,
        @NotNull Logger logger,
        @NotNull String targetVersion,
        @NotNull List<IConfigMigration> migrations
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.targetVersion = targetVersion;
        this.migrations = migrations;
    }

    public void runEligibleMigrations() {
        var configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }

        var config = YamlConfiguration.loadConfiguration(configFile);
        var currentVersion = detectVersion(config);
        if (currentVersion == null) {
            logger.warn("Could not detect config version. Skipping migration.");
            return;
        }

        if (currentVersion.equals(targetVersion)) {
            return;
        }

        var chain = buildChain(currentVersion);
        if (chain.isEmpty()) {
            return;
        }

        logger.info("Running config migrations: v{} -> v{}", currentVersion, targetVersion);

        for (var migration : chain) {
            logger.info("Migrating config from v{} to v{} ...", migration.fromVersion(), migration.toVersion());

            migration.run(plugin, logger);

            logger.info("Successfully migrated config to v{}.", migration.toVersion());
        }
    }

    @Nullable
    private String detectVersion(@NotNull YamlConfiguration config) {
        var versionObj = config.get("version");
        if (versionObj instanceof String s) {
            return s;
        }

        if (versionObj instanceof Number n) {
            return String.valueOf(n.intValue());
        }

        if (config.contains("item") || config.contains("gui_rows") || config.isConfigurationSection("hats")) {
            return "4";
        }

        return null;
    }

    private List<IConfigMigration> buildChain(@NotNull String currentVersion) {
        var chain = new ArrayList<IConfigMigration>();
        var current = currentVersion;

        while (!current.equals(targetVersion)) {
            var migration = findMigration(current);
            if (migration == null) {
                logger.warn("No migration found from config version {}.", current);
                return List.of();
            }

            chain.add(migration);
            current = migration.toVersion();
        }

        return chain;
    }

    private IConfigMigration findMigration(@NotNull String fromVersion) {
        if (migrations == null) {
            migrations = createDefaultMigrations();
        }

        for (var migration : migrations) {
            if (migration.fromVersion().equals(fromVersion)) {
                return migration;
            }
        }

        return null;
    }

    private static List<IConfigMigration> createDefaultMigrations() {
        return List.of(new V4ToV5Migration());
    }
}
