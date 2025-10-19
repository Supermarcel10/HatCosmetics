package me.Tonus_.hatCosmetics.updates;

import me.Tonus_.hatCosmetics.networking.IUpstreamAPIClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;


public class SemanticVersionCheckerTests {
    private final Logger logger = mock();
    private final IUpstreamAPIClient apiClient = mock();
    private final IPluginVersionRetriever pluginVersionRetriever = mock();
    private final Boolean isStableRelease = true;
    private final SemanticVersionChecker sut = new SemanticVersionChecker(logger, apiClient, pluginVersionRetriever, isStableRelease);

    @Test
    void checkForUpdates_whenRemoteVersionFailedToRetrieve_shouldLogWarning() {
        // Arrange
        doReturn(null).when(apiClient).getLatestVersion(isStableRelease);

        // Act
        sut.checkForUpdates();

        // Assert
        verify(logger).warn("Failed to check for updates.");
        verify(logger).warn(any());
    }

    @Test
    void checkForUpdates_whenUpToDate_shouldNotLogAnything() {
        // Arrange
        var remoteVersion = "1.0.0";
        doReturn(remoteVersion).when(apiClient).getLatestVersion(isStableRelease);

        var currentVersion = "1.0.0";
        doReturn(currentVersion).when(pluginVersionRetriever).getVersion();

        // Act
        sut.checkForUpdates();

        // Assert
        verify(logger, times(0)).warn(any());
    }

    @Test
    void checkForUpdates_whenVersionNewer_shouldLogWarning() {
        // Arrange
        var remoteVersion = "1.0.0";
        doReturn(remoteVersion).when(apiClient).getLatestVersion(isStableRelease);

        var currentVersion = "2.0.0";
        doReturn(currentVersion).when(pluginVersionRetriever).getVersion();

        // Act
        sut.checkForUpdates();

        // Assert
        verify(logger).warn("You are running an unknown or unsupported version!");
        verify(logger).warn("Ensure you download the plugin from trusted sources.");
    }

    @Test
    void checkForUpdates_whenVersionOlderAndFailedToGetURL_shouldLogWarnings() {
        // Arrange
        var remoteVersion = "2.0.0";
        doReturn(remoteVersion).when(apiClient).getLatestVersion(isStableRelease);

        var currentVersion = "1.0.0";
        doReturn(currentVersion).when(pluginVersionRetriever).getVersion();

        // Act
        sut.checkForUpdates();

        // Assert
        verify(logger).warn("A new version is available: {}", remoteVersion);
        verify(logger).warn("You are currently running: {}", currentVersion);
        verify(logger).warn("Failed to retrieve resource URL.");
    }

    @Test
    void checkForUpdates_whenVersionOlderAndURLRetrieved_shouldLogWarnings() {
        // Arrange
        var remoteVersion = "2.0.0";
        doReturn(remoteVersion).when(apiClient).getLatestVersion(isStableRelease);

        var currentVersion = "1.0.0";
        doReturn(currentVersion).when(pluginVersionRetriever).getVersion();

        var url = "SOME_URL";
        doReturn(url).when(apiClient).getResourceURL();

        // Act
        sut.checkForUpdates();

        // Assert
        verify(logger).warn("A new version is available: {}", remoteVersion);
        verify(logger).warn("You are currently running: {}", currentVersion);
        verify(logger).warn("Update here: {}/version/latest", url);
    }
}
