package me.Tonus_.hatCosmetics.cosmetic.permission;

import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;
import org.bukkit.entity.Player;


public interface ICosmeticPermissionChecker {
    boolean canUseCosmetic(Player player, Cosmetic cosmetic);
}
