package me.Tonus_.hatCosmetics.networking;

import me.Tonus_.hatCosmetics.HatCosmetics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ModrinthAPIClient {
	/**
	 * Retrieves the latest version number of a plugin from Modrinth.
	 *
	 * @param projectID The ID of the project on Modrinth.
	 * @return The latest version number of the plugin, or null if unable to retrieve.
	 */
	public static @Nullable String getLatestPluginVersion(String projectID) {
		try {
			String response = makeRequest("https://api.modrinth.com/v2/project/" + projectID);
			if (response == null) return null;

			String versionID = extractValue(response, "\"versions\":");
			if (versionID == null) {
				HatCosmetics.getLog().warn("Failed to extract version ID.");
				return null;
			}

			response = makeRequest("https://api.modrinth.com/v2/project/" + projectID + "/version/" + extractLatestVersionID(versionID));
			if (response == null) return null;

			return extractValue(response, "\"version_number\":");
		} catch (IOException e) {
			HatCosmetics.getLog().warn("Failed to retrieve latest version.");
		}

		return null;
	}

	/**
	 * Makes an HTTP GET request to the specified URL
	 *
	 * @param url The URL to send the request to.
	 * @return The response body as a String, or null if the request failed.
	 * @throws IOException If an I/O error occurs during the request.
	 */
	private static @Nullable String makeRequest(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			HatCosmetics.getLog().warn("Failed to retrieve data. Response code: " + responseCode);
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
	 * @param input The input string to search.
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
}
