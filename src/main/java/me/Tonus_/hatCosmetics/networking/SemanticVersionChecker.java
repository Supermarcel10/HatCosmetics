package me.Tonus_.hatCosmetics.networking;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.logging.Logger;

public class SemanticVersionChecker implements IVersionChecker {
    private final Plugin plugin;
    private final Logger logger;
    private final IModrinthAPIClient apiClient;
	private final boolean isStableRelease;

    public SemanticVersionChecker(
            @NotNull Plugin plugin,
            @NotNull IModrinthAPIClient apiClient,
            boolean isStableRelease
    ) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.apiClient = apiClient;
		this.isStableRelease = isStableRelease;
    }

	public void checkForUpdates() {
        String remoteVersion = apiClient.getLatestVersion(isStableRelease);
        if (remoteVersion == null) {
            logger.warning("Failed to check for updates.");
            return;
        }

        String url = apiClient.getResourceURL();
        if (url == null) {
            logger.warning("Failed to retrieve resource URL.");
        }

        String version = getVersion();

        int versionComparison = compareVersions(version, remoteVersion);
        if (versionComparison < 0) {
            logger.warning("A new version is available: " + remoteVersion);
            logger.warning("You are currently running: " + version);

            if (url != null) logger.warning("Update here: " + url + "/version/latest");
        } else if (versionComparison > 0) {
            logger.warning("You are running an unknown or unsupported version!");
            logger.warning("Ensure you download the plugin from trusted sources.");
        }
    }

    private @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    private static int compareVersions(@NotNull String v1, @NotNull String v2) {
        String[] v1Parts = v1.split("\\.");
        String[] v2Parts = v2.split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

            if (v1Part < v2Part) return -1;
            else if (v1Part > v2Part) return 1;
        }

        return 0;
    }
}
