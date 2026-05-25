package me.Tonus_.hatCosmetics.cosmetic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import me.Tonus_.hatCosmetics.cosmetic.permission.ICosmeticPermissionChecker;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.versionedAPICalls.CustomModelData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CosmeticItemFactory implements ICosmeticItemFactory {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final CustomModelData customModelData;
    private final CosmeticTagManager cosmeticTagManager;
    private final IMessageRetriever messageRetriever;
    private final ICosmeticPermissionChecker permissionChecker;

    @Override
    public Set<ItemStack> createItems(
        @NotNull Player player,
        @NotNull List<Cosmetic> cosmetics,
        @NotNull Optional<String> wornCosmeticName
    ) {
        var result = new LinkedHashSet<ItemStack>();
        for (var cosmetic : cosmetics) {
            var actionMessage = getActionMessage(player, cosmetic, wornCosmeticName);
            result.add(create(cosmetic, player, actionMessage));
        }

        return result;
    }

    public @NotNull ItemStack create(@NotNull Cosmetic cosmetic, Player player, @NotNull MessageReference actionMessage) {
        var baseItem = new ItemStack(cosmetic.material());
        var modelDataItem = customModelData.appendModelData(baseItem, cosmetic.customModelData());

        var meta = modelDataItem.getItemMeta();
        meta.displayName(LEGACY.deserialize(cosmetic.displayName(player.locale())));

        var lore = new ArrayList<Component>();
        for (var line : cosmetic.description(player.locale())) {
            lore.add(LEGACY.deserialize(line));
        }

        lore.add(Component.empty());
        lore.add(Component.text(messageRetriever.getMessage(player, actionMessage)));

        meta.lore(lore);
        modelDataItem.setItemMeta(meta);

        return cosmeticTagManager.addCosmeticTag(modelDataItem, cosmetic.name());
    }

    private @NotNull MessageReference getActionMessage(
        Player player,
        Cosmetic cosmetic,
        Optional<String> wornCosmeticName
    ) {
        if (!permissionChecker.canUseCosmetic(player, cosmetic))
            return MessageReference.COSMETIC_NO_PERMISSION_SHORT;

        if (wornCosmeticName.isEmpty())
            return MessageReference.COSMETIC_INVENTORY_EQUIP;

        return cosmetic.name().equalsIgnoreCase(wornCosmeticName.get())
                ? MessageReference.COSMETIC_INVENTORY_UNEQUIP
                : MessageReference.COSMETIC_INVENTORY_EQUIP;
    }
}
