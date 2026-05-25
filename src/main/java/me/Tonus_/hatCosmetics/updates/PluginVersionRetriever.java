package me.Tonus_.hatCosmetics.updates;

import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;


@AllArgsConstructor
public class PluginVersionRetriever implements IPluginVersionRetriever {
    private final Plugin plugin;

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
