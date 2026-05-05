package me.Tonus_.hatCosmetics.cosmetic;

import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public interface ICosmeticItemFactory {
    Set<ItemStack> createAll(@NotNull Player player);
    @NotNull ItemStack create(@NotNull Cosmetic cosmetic, Player player);
}
