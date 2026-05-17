package me.Tonus_.hatCosmetics.cosmetic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface ICosmeticEquipManager {
    boolean equip(@NotNull Player player, @NotNull String cosmeticName, boolean silentDeny);
    boolean unequip(@NotNull Player player);
    @Nullable String getWornCosmeticName(@NotNull Player player);
}
