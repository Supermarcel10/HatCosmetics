package me.Tonus_.hatCosmetics.networking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;


public class ModrinthUpstreamAPIClient implements IUpstreamAPIClient {
	private static final String UPSTREAM_URL = "https://api.modrinth.com/v2/project/";

	private final Logger logger;
	private final String primaryURL;
	private JsonObject _projectData;

	public ModrinthUpstreamAPIClient(Logger logger, String projectID) {
		this.logger = logger;
		this.primaryURL = UPSTREAM_URL + projectID;
	}

	ModrinthUpstreamAPIClient(Logger logger, String projectID, String projectData) {
		this.logger = logger;
		this.primaryURL = UPSTREAM_URL + projectID;
		this._projectData = JsonParser.parseString(projectData).getAsJsonObject();
	}

	private @Nullable JsonObject getProjectData() {
		if (_projectData == null) {
			try {
				_projectData = makeRequest(primaryURL);
			} catch (IOException e) {
				return null;
			}
		}

		return _projectData;
	}

	/**
	 * Retrieves the latest version number of a resource from Modrinth.
	 *
	 * @param stableOnly If true, returns the latest stable version (non-beta, non-prerelease).
	 *                   If false, returns the absolute latest version.
	 * @return The latest version number of the resource, or null if unable to retrieve.
	 */
	public @Nullable String getLatestVersion(boolean stableOnly) {
		var projectData = getProjectData();
		if (projectData == null) return null;

		var versionIDs = projectData.getAsJsonArray("versions");
		if (versionIDs.isEmpty()) {
			logger.warn("No versions found for project.");
			return null;
		}

		for (JsonElement versionID : versionIDs.asList().reversed()) {
			if (versionID == null) continue;

			try {
				JsonObject versionData = makeRequest(primaryURL + "/version/" + versionID.getAsString());
				if (versionData == null) continue;

				String versionType = versionData.get("version_type").getAsString();
				if (stableOnly && !"release".equals(versionType)) continue;

				return versionData.get("version_number").getAsString();
			} catch (IOException e) {
				logger.warn("Failed to retrieve version details for ID {}. {}", versionID.getAsString(), e.getMessage());
			}
		}

		return null;
	}

	/**
	 * Retrieves the URL to the resource from Modrinth.
	 *
	 * @return The URL of the resource, or null if unable to retrieve.
	 */
	public @Nullable String getResourceURL() {
		var projectData = getProjectData();
		if (projectData == null) return null;

		var projectType = projectData.get("project_type");
		var slug = projectData.get("slug");

		if (projectType == null || slug == null) return null;

		return "https://modrinth.com/" + projectType.getAsString() + "/" + slug.getAsString();
	}

	/**
	 * Makes an HTTP GET request to the specified URL
	 *
	 * @param url The URL to send the request to.
	 * @return The response body as a JsonObject, or null if the request failed.
	 * @throws IOException If an I/O error occurs during the request.
	 */
	private @Nullable JsonObject makeRequest(String url) throws IOException {
		var connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
		connection.setRequestMethod("GET");

		var responseCode = connection.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			logger.warn("Failed to retrieve data. Response code {}", responseCode);
			return null;
		}

		return readResponse(connection);
	}

	/**
	 * Reads the response from an HTTP connection.
	 *
	 * @param connection The HttpURLConnection to read from.
	 * @return The response body as a JsonObject.
	 * @throws IOException If an I/O error occurs while reading the response.
	 */
	private static @NotNull JsonObject readResponse(HttpURLConnection connection) throws IOException {
		try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
