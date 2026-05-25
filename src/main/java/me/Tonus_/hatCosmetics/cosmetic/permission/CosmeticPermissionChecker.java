package me.Tonus_.hatCosmetics.cosmetic.permission;

import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;
import me.Tonus_.hatCosmetics.permissions.PermissionNode;
import org.bukkit.entity.Player;


public class CosmeticPermissionChecker implements ICosmeticPermissionChecker {
    @Override
    public boolean canUseCosmetic(Player player, Cosmetic cosmetic) {
        return player.hasPermission(PermissionNode.HAT_WILDCARD)
            || player.hasPermission(cosmetic.getPermissionNode());
    }

    @Override
    public boolean hasWildcard(Player player) {
        return player.hasPermission(PermissionNode.HAT_WILDCARD);
    }
}
