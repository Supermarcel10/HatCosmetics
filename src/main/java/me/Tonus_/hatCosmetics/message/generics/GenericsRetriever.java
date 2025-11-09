package me.Tonus_.hatCosmetics.message.generics;

import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.message.color.IColorParser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
public class GenericsRetriever implements IGenericsRetriever {
    private static final String GENERICS_FILE = "messages/generics.yml";

    private final Logger logger;
    private final IColorParser colorParser;

    private Map<String, String> generics;

    public @Nullable String getGeneric(@NotNull String key) {
        if (generics == null) {
            generics = loadGenerics();
        }

        var value = generics.get(key);
        if (value == null) {
            logger.warn("Could not retrieve generic with key '{}'.", key);
        }

        return value;
    }

    private @NotNull Map<String, String> loadGenerics() {
        var generics = new HashMap<String, String>();

        try (var inputStream = GenericsRetriever.class.getClassLoader().getResourceAsStream(GENERICS_FILE)) {
            if (inputStream != null) {
                var isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                var yaml = YamlConfiguration.loadConfiguration(isr);

                for (var entry : yaml.getValues(true).entrySet()) {
                    var parsedValue = colorParser.parse(String.valueOf(entry.getValue()));
                    generics.put(entry.getKey(), parsedValue);
                }
            } else throw new IOException("Failed to open input stream!");
        } catch (IOException e) {
            logger.error("Failed to load generic messages! {}", e.toString());
        }

        logger.info("Loaded {} generic messages.", generics.size());
        return generics;
    }
}
