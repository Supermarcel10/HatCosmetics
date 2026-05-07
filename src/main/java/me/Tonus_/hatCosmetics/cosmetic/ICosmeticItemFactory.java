package me.Tonus_.hatCosmetics.cosmetic;

import java.util.Optional;
import java.util.Set;
import me.Tonus_.hatCosmetics.message.MessageReference;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public interface ICosmeticItemFactory {
    Set<ItemStack> createAll(@NotNull Player player, Optional<String> wornCosmeticName);
    @NotNull ItemStack create(@NotNull Cosmetic cosmetic, Player player, @NotNull MessageReference actionMessage);
}
