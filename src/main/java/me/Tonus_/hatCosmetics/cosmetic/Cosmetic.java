package me.Tonus_.hatCosmetics.cosmetic;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.Tonus_.hatCosmetics.permissions.PermissionNode;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;


public record Cosmetic(
    String name,
    Material material,
    String customModelData,
    @Nullable String permission,
    Map<String, DisplayData> rawDisplay,
    int order
) {
    public record DisplayData(String name, List<String> description) {}

    public String getPermissionNode() {
        return permission != null ? permission : PermissionNode.forHat(name);
    }

    public String displayName(Locale locale) {
        var entry = findDisplay(locale);
        return entry != null ? entry.name() : name;
    }

    public List<String> description(Locale locale) {
        var entry = findDisplay(locale);
        return entry != null ? entry.description() : List.of();
    }

    private DisplayData findDisplay(Locale locale) {
        if (rawDisplay.isEmpty()) {
            return null;
        }

        var localeEntry = rawDisplay.get(locale.toString());
        if (localeEntry != null) {
            return localeEntry;
        }

        var language = locale.getLanguage();
        if (!language.isEmpty()) {
            var languageEntry = rawDisplay.get(language);
            if (languageEntry != null) {
                return languageEntry;
            }
        }

        // TODO: Figure out how to set default more accurately
        return rawDisplay.values().stream().findFirst().orElse(null);
    }
}
