package me.Tonus_.hatCosmetics.config.migration;

import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;


public interface IConfigMigration {
    String fromVersion();
    String toVersion();
    void run(Plugin plugin, Logger logger);
}
