package me.Tonus_.hatCosmetics.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.cosmetic.ICosmeticEquipManager;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.permissions.PermissionNode;
import me.Tonus_.hatCosmetics.reload.IPluginReloader;
import org.bukkit.Bukkit;
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
    @CommandCompletion("@hats @playerTarget")
    public void onEquip(@NotNull Player player, String hatName, @Optional String targetUsername) {
        if (hatName == null || hatName.isBlank()) {
            messageRetriever.sendMessage(player, MessageReference.COSMETIC_ARG_NOT_GIVEN);
            return;
        }

        if (targetUsername != null) {
            var target = Bukkit.getPlayer(targetUsername);
            if (target == null) {
                messageRetriever.sendMessage(player, MessageReference.PLAYER_NOT_ONLINE);
                return;
            }
            if (!player.hasPermission(PermissionNode.ADMIN_EQUIP_OTHER)) {
                messageRetriever.sendMessage(player, MessageReference.COSMETIC_NO_PERMISSION_LONG);
                return;
            }
            equipManager.equip(target, hatName, player, false);
            return;
        }

        equipManager.equip(player, hatName, player, false);
    }

    @Subcommand("unequip|u")
    @CommandCompletion("@players")
    public void onUnequip(@NotNull Player player, @Optional Player target) {
        if (target != null) {
            if (!player.hasPermission(PermissionNode.ADMIN_UNEQUIP_OTHER)) {
                messageRetriever.sendMessage(player, MessageReference.COSMETIC_NO_PERMISSION_LONG);
                return;
            }

            equipManager.unequip(target, player);
            return;
        }

        equipManager.unequip(player, null);
    }

    @Subcommand("reload|r")
    @CommandPermission(PermissionNode.RELOAD)
    public void onReload(@NotNull CommandSender sender) {
        pluginReloader.reload(sender);
    }
}
