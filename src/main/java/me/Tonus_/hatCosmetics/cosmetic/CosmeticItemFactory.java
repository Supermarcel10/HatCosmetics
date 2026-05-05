package me.Tonus_.hatCosmetics.cosmetic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.versionedAPICalls.CustomModelData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class CosmeticItemFactory implements ICosmeticItemFactory {
    private final CustomModelData customModelData;
    private final CosmeticTagManager cosmeticTagManager;
    private final IMessageRetriever messageRetriever;

    public Set<ItemStack> createAll(@NotNull Player player) {
        return CosmeticLoader.load().stream()
                .map(c -> create(c, player))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public @NotNull ItemStack create(@NotNull Cosmetic cosmetic, Player player) {
        var baseItem = new ItemStack(cosmetic.material());
        var modelDataItem = customModelData.appendModelData(baseItem, cosmetic.customModelData());

        var meta = modelDataItem.getItemMeta();
        if (meta != null) {
            var displayName = Component.text(ChatColor.translateAlternateColorCodes('&', cosmetic.displayName()));
            meta.displayName(displayName);

            var lore = new ArrayList<Component>();
            for (var line : cosmetic.description()) {
                lore.add(Component.text(ChatColor.translateAlternateColorCodes('&', line)));
            }

            lore.add(Component.empty());

            var inventoryEquipMessage = ChatColor.translateAlternateColorCodes('&', messageRetriever.getMessage(player, MessageReference.COSMETIC_INVENTORY_EQUIP));
            lore.add(Component.text(inventoryEquipMessage));

            meta.lore(lore);
            modelDataItem.setItemMeta(meta);
        }

        return cosmeticTagManager.addCosmeticTag(modelDataItem, cosmetic.name());
    }
}
