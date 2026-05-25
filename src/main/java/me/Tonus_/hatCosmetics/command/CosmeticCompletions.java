package me.Tonus_.hatCosmetics.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;
import me.Tonus_.hatCosmetics.cosmetic.permission.ICosmeticPermissionChecker;
import me.Tonus_.hatCosmetics.permissions.PermissionNode;
import me.Tonus_.hatCosmetics.storage.ICosmeticStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class CosmeticCompletions {
    private final ICosmeticStorage cosmeticStorage;
    private final ICosmeticPermissionChecker permissionChecker;

    public Collection<String> hats(BukkitCommandCompletionContext c) {
        var player = c.getPlayer();
        var cosmetics = cosmeticStorage.loadAll();

        if (permissionChecker.hasWildcard(player)) {
            return cosmetics.stream().map(Cosmetic::name).collect(Collectors.toList());
        }

        return cosmetics.stream()
            .filter(cosmetic -> permissionChecker.canUseCosmetic(player, cosmetic))
            .map(Cosmetic::name)
            .collect(Collectors.toList());
    }

    public Collection<String> playerTarget(BukkitCommandCompletionContext c) {
        var player = c.getPlayer();

        if (player.hasPermission(PermissionNode.ADMIN_EQUIP_OTHER)) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
        }

        return List.of();
    }
}
