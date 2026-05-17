package me.Tonus_.hatCosmetics.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.structures.*;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import java.util.Locale;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ConfigReference<T> {
    public static ConfigReference<String> VERSION = cr("version", String.class);

    public static ConfigReference<Locale> SERVER_LOCALE = cr("locale.server-locale", Locale.class);
    public static ConfigReference<Boolean> FORCED_LOCALE = cr("locale.force-locale", Boolean.class);

    public static ConfigReference<StorageFormat> STORAGE_FORMAT = cr("storage.format", StorageFormat.class);

    public static ConfigReference<CachingAggressiveness> PERFORMANCE_CACHING_AGGRESSIVNESS = cr("performance.caching-agressivness", CachingAggressiveness.class);

    public static ConfigReference<Integer> GUI_ROWS = cr("gui.rows", Integer.class);
    public static ConfigReference<Material> GUI_BORDER_MATERIAL = cr("gui.items.border", Material.class);
    public static ConfigReference<Material> GUI_NEXT_PAGE_MATERIAL = cr("gui.items.next-page", Material.class);
    public static ConfigReference<Material> GUI_PREV_PAGE_MATERIAL = cr("gui.items.previous-page", Material.class);
    public static ConfigReference<Material> GUI_CLOSE_MATERIAL = cr("gui.items.close", Material.class);
    public static ConfigReference<Boolean> GUI_HIDE_HATS = cr("gui.hide-hats", Boolean.class);

    public static ConfigReference<Boolean> HATS_KEEP_ON_DEATH = cr("hats.keep-on-death", Boolean.class);

    private final String yamlPath;
    private final Class<T> type;

    @Contract("_, _ -> new")
    static <T> @NotNull ConfigReference<T> cr(@NotNull String yamlPath, @NotNull Class<T> type) {
        return new ConfigReference<>(yamlPath, type);
    }
}
