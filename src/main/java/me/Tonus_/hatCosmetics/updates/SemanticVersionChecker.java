package me.Tonus_.hatCosmetics.updates;

import lombok.AllArgsConstructor;
import me.Tonus_.hatCosmetics.networking.IUpstreamAPIClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;


@AllArgsConstructor
public class SemanticVersionChecker implements IVersionChecker {
	private final Logger logger;
	private final IUpstreamAPIClient apiClient;
	private final IPluginVersionRetriever pluginVersionRetriever;
	private final boolean isStableRelease;

	public void checkForUpdates() {
		String remoteVersion = apiClient.getLatestVersion(isStableRelease);
		if (remoteVersion == null) {
			logger.warn("Failed to check for updates.");
			return;
		}

		String url = apiClient.getResourceURL();
		if (url == null) {
			logger.warn("Failed to retrieve resource URL.");
		}

		var currentVersion = pluginVersionRetriever.getVersion();

		int versionComparison = compareVersions(currentVersion, remoteVersion);
		if (versionComparison < 0) {
            logger.warn("A new version is available: {}", remoteVersion);
            logger.warn("You are currently running: {}", currentVersion);

			if (url != null) logger.warn("Update here: {}/version/latest", url);
		} else if (versionComparison > 0) {
			logger.warn("You are running an unknown or unsupported version!");
			logger.warn("Ensure you download the plugin from trusted sources.");
		}
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