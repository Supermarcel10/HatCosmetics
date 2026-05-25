package me.Tonus_.hatCosmetics.cosmetic;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface ICosmeticEquipManager {
    boolean equip(@NotNull Player target, @NotNull String cosmeticName, @Nullable CommandSender invoker, boolean silentDeny);
    boolean unequip(@NotNull Player target, @Nullable CommandSender invoker);
    @Nullable String getWornCosmeticName(@NotNull Player player);
}
