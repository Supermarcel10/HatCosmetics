package me.Tonus_.hatCosmetics.cosmetic;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.cosmetic.permission.ICosmeticPermissionChecker;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.storage.ICosmeticStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@RequiredArgsConstructor
public class CosmeticEquipManager implements ICosmeticEquipManager {
    private final ICosmeticItemFactory cosmeticItemFactory;
    private final ICosmeticTagManager cosmeticTagManager;
    private final ICosmeticStorage cosmeticStorage;
    private final IMessageRetriever messageRetriever;
    private final IConfigRetriever configRetriever;
    private final ICosmeticPermissionChecker permissionChecker;

    @Override
    public boolean equip(@NotNull Player target, @NotNull String cosmeticName, @Nullable CommandSender invoker, boolean silentDeny) {
        var isAdminInvokation = invoker != null && invoker != target;
        var cosmetic = cosmeticStorage
            .loadAll()
            .stream()
            .filter(c -> c.name().equalsIgnoreCase(cosmeticName))
            .findFirst()
            .orElse(null);

        if (cosmetic == null) {
            messageRetriever.sendMessage(target, MessageReference.COSMETIC_NOT_FOUND);
            return false;
        }

        if (!permissionChecker.canUseCosmetic(target, cosmetic)) {
            if (!silentDeny) {
                var hideHats = configRetriever.getValue(ConfigReference.GUI_HIDE_HATS, false);
                var msg = hideHats ? MessageReference.COSMETIC_NOT_FOUND : MessageReference.COSMETIC_NO_PERMISSION_LONG;
                messageRetriever.sendMessage(target, msg);
            }

            return false;
        }

        var equipment = target.getEquipment();
        if (equipment == null) return false;

        var helmet = equipment.getHelmet();

        if (helmet != null && !helmet.getType().isAir() && getEquippedCosmetic(helmet).isEmpty()) {
            if (isAdminInvokation) {
                messageRetriever.sendMessage(
                    invoker,
                    MessageReference.COSMETIC_EQUIP_FAIL_OTHER,
                    Map.of("player", target.getName())
                );
            }

            messageRetriever.sendMessage(target, MessageReference.COSMETIC_EQUIP_FAIL);
            return false;
        }

        var hatItem = cosmeticItemFactory.create(cosmetic, target, MessageReference.COSMETIC_INVENTORY_UNEQUIP);

        equipment.setHelmet(hatItem);

        if (isAdminInvokation) {
            messageRetriever.sendMessage(
                invoker,
                MessageReference.COSMETIC_EQUIP_SUCCESS_INVOKER,
                Map.of("player", target.getName(), "cosmetic", cosmetic.displayName(target.locale()))
            );
            messageRetriever.sendMessage(
                target,
                MessageReference.COSMETIC_EQUIP_SUCCESS_TARGET,
                Map.of("cosmetic", cosmetic.displayName(target.locale()))
            );
        } else {
            messageRetriever.sendMessage(
                target,
                MessageReference.COSMETIC_EQUIP_SUCCESS,
                Map.of("cosmetic", cosmetic.displayName(target.locale()))
            );
        }

        return true;
    }

    @Override
    public boolean unequip(@NotNull Player target, @Nullable CommandSender invoker) {
        var isAdminInvokation = invoker != null && invoker != target;

        var equipment = target.getEquipment();
        if (equipment == null) return false;

        var helmet = equipment.getHelmet();
        var cosmeticTag = getEquippedCosmetic(helmet);

        if (cosmeticTag.isEmpty()) {
            if (isAdminInvokation) {
                messageRetriever.sendMessage(
                    invoker,
                    MessageReference.COSMETIC_UNEQUIP_OTHER_FAIL,
                    Map.of("player", target.getName())
                );
            }

            messageRetriever.sendMessage(target, MessageReference.COSMETIC_UNEQUIP_FAIL);

            return false;
        }

        var cosmetic = cosmeticStorage
            .loadAll()
            .stream()
            .filter(c -> c.name().equalsIgnoreCase(cosmeticTag.get()))
            .findFirst()
            .orElse(null);

        var parsedName = cosmetic != null
                ? cosmetic.displayName(target.locale())
                : cosmeticTag.get();

        equipment.setHelmet(null);

        if (isAdminInvokation) {
            messageRetriever.sendMessage(
                invoker,
                MessageReference.COSMETIC_UNEQUIP_SUCCESS_INVOKER,
                Map.of("player", target.getName(), "cosmetic", parsedName)
            );
            messageRetriever.sendMessage(
                target,
                MessageReference.COSMETIC_UNEQUIP_SUCCESS_TARGET,
                Map.of("cosmetic", parsedName)
            );
        } else {
            messageRetriever.sendMessage(
                target,
                MessageReference.COSMETIC_UNEQUIP_SUCCESS,
                Map.of("cosmetic", parsedName)
            );
        }

        return true;
    }

    @Nullable
    public String getWornCosmeticName(@NotNull Player player) {
        var equipment = player.getEquipment();
        if (equipment == null) return null;

        var cosmeticTag = getEquippedCosmetic(equipment.getHelmet());
        if (cosmeticTag.isEmpty()) return null;

        return cosmeticTag.get();
    }

    private Optional<String> getEquippedCosmetic(@Nullable ItemStack helmet) {
        if (helmet == null || helmet.getType().isAir()) return Optional.empty();
        return cosmeticTagManager.getCosmeticTag(helmet);
    }
}
