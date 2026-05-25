package me.Tonus_.hatCosmetics.cosmetic;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;


public class CosmeticSelectionInventoryHolder implements InventoryHolder {
    @Getter private final Inventory inventory;

    public CosmeticSelectionInventoryHolder(int numberOfRows, Component titleTextComponent) {
        inventory = Bukkit.createInventory(this, numberOfRows * 9 + 18, titleTextComponent);
    }
}
