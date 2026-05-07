package me.Tonus_.hatCosmetics.cosmetic;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@RequiredArgsConstructor
public class CosmeticEquipManager implements ICosmeticEquipManager {
    private final ICosmeticItemFactory cosmeticItemFactory;
    private final ICosmeticTagManager cosmeticTagManager;
    private final ICosmeticLoader cosmeticLoader;
    private final IMessageRetriever messageRetriever;

    public boolean equip(@NotNull Player player, @NotNull String cosmeticName) {
        var cosmetic = cosmeticLoader.load().stream()
                .filter(c -> c.name().equalsIgnoreCase(cosmeticName))
                .findFirst()
                .orElse(null);

        if (cosmetic == null) {
            messageRetriever.sendMessage(player, MessageReference.COSMETIC_NOT_FOUND);
            return false;
        }

        var equipment = player.getEquipment();
        if (equipment == null) return false;

        var helmet = equipment.getHelmet();

        if (helmet != null && !helmet.getType().isAir() && getEquippedCosmetic(helmet).isEmpty()) {
            messageRetriever.sendMessage(player, MessageReference.COSMETIC_EQUIP_FAIL);
            return false;
        }

        var hatItem = cosmeticItemFactory.create(cosmetic, player);

        equipment.setHelmet(hatItem);

        messageRetriever.sendMessage(
            player,
            MessageReference.COSMETIC_EQUIP_SUCCESS,
            Map.of("cosmetic", cosmetic.displayName())
        );

        return true;
    }

	public boolean unequip(@NotNull Player player) {
        var equipment = player.getEquipment();
        if (equipment == null) return false;

        var helmet = equipment.getHelmet();
        var cosmeticTag = getEquippedCosmetic(helmet);

        if (cosmeticTag.isEmpty()) {
            messageRetriever.sendMessage(player, MessageReference.COSMETIC_UNEQUIP_FAIL);
            return false;
        }

        var cosmetic = cosmeticLoader.load().stream()
                .filter(c -> c.name().equals(cosmeticTag.get()))
                .findFirst()
                .orElse(null);

        var parsedName = cosmetic != null
                ? cosmetic.displayName()
                : cosmeticTag.get();

        equipment.setHelmet(null);

        messageRetriever.sendMessage(
            player,
            MessageReference.COSMETIC_UNEQUIP_SUCCESS,
            Map.of("cosmetic", parsedName)
        );

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
