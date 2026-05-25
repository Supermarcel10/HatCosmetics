package me.Tonus_.hatCosmetics.networking;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ModrinthUpstreamAPIClientTests {
    private final Logger logger = mock();
    private final HttpURLConnection connection = mock();
    private final URI uri = mock();

    private static final String TEST_PROJECT_ID = "test-project-id";
    private static final String BASE_URL = "https://api.modrinth.com/v2/project/" + TEST_PROJECT_ID;

    private static final String SAMPLE_PROJECT_RESPONSE = """
            {
                "id": "test-project-id",
                "slug": "test-mod",
                "project_type": "mod",
                "versions":["version-id-1", "version-id-2", "version-id-3"]
            }
            """;

    private static final String SAMPLE_BETA_VERSION_RESPONSE = """
            {
                "id": "version-id-3",
                "version_number": "1.3.0-beta",
                "version_type": "beta"
            }
            """;

    ModrinthUpstreamAPIClientTests() throws IOException {
        URL url = mock();
        doReturn(connection).when(url).openConnection();
        doReturn(url).when(uri).toURL();
    }

    @Test
    void getResourceURL_whenValidProjectData_shouldReturnCorrectURL() {
        // Arrange
        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, SAMPLE_PROJECT_RESPONSE);

        // Act
        var result = sut.getResourceURL();

        // Assert
        assertEquals("https://modrinth.com/mod/test-mod", result);
    }

    @Test
    void getResourceURL_whenProjectDataIsNull_shouldReturnNull() {
        // Arrange
        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, "{}");

        // Act
        var resourceURL = sut.getResourceURL();

        // Assert
        assertNull(resourceURL);
    }

    @Test
    void getResourceURL_whenProjectTypeOrSlugIsMissing_shouldReturnNull() {
        // Arrange
        var incompleteResponse = """
                {
                    "id": "test-project-id",
                    "project_type": "mod"
                }
                """;

        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, incompleteResponse);

        // Act
        var resourceURL = sut.getResourceURL();

        // Assert
        assertNull(resourceURL);
    }

    @Test
    void getLatestVersion_whenStableOnlyTrue_shouldReturnLatestStableVersion() throws IOException {
        // Arrange
        var projectResponse = """
            {
                "id": "test-project-id",
                "slug": "test-mod",
                "project_type": "mod",
                "versions":["version-id-1", "version-id-2"]
            }
            """;

        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, projectResponse);

        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            // Create distinct URIs for each version
            URI uri2 = mock();
            URI uri1 = mock();

            // Map specific URLs to their corresponding URIs
            uriMock.when(() -> URI.create(BASE_URL + "/version/version-id-2")).thenReturn(uri2);
            uriMock.when(() -> URI.create(BASE_URL + "/version/version-id-1")).thenReturn(uri1);

            // Create distinct URLs for each URI
            URL url2 = mock();
            URL url1 = mock();
            when(uri2.toURL()).thenReturn(url2);
            when(uri1.toURL()).thenReturn(url1);

            // Create distinct connections for each URL
            HttpURLConnection connection2 = mock();
            HttpURLConnection connection1 = mock();
            when(url2.openConnection()).thenReturn(connection2);
            when(url1.openConnection()).thenReturn(connection1);

            // Set up responses
            setupMockResponse(connection2, SAMPLE_BETA_VERSION_RESPONSE);
            setupMockResponse(connection1, """
                {
                    "id": "version-id-2",
                    "version_number": "1.2.3",
                    "version_type": "release"
                }
                """);

            // Act
            var latestVersion = sut.getLatestVersion(true);

            // Assert
            assertEquals("1.2.3", latestVersion);

            verify(connection2).getResponseCode();
            verify(connection2).getInputStream();
            verify(connection1).getResponseCode();
            verify(connection1).getInputStream();
        }
    }

    @Test
    void getLatestVersion_whenOnlyBetaVersionsExistAndStableOnlyTrue_shouldReturnNull() throws IOException {
        // Arrange
        var projectResponse = """
                {
                    "id": "test-project-id",
                    "slug": "test-mod",
                    "project_type": "mod",
                    "versions":["version-id-3"]
                }
                """;

        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, projectResponse);

        setupMockResponse(connection, SAMPLE_BETA_VERSION_RESPONSE);

        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            uriMock.when(() -> URI.create(anyString())).thenReturn(uri);

            // Act
            String latestVersion = sut.getLatestVersion(true);

            // Assert
            assertNull(latestVersion);
        }
    }

    @Test
    void getLatestVersion_whenStableOnlyFalse_shouldReturnAbsoluteLatestVersion() throws IOException {
        // Arrange
        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, SAMPLE_PROJECT_RESPONSE);

        setupMockResponse(connection, SAMPLE_BETA_VERSION_RESPONSE);

        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            uriMock.when(() -> URI.create(BASE_URL + "/version/version-id-3")).thenReturn(uri);

            // Act
            String latestVersion = sut.getLatestVersion(false);

            // Assert
            assertEquals("1.3.0-beta", latestVersion);
        }
    }

    @Test
    void getLatestVersion_whenNoVersionsExist_shouldReturnNull() {
        // Arrange
        var response = """
                {
                    "id": "test-project-id",
                    "slug": "test-mod",
                    "project_type": "mod",
                    "versions":[]
                }
                """;

        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, response);

        // Act
        var latestVersion = sut.getLatestVersion(true);

        // Assert
        assertNull(latestVersion);
        verify(logger).warn("No versions found for project.");
    }

    @Test
    void getLatestVersion_whenVersionRequestFails_shouldLogWarningAndContinue() throws IOException {
        // Arrange
        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, SAMPLE_PROJECT_RESPONSE);

        doReturn(HttpURLConnection.HTTP_NOT_FOUND).when(connection).getResponseCode();

        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            uriMock.when(() -> URI.create(BASE_URL + "/version/version-id-1")).thenReturn(uri);
            uriMock.when(() -> URI.create(BASE_URL + "/version/version-id-2")).thenReturn(uri);
            uriMock.when(() -> URI.create(BASE_URL + "/version/version-id-3")).thenReturn(uri);

            // Act
            String latestVersion = sut.getLatestVersion(true);

            // Assert
            assertNull(latestVersion);
            verify(logger, atLeastOnce()).warn("Failed to retrieve data. Response code {}", 404);
            verify(connection, atLeastOnce()).disconnect();
        }
    }

    @Test
    void getLatestVersion_whenIOExceptionOccurs_shouldLogWarningAndContinue() throws IOException {
        // Arrange
        var singleVersionProjectResponse = """
            {
                "id": "test-project-id",
                "slug": "test-mod",
                "project_type": "mod",
                "versions":["version-id-1"]
            }
            """;

        var sut = new ModrinthUpstreamAPIClient(logger, TEST_PROJECT_ID, singleVersionProjectResponse);

        doThrow(new IOException("Network Error")).when(connection).getResponseCode();

        try (MockedStatic<URI> uriMock = mockStatic(URI.class)) {
            uriMock.when(() -> URI.create(BASE_URL + "/version/version-id-1")).thenReturn(uri);

            // Act
            String latestVersion = sut.getLatestVersion(true);

            // Assert
            assertNull(latestVersion);
            verify(logger).warn("Failed to retrieve version details for ID {}. {}", "version-id-1", "Network Error");
            verify(connection).disconnect();
        }
    }

    private void setupMockResponse(@NotNull HttpURLConnection connection, @NotNull String response) throws IOException {
        doReturn(HttpURLConnection.HTTP_OK).when(connection).getResponseCode();
        InputStream inputStream = new ByteArrayInputStream(response.getBytes());
        doReturn(inputStream).when(connection).getInputStream();
    }
}