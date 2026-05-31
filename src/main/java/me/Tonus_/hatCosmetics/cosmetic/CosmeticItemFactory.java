package me.Tonus_.hatCosmetics.cosmetic;

import java.util.*;
import me.Tonus_.hatCosmetics.cosmetic.permission.ICosmeticPermissionChecker;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.versionedAPICalls.CustomModelData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CosmeticItemFactory implements ICosmeticItemFactory {
    private final CustomModelData customModelData;
    private final CosmeticTagManager cosmeticTagManager;
    private final IMessageRetriever messageRetriever;
    private final ICosmeticPermissionChecker permissionChecker;

    @Override
    public Set<ItemStack> createItems(
        @NotNull Player player,
        @NotNull List<Cosmetic> cosmetics,
        @NotNull Optional<String> wornCosmeticName,
        boolean isWornCosmeticOverlayed
    ) {
        var result = new LinkedHashSet<ItemStack>();
        for (var cosmetic : cosmetics) {
            var actionMessage = getActionMessage(player, cosmetic, wornCosmeticName, isWornCosmeticOverlayed);
            result.add(create(cosmetic, player, actionMessage));
        }

        return result;
    }

    public @NotNull ItemStack create(@NotNull Cosmetic cosmetic, Player player, @NotNull MessageReference actionMessage) {
        var baseItem = new ItemStack(cosmetic.material());
        var modelDataItem = customModelData.appendModelData(baseItem, cosmetic.customModelData());

        var meta = modelDataItem.getItemMeta();
        var displayName = Component.text(ChatColor.translateAlternateColorCodes('&', cosmetic.displayName(player.locale())));
        meta.displayName(displayName);

        var lore = new ArrayList<Component>();
        for (var line : cosmetic.description(player.locale())) {
            lore.add(Component.text(ChatColor.translateAlternateColorCodes('&', line)));
        }

        lore.add(Component.empty());
        lore.add(Component.text(messageRetriever.getMessage(player, actionMessage)));

        meta.lore(lore);
        modelDataItem.setItemMeta(meta);

        return cosmeticTagManager.addCosmeticTag(modelDataItem, cosmetic.name());
    }

    @Override
    public @NotNull ItemStack applyOverlay(@NotNull ItemStack armor, @NotNull Cosmetic cosmetic, @NotNull Player player) {
        var item = armor.clone();
        var meta = item.getItemMeta();

        if (meta.hasCustomModelData()) {
            item = cosmeticTagManager.storeOriginalModelData(item, meta.getCustomModelData());
        }

        item = customModelData.appendModelData(item, cosmetic.customModelData());
        meta = item.getItemMeta();

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        var lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<Component>();
        lore.add(0, Component.text("Cosmetic: " + ChatColor.translateAlternateColorCodes('&', cosmetic.displayName(player.locale()))));
        lore.add(Component.empty());
        lore.add(Component.text(messageRetriever.getMessage(player, MessageReference.COSMETIC_INVENTORY_UNEQUIP_OVERLAYED)));
        meta.lore(lore);
        item.setItemMeta(meta);

        item = cosmeticTagManager.addCosmeticTag(item, cosmetic.name());
        item = cosmeticTagManager.setOverlayTag(item, true);

        return item;
    }

    @Override
    public @NotNull ItemStack removeOverlay(@NotNull ItemStack armor) {
        var item = armor.clone();

        var originalCmd = cosmeticTagManager.getOriginalModelData(item);

        item = cosmeticTagManager.removeOverlayTags(item);

        var meta = item.getItemMeta();
        originalCmd.ifPresent(meta::setCustomModelData);

        meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);

        if (meta.hasLore()) {
            var lore = new ArrayList<>(meta.lore());
            if (!lore.isEmpty()) {
                lore.remove(0);
            }
            if (!lore.isEmpty()) {
                lore.remove(lore.size() - 1);
            }
            if (!lore.isEmpty()) {
                lore.remove(lore.size() - 1);
            }
            meta.lore(lore);
        }

        item.setItemMeta(meta);

        return item;
    }

    private @NotNull MessageReference getActionMessage(Player player, Cosmetic cosmetic, Optional<String> wornCosmeticName, boolean isWornCosmeticOverlayed) {
        if (!permissionChecker.canUseCosmetic(player, cosmetic)) {
            return MessageReference.COSMETIC_NO_PERMISSION_SHORT;
        }

        if (wornCosmeticName.isEmpty()) {
            return MessageReference.COSMETIC_INVENTORY_EQUIP;
        }

        if (cosmetic.name().equalsIgnoreCase(wornCosmeticName.get())) {
            return isWornCosmeticOverlayed
                ? MessageReference.COSMETIC_INVENTORY_UNEQUIP_OVERLAYED
                : MessageReference.COSMETIC_INVENTORY_UNEQUIP;
        }

        return MessageReference.COSMETIC_INVENTORY_EQUIP;
    }
}
