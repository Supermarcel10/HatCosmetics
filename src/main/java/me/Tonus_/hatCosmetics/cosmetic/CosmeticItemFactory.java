package me.Tonus_.hatCosmetics.cosmetic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
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
    private final ICosmeticLoader cosmeticLoader;

    public Set<ItemStack> createAll(@NotNull Player player) {
        return cosmeticLoader.load().stream()
                .map(c -> create(c, player, MessageReference.COSMETIC_INVENTORY_EQUIP))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<ItemStack> createAll(@NotNull Player player, @NotNull Optional<String> wornCosmeticName) {
        return cosmeticLoader.load().stream()
                .map(cosmetic -> getActionMessage(player, wornCosmeticName, cosmetic))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

	private @NotNull ItemStack getActionMessage(Player player, Optional<String> wornCosmeticName, Cosmetic cosmetic) {
		if (wornCosmeticName.isEmpty()) return create(cosmetic, player);

		var actionMessage = cosmetic.name().equals(wornCosmeticName.get())
		        ? MessageReference.COSMETIC_INVENTORY_UNEQUIP
		        : MessageReference.COSMETIC_INVENTORY_EQUIP;

		return create(cosmetic, player, actionMessage);
	}

    public @NotNull ItemStack create(@NotNull Cosmetic cosmetic, Player player) {
        return create(cosmetic, player, MessageReference.COSMETIC_INVENTORY_EQUIP);
    }

    private @NotNull ItemStack create(@NotNull Cosmetic cosmetic, Player player, @NotNull MessageReference actionMessage) {
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
            lore.add(Component.text(messageRetriever.getMessage(player, actionMessage)));

            meta.lore(lore);
            modelDataItem.setItemMeta(meta);
        }

        return cosmeticTagManager.addCosmeticTag(modelDataItem, cosmetic.name());
    }
}
