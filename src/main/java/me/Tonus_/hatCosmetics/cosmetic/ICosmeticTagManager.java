package me.Tonus_.hatCosmetics.cosmetic;

import org.bukkit.inventory.ItemStack;
import java.util.Optional;


public interface ICosmeticTagManager {
    boolean hasOverlay(ItemStack itemStack);
    Optional<String> getCosmeticTag(ItemStack itemStack);
    ItemStack addCosmeticTag(ItemStack itemStack, String name);
}
