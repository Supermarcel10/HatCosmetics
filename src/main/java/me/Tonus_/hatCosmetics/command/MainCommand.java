package me.Tonus_.hatCosmetics.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
@CommandAlias("hatcosmetics|hats|hc")
public class MainCommand extends BaseCommand {
    private final IInventoryManager inventoryManager;

    @Default
    public void onDefault(@NotNull Player player) {
        inventoryManager.openInventory(player);
    }

    @Subcommand("equip|e")
    public void onEquip(@NotNull Player player, String hatName) {

    }

    @Subcommand("unequip|u")
    public void onUnequip(@NotNull Player player) {

    }
}
