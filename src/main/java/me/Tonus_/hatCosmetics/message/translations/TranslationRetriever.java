package me.Tonus_.hatCosmetics.message.translations;

import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.message.MessageReference;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


// TODO: Instead of loading all translations, load only the server locale, and then load the rest when needed (e.g. when a player joins)
@RequiredArgsConstructor
public class TranslationRetriever implements ITranslationRetriever {
    private final Plugin plugin;

    private Map<String, FileConfiguration> translations = null;

    @TestOnly
    public TranslationRetriever(Plugin plugin, Map<String, FileConfiguration> translations) {
        this.plugin = plugin;
        this.translations = translations;
    }

    private void loadTranslations() {
        loadLocalTranslations();
        loadAllTranslations();

        plugin.getSLF4JLogger().info("Loaded {} translations.", translations.size());
    }

    public @Nullable String tryGetTranslation(String language, MessageReference messageReference) {
        if (translations == null) {
            translations = new HashMap<>(0);
            loadTranslations();
        }

        var yaml = translations.get(language);
        var yamlPath = messageReference.getYamlPath();

        return yaml == null ? null : yaml.getString(yamlPath);
    }

    /**
     * Loads all local translations
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadLocalTranslations() {
        var messageDir = new File(plugin.getDataFolder(), "messages");
        if (!messageDir.exists()) {
            messageDir.mkdirs();
        }

        if (!messageDir.isDirectory()) {
            plugin.getSLF4JLogger().error("Messages is not a directory!");
            return;
        }

        var files = messageDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                translations.put(
                        file.getName().replace(".yml", ""),
                        YamlConfiguration.loadConfiguration(file)
                );
            }
        }
    }

    /**
     * Loads all generic translations from the JAR file
     */
    private void loadAllTranslations() {
        try (var jarFile = new JarFile(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            var entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (isValidTranslationFile(entry)) loadTranslationFile(entry);
            }
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("Failed to load translations from JAR file! {}", e.toString());
        }
    }

    /**
     * Loads the translation file into memory
     * @param entry JarEntry to load
     */
    private void loadTranslationFile(@NotNull JarEntry entry) {
        var entryName = entry.getName();

        try (var inputStream = plugin.getClass().getResourceAsStream("/" + entryName)) {
            if (inputStream != null) {
                var locale = entry.getName().replace("messages/", "").replace(".yml", "");
                var language = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                translations.put(locale, language);
            }
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("Failed to load translation file {}. {}", entryName, e.toString());
        }
    }

    /**
     * Checks if the entry is a valid translation file
     * @param entry JarEntry to check
     * @return boolean if the entry is a valid translation file
     */
    private static boolean isValidTranslationFile(@NotNull JarEntry entry) {
        var entryName = entry.getName();
        return entryName.startsWith("messages/") &&
                !entry.isDirectory() &&
                entryName.endsWith(".yml") &&
                !entryName.equals("messages/template.yml");
    }
}
