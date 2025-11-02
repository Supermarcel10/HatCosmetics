package me.Tonus_.hatCosmetics.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.Tonus_.hatCosmetics.config.structures.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ConfigReference<T> {
    public static ConfigReference<String> VERSION = cr("version", String.class);

    public static ConfigReference<String> SERVER_LOCALE = cr("locale.server-locale", String.class);
    public static ConfigReference<Boolean> FORCED_LOCALE = cr("locale.forced-locale", Boolean.class);

    public static ConfigReference<StorageFormat> STORAGE_FORMAT = cr("storage.format", StorageFormat.class);

    public static ConfigReference<CachingAggressiveness> PERFORMANCE_CACHING_AGGRESSIVNESS = cr("performance.caching-agressivness", CachingAggressiveness.class);

    public static ConfigReference<Integer> GUI_ROWS = cr("gui.rows", Integer.class);
    public static ConfigReference<ItemStack> GUI_BORDER_ITEM = cr("gui.items.border", ItemStack.class);
    public static ConfigReference<ItemStack> GUI_NEXT_PAGE_ITEM = cr("gui.items.next-page", ItemStack.class);
    public static ConfigReference<ItemStack> GUI_PREV_PAGE_ITEM = cr("gui.items.previous-page", ItemStack.class);
    public static ConfigReference<ItemStack> GUI_CLOSE_ITEM = cr("gui.items.close", ItemStack.class);

    public static ConfigReference<Boolean> HATS_KEEP_ON_DEATH = cr("hats.keep-on-death", Boolean.class);

    @Contract("_, _ -> new")
    static <T> @NotNull ConfigReference<T> cr(@NotNull String yamlPath, @NotNull Class<T> type) {
        return new ConfigReference<>(yamlPath, type);
    }

    final String yamlPath;
    final Class<T> type;
}