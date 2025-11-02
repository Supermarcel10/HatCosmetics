package me.Tonus_.hatCosmetics.message.generics;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class GenericsRetriever implements IGenericsRetriever {
    private static final String GENERICS_FILE = "messages/generics.yml";

    private final Map<String, String> generics = new HashMap<>();
    private final Logger logger;

    public GenericsRetriever(Logger logger) {
        this.logger = logger;
        loadGenerics();
    }

    public @Nullable String getGeneric(@NotNull String key) {
        var value = generics.get(key);
        if (value == null) logger.warn("Could not retrieve generic with key '{}'.", key);
        return value;
    }

    /**
     * Loads all generic messages
     */
    private void loadGenerics() {
        try (var inputStream = GenericsRetriever.class.getClassLoader().getResourceAsStream(GENERICS_FILE)) {
            if (inputStream != null) {
                var isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                var yaml = YamlConfiguration.loadConfiguration(isr);

                for (var entry : yaml.getValues(true).entrySet()) {
                    generics.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            } else throw new IOException("Failed to open input stream!");
        } catch (IOException e) {
            logger.error("Failed to load generic messages! {}", e.toString());
        }

        logger.info("Loaded {} generic messages.", generics.size());
    }
}
