package me.Tonus_.hatCosmetics.updates;

import me.Tonus_.hatCosmetics.HatCosmetics;
import me.Tonus_.hatCosmetics.networking.ModrinthAPIClient;
import org.jetbrains.annotations.NotNull;


public class VersionChecker {
	public static void checkForUpdates(String projectID) {
		String remoteVersion = ModrinthAPIClient.getLatestPluginVersion(projectID);
		if (remoteVersion == null) {
			HatCosmetics.getLog().warn("Failed to check for updates.");
			return;
		}

		String pluginVersion = getPluginVersion();

		int versionComparison = compareVersions(pluginVersion, remoteVersion);
		if (versionComparison < 0) {
			HatCosmetics.getLog().warn("A new version is available: " + remoteVersion);
			HatCosmetics.getLog().warn("You are currently running: " + pluginVersion);
			HatCosmetics.getLog().warn("Update here: https://modrinth.com/plugin/hatcosmetics/version/latest");
		} else if (versionComparison > 0) {
			HatCosmetics.getLog().warn("You are running an unknown version!");
			HatCosmetics.getLog().warn("Ensure you download the plugin from trusted sources.");
		}
	}

	private static @NotNull String getPluginVersion() {
		return HatCosmetics.getInstance().getDescription().getVersion();
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
