package me.Tonus_.hatCosmetics.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.cosmetic.ICosmeticEquipManager;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.permissions.PermissionNode;
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
    private final IMessageRetriever messageRetriever;

    @Default
    public void onDefault(@NotNull Player player) {
        inventoryManager.openInventory(player);
    }

    @Subcommand("equip|e")
    @CommandCompletion("@hats")
    public void onEquip(@NotNull Player player, String hatName) {
        if (hatName == null || hatName.isBlank()) {
            messageRetriever.sendMessage(player, MessageReference.COSMETIC_ARG_NOT_GIVEN);
            return;
        }

        equipManager.equip(player, hatName, player, false);
    }

    @Subcommand("equip|e")
    @CommandCompletion("@players @hats")
    @CommandPermission(PermissionNode.ADMIN_EQUIP_OTHER)
    public void onEquipAdmin(@NotNull Player sender, Player target, String hatName) {
        equipManager.equip(target, hatName, sender, false);
    }

    @Subcommand("unequip|u")
    public void onUnequip(@NotNull Player player) {
        equipManager.unequip(player, null);
    }

    @Subcommand("unequip|u")
    @CommandCompletion("@players")
    @CommandPermission(PermissionNode.ADMIN_UNEQUIP_OTHER)
    public void onUnequipAdmin(@NotNull Player sender, Player target) {
        equipManager.unequip(target, sender);
    }

    @Subcommand("reload|r")
    @CommandPermission(PermissionNode.RELOAD)
    public void onReload(@NotNull CommandSender sender) {
        pluginReloader.reload(sender);
    }
}
