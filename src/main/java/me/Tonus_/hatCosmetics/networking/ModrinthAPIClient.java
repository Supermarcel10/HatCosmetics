package me.Tonus_.hatCosmetics.networking;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Logger;

public class ModrinthAPIClient implements IModrinthAPIClient {
    private final Logger logger;
    private final String primaryURL;

    private String response;
    private String latestVersionResponse;
    private String latestStableVersionResponse;

    ModrinthAPIClient(Logger logger, String projectID, String response) {
        this.logger = logger;
        this.primaryURL = "https://api.modrinth.com/v2/project/" + projectID;
    }

    public ModrinthAPIClient(Logger logger, String projectID) {
        this.logger = logger;
        this.primaryURL = "https://api.modrinth.com/v2/project/" + projectID;

        getJSONResponse();
    }

    /**
     * Retrieves the latest version number of a resource from Modrinth.
     *
     * @param stableOnly If true, returns the latest stable version (non-beta, non-prerelease).
     *                   If false, returns the absolute latest version.
     * @return The latest version number of the resource, or null if unable to retrieve.
     */
    public @Nullable String getLatestVersion(boolean stableOnly) {
        String versionResponse = stableOnly ? latestStableVersionResponse : latestVersionResponse;
        if (versionResponse == null || versionResponse.isEmpty()) return null;
        return extractValue(versionResponse, "\"version_number\":");
    }

    /**
     * Retrieves the URL to the resource from Modrinth.
     *
     * @return The URL of the resource, or null if unable to retrieve.
     */
    public @Nullable String getResourceURL() {
        if (response.isEmpty()) return null;

        var project_type = extractValue(response, "\"project_type\":");
        var slug = extractValue(response, "\"slug\":");

        return "https://modrinth.com/" + project_type + "/" + slug;
    }

    /**
     * Captures and caches Modrinth API's response
     */
    private void getJSONResponse() {
        try {
            String response = makeRequest(primaryURL);
            if (response == null) return;

            this.response = response;

            String versionID = extractValue(response, "\"versions\":");
            if (versionID == null) {
                logger.warning("Failed to extract version ID.");
                return;
            }

			String latestVersionID = extractLatestVersionID(versionID);
            if (latestVersionID != null) {
                this.latestVersionResponse = makeRequest(primaryURL + "/version/" + latestVersionID);
            }

            String latestStableVersionID = extractLatestStableVersionID(versionID);
            if (latestStableVersionID != null) {
                this.latestStableVersionResponse = makeRequest(primaryURL + "/version/" + latestStableVersionID);
            }
        } catch (IOException e) {
            logger.warning("Failed to retrieve response.");
        }
    }

    /**
     * Makes an HTTP GET request to the specified URL
     *
     * @param url The URL to send the request to.
     * @return The response body as a String, or null if the request failed.
     * @throws IOException If an I/O error occurs during the request.
     */
    private @Nullable String makeRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            logger.warning("Failed to retrieve data. Response code: " + responseCode);
            return null;
        }

        return readResponse(connection);
    }

    /**
     * Reads the response from an HTTP connection.
     *
     * @param connection The HttpURLConnection to read from.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs while reading the response.
     */
    private static @NotNull String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * Extracts a value from a JSON-like string based on a target key.
     *
     * @param input  The input string to search.
     * @param target The target key to search for.
     * @return The extracted value, or null if not found.
     */
    private static @Nullable String extractValue(@NotNull String input, String target) {
        int startIndex = input.indexOf(target);
        if (startIndex == -1) return null;

        int i = startIndex + target.length() + 1;
        char openingChar = input.charAt(i - 1);
        char closingChar = openingChar == '"' ? '"' : openingChar == '[' ? ']' : '\0';
        if (closingChar == '\0') return null;

        int endIndex = input.indexOf(closingChar, i);
        if (endIndex == -1) return null;

        return input.substring(i, endIndex);
    }

    /**
     * Extracts the latest version ID from a string containing version information.
     *
     * @param versionsString The string containing version information.
     * @return The latest version ID, or null if unable to extract.
     */
    private static String extractLatestVersionID(String versionsString) {
        if (versionsString == null) return null;

        int lastQuoteIndex = versionsString.lastIndexOf('"');
        if (lastQuoteIndex == -1) return null;

        int secondLastQuoteIndex = versionsString.lastIndexOf('"', lastQuoteIndex - 1);
        if (secondLastQuoteIndex == -1) return null;

        return versionsString.substring(secondLastQuoteIndex + 1, lastQuoteIndex);
    }

	/**
	 * Extracts the latest stable version ID from a string containing version information.
	 * This method fetches individual version details to check their version_type.
	 *
	 * @param versionsString The string containing version information.
	 * @return The latest stable version ID, or null if unable to extract.
	 */
	private String extractLatestStableVersionID(String versionsString) {
		if (versionsString == null) return null;

		String[] versionIDs = versionsString.split(",");

		for (int i = versionIDs.length - 1; i >= 0; i--) {
			String versionID = versionIDs[i].trim().replaceAll("\"", "");
			if (versionID.isEmpty()) continue;

			try {
				// Make request to get version details
				String versionResponse = makeRequest(primaryURL + "/version/" + versionID);
				if (versionResponse == null) continue;

				// Check release type
				String versionType = extractValue(versionResponse, "\"version_type\":");
				if ("release".equals(versionType)) return versionID;
			} catch (IOException e) {
				logger.warning("Failed to retrieve version details for ID: " + versionID);
			}
		}

		return null;
	}
}
