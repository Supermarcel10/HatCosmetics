package me.Tonus_.hatCosmetics.cosmetic.permission;

import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;
import org.bukkit.entity.Player;


@RequiredArgsConstructor
public class CosmeticPermissionChecker implements ICosmeticPermissionChecker {
    @Override
    public boolean canUseCosmetic(Player player, Cosmetic cosmetic) {
        return player.hasPermission(cosmetic.getPermissionNode());
    }
}
