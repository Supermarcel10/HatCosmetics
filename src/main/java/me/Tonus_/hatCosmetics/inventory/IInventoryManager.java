package me.Tonus_.hatCosmetics.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;


public interface IInventoryManager {
    void openInventory(@NotNull Player player);
    void closeInventory(@NotNull Player player);
    void handleCosmeticsSelectionClick(@NotNull InventoryClickEvent event);
    void closeAllInventories();
}