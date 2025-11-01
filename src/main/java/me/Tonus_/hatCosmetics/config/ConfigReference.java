package me.Tonus_.hatCosmetics.config;

import lombok.AllArgsConstructor;
import me.Tonus_.hatCosmetics.config.structures.*;
import org.bukkit.inventory.ItemStack;


@AllArgsConstructor
public enum ConfigReference {
    VERSION("version", String.class),

    LOCALE("locale.server-locale", String.class),
    FORCED_LOCALE("locale.forced-locale", Boolean.class),

    STORAGE_FORMAT("storage.format", StorageFormat.class),

    PERFORMANCE_CACHING_AGGRESSIVNESS("performance.caching-agressivness", CachingAggressiveness.class),

    GUI_ROWS("gui.rows", Integer.class),
    GUI_BORDER_ITEM("gui.items.border", ItemStack.class),
    GUI_NEXT_PAGE_ITEM("gui.items.next-page", ItemStack.class),
    GUI_PREV_PAGE_ITEM("gui.items.previous-page", ItemStack.class),
    GUI_CLOSE_ITEM("gui.items.close", ItemStack.class),

    HATS_KEEP_ON_DEATH("hats.keep-on-death", Boolean.class),
    ;

    final String yamlPath;
    final Class<?> type;
}