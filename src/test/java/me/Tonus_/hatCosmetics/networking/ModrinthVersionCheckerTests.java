package me.Tonus_.hatCosmetics.networking;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;
import static org.mockito.Mockito.*;


public class ModrinthVersionCheckerTests {
	private Plugin plugin;
    private Logger logger;
    private ModrinthAPIClient apiClient;

    @BeforeEach
    void setUp() {
        plugin = mock();
        logger = mock();
        when(plugin.getLogger()).thenReturn(logger);

        PluginDescriptionFile pluginDescription = mock();
        doReturn(pluginDescription).when(plugin).getDescription();
        doReturn("1.0.0").when(pluginDescription).getVersion();

        apiClient = spy(new ModrinthAPIClient(logger, "", ""));
    }

	@Test
	public void checkForUpdates_whenFailedToGetLatest_shouldShowAdequateWarning() {
		// Arrange
		var sut = new SemanticVersionChecker(plugin, apiClient, false);

		// Act
		sut.checkForUpdates();

		// Assert
		verify(logger, times(1)).warning("Failed to check for updates.");
	}

	@Test
    public void checkForUpdates_whenNoRelease_shouldNotShowAnything() {
		// Arrange
		var isStableRelease = false;

		doReturn("1.0.0").when(apiClient).getLatestVersion(isStableRelease);
        doReturn("resourceUrl").when(apiClient).getResourceURL();

		var sut = new SemanticVersionChecker(plugin, apiClient, isStableRelease);

		// Act
		sut.checkForUpdates();

		// Assert
		verify(logger, times(0)).warning(any(String.class));
    }

    @Test
    public void checkForUpdates_whenRelease_shouldShowUpdateWarningPrompt() {
		// Arrange
		var isStableRelease = false;

		doReturn("1.0.1").when(apiClient).getLatestVersion(isStableRelease);
        doReturn("resourceUrl").when(apiClient).getResourceURL();

		var sut = new SemanticVersionChecker(plugin, apiClient, isStableRelease);

		// Act
		sut.checkForUpdates();

		// Assert
		verify(logger).warning("A new version is available: 1.0.1");
        verify(logger).warning("You are currently running: 1.0.0");
        verify(logger).warning("Update here: resourceUrl/version/latest");
    }

    @Test
    public void checkForUpdates_whenUnknownVersion_shouldShowWarningPrompt() {
		// Arrange
		var isStableRelease = false;

		doReturn("0.9.0").when(apiClient).getLatestVersion(isStableRelease);
        doReturn("resourceUrl").when(apiClient).getResourceURL();

		var sut = new SemanticVersionChecker(plugin, apiClient, isStableRelease);

		// Act
		sut.checkForUpdates();

		// Assert
		verify(logger).warning("You are running an unknown or unsupported version!");
        verify(logger).warning("Ensure you download the plugin from trusted sources.");
    }
}
