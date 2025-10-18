package me.Tonus_.hatCosmetics.updates;

import lombok.AllArgsConstructor;
import me.Tonus_.hatCosmetics.networking.ModrinthAPIClient;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


@AllArgsConstructor
public class VersionChecker {
	private final Plugin plugin;
	private final String projectID;

	/**
	 * Checks for updates for the plugin
	 */
	public void checkForUpdates() {
		String remoteVersion = ModrinthAPIClient.getLatestPluginVersion(projectID);
		if (remoteVersion == null) {
			plugin.getSLF4JLogger().warn("Failed to check for updates.");
			return;
		}

		String pluginVersion = getPluginVersion();

		int versionComparison = compareVersions(pluginVersion, remoteVersion);
		if (versionComparison > 0) {
			plugin.getSLF4JLogger().warn("A new version is available: {}", remoteVersion);
			plugin.getSLF4JLogger().warn("You are currently running: {}", pluginVersion);
			plugin.getSLF4JLogger().warn("Update here: https://modrinth.com/plugin/hatcosmetics/version/latest");
		} else if (versionComparison < 0) {
			plugin.getSLF4JLogger().warn("You are running an unknown version!");
			plugin.getSLF4JLogger().warn("Ensure you download the plugin from trusted sources.");
		}
	}

	/**
	 * Retrieves the version of the plugin
	 * @return the version of the plugin
	 */
	private @NotNull String getPluginVersion() {
		return plugin.getDescription().getVersion();
	}

	/**
	 * Compares two versions
	 * @param local the local version
	 * @param remote the remote version
	 * @return -1 if unknown version, 0 if up to date, 1 if it needs updating
	 */
	private static int compareVersions(@NotNull String local, @NotNull String remote) {
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
