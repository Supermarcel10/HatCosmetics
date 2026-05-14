package me.Tonus_.hatCosmetics.cosmetic;

import java.util.List;
import me.Tonus_.hatCosmetics.permissions.PermissionNode;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;


public record Cosmetic(
    String name,
    String displayName,
    Material material,
    String customModelData,
    List<String> description,
    @Nullable String permission
) {
    public String getPermissionNode() {
        return permission != null ? permission : PermissionNode.forHat(name);
    }
}
