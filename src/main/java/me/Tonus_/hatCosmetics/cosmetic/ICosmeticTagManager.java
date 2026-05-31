package me.Tonus_.hatCosmetics.cosmetic;

import org.bukkit.inventory.ItemStack;
import java.util.Optional;


public interface ICosmeticTagManager {
    boolean hasOverlay(ItemStack itemStack);
    Optional<String> getCosmeticTag(ItemStack itemStack);
    ItemStack addCosmeticTag(ItemStack itemStack, String name);
    ItemStack storeOriginalModelData(ItemStack itemStack, int modelData);
    Optional<Integer> getOriginalModelData(ItemStack itemStack);
    ItemStack setOverlayTag(ItemStack itemStack, boolean hasOverlay);
    ItemStack removeOverlayTags(ItemStack itemStack);
}
