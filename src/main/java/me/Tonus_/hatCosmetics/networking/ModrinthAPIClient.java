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

	private static String extractLatestVersionID(String versionsString) {
		if (versionsString == null) return null;

		int lastQuoteIndex = versionsString.lastIndexOf('"');
		if (lastQuoteIndex == -1) return null;

		int secondLastQuoteIndex = versionsString.lastIndexOf('"', lastQuoteIndex - 1);
		if (secondLastQuoteIndex == -1) return null;

		return versionsString.substring(secondLastQuoteIndex + 1, lastQuoteIndex);
	}
}
