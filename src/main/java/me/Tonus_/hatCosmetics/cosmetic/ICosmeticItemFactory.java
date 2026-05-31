package me.Tonus_.hatCosmetics.cosmetic;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import me.Tonus_.hatCosmetics.message.MessageReference;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public interface ICosmeticItemFactory {
    Set<ItemStack> createItems(
        @NotNull Player player,
        @NotNull List<Cosmetic> cosmetics,
        @NotNull Optional<String> wornCosmeticName,
        boolean isWornCosmeticOverlayed
    );

    @NotNull
    ItemStack create(
        @NotNull Cosmetic cosmetic,
        Player player,
        @NotNull MessageReference actionMessage
    );

    @NotNull
    ItemStack applyOverlay(
        @NotNull ItemStack armor,
        @NotNull Cosmetic cosmetic,
        @NotNull Player player
    );

    @NotNull
    ItemStack removeOverlay(@NotNull ItemStack armor);
}
