package me.Tonus_.hatCosmetics.updates;

import me.Tonus_.hatCosmetics.HatCosmetics;
import me.Tonus_.hatCosmetics.networking.ModrinthAPIClient;
import org.jetbrains.annotations.NotNull;


public class VersionChecker {
	/**
	 * Checks for updates for the plugin
	 * @param projectID the Modrinth project ID of the plugin
	 */
	public static void checkForUpdates(String projectID) {
		String remoteVersion = ModrinthAPIClient.getLatestPluginVersion(projectID);
		if (remoteVersion == null) {
			HatCosmetics.getLog().warn("Failed to check for updates.");
			return;
		}

		String pluginVersion = getPluginVersion();

		int versionComparison = compareVersions(pluginVersion, remoteVersion);
		if (versionComparison > 0) {
			HatCosmetics.getLog().warn("A new version is available: {}", remoteVersion);
			HatCosmetics.getLog().warn("You are currently running: {}", pluginVersion);
			HatCosmetics.getLog().warn("Update here: https://modrinth.com/plugin/hatcosmetics/version/latest");
		} else if (versionComparison < 0) {
			HatCosmetics.getLog().warn("You are running an unknown version!");
			HatCosmetics.getLog().warn("Ensure you download the plugin from trusted sources.");
		}
	}

	/**
	 * Retrieves the version of the plugin
	 * @return the version of the plugin
	 */
	private static @NotNull String getPluginVersion() {
		return HatCosmetics.getInstance().getDescription().getVersion();
	}

	/**
	 * Compares two versions
	 * @param local the local version
	 * @param remote the remote version
	 * @return -1 if unknown version, 0 if up to date, 1 if it needs updating
	 */
	protected static int compareVersions(@NotNull String local, @NotNull String remote) {
		String[] localParts = local.split("\\.");
		String[] remoteParts = remote.split("\\.");

		int length = Math.max(localParts.length, remoteParts.length);

		for (int i = 0; i < length; i++) {
			int localPart = i < localParts.length ? Integer.parseInt(localParts[i]) : 0;
			int remotePart = i < remoteParts.length ? Integer.parseInt(remoteParts[i]) : 0;

			if (localPart < remotePart) return 1;
			else if (localPart > remotePart) return -1;
		}

		return 0;
	}
}
