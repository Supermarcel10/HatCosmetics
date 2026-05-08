package me.Tonus_.hatCosmetics.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.cosmetic.ICosmeticEquipManager;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import me.Tonus_.hatCosmetics.reload.IPluginReloader;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
@CommandAlias("hatcosmetics|hats|hc")
public class MainCommand extends BaseCommand {
    private final IInventoryManager inventoryManager;
    private final ICosmeticEquipManager equipManager;
    private final IPluginReloader pluginReloader;

    @Default
    public void onDefault(@NotNull Player player) {
        inventoryManager.openInventory(player);
    }

    @Subcommand("equip|e")
    public void onEquip(@NotNull Player player, String hatName) {
        equipManager.equip(player, hatName);
    }

    @Subcommand("unequip|u")
    public void onUnequip(@NotNull Player player) {
        equipManager.unequip(player);
    }

    @Subcommand("reload|r")
    @CommandPermission("hatcosmetics.reload")
    public void onReload(@NotNull CommandSender sender) {
        pluginReloader.reload(sender);
    }
}
