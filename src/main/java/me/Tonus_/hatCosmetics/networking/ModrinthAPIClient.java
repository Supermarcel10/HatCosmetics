package me.Tonus_.hatCosmetics.networking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Logger;


public class ModrinthAPIClient implements IModrinthAPIClient {
    private final Logger logger;
    private final String primaryURL;
    private final JsonObject projectData;

    ModrinthAPIClient(Logger logger, String projectID, String response) {
        this.logger = logger;
        this.primaryURL = "https://api.modrinth.com/v2/project/" + projectID;

        this.projectData = JsonParser.parseString(response).getAsJsonObject();
    }

    public ModrinthAPIClient(Logger logger, String projectID) {
        this.logger = logger;
        this.primaryURL = "https://api.modrinth.com/v2/project/" + projectID;

        // Parse JSON data
        try {
            this.projectData = makeRequest(primaryURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the latest version number of a resource from Modrinth.
     *
     * @param stableOnly If true, returns the latest stable version (non-beta, non-prerelease).
     *                   If false, returns the absolute latest version.
     * @return The latest version number of the resource, or null if unable to retrieve.
     */
    public @Nullable String getLatestVersion(boolean stableOnly) {
        JsonArray versionIDs = projectData.getAsJsonArray("versions");
        if (versionIDs.isEmpty()) {
            logger.warning("No versions found for project.");
            return null;
        }

        for (JsonElement versionID : versionIDs) {
            if (versionID == null) continue;

            try {
                JsonObject versionData = makeRequest(primaryURL + "/version/" + versionID.getAsString());
                if (versionData == null) continue;

                String versionType = versionData.get("version_type").getAsString();
                if (stableOnly && !"release".equals(versionType)) continue;

                return versionData.get("version_number").getAsString();
            } catch (IOException e) {
                logger.warning("Failed to retrieve version details for ID: " + versionID.getAsString() + " - " + e.getMessage());
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
        if (projectData == null) return null;

        JsonElement projectType = projectData.get("project_type");
        JsonElement slug = projectData.get("slug");

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
     * @return The response body as a JsonObject.
     * @throws IOException If an I/O error occurs while reading the response.
     */
    private static @NotNull JsonObject readResponse(HttpURLConnection connection) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
