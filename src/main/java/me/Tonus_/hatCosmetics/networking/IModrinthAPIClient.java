package me.Tonus_.hatCosmetics.networking;

import javax.annotation.Nullable;

public interface IModrinthAPIClient {
    /**
     * Retrieves the latest version number of a resource from Modrinth.
     *
     * @param stableOnly If true, returns the latest stable version (non-beta, non-prerelease).
     *                   If false, returns the absolute latest version.
     * @return The latest version number of the resource, or null if unable to retrieve.
     */
    @Nullable String getLatestVersion(boolean stableOnly);

    /**
     * Retrieves the URL to the resource from Modrinth.
     *
     * @return The URL of the resource, or null if unable to retrieve.
     */
    @Nullable String getResourceURL();
}
