package me.Tonus_.hatCosmetics.player;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticSelectionInventoryHolder;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticTagManager;
import me.Tonus_.hatCosmetics.cosmetic.ICosmeticEquipManager;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class PlayerEventManager implements Listener {
    private final IInventoryManager inventoryManager;
    private final CosmeticTagManager tagManager;
    private final ICosmeticEquipManager equipManager;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        var topHolder = event.getView().getTopInventory().getHolder();
        if (topHolder instanceof CosmeticSelectionInventoryHolder) {
            inventoryManager.handleCosmeticsSelectionClick(event);
            return;
        }

        handlePlayerInventoryClick(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        var helmet = player.getEquipment().getHelmet();
        if (isCosmetic(helmet)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CosmeticSelectionInventoryHolder) {
            var player = (Player) event.getPlayer();
            inventoryManager.closeInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        var itemStack = event.getItemDrop().getItemStack();

        var cosmeticTag = tagManager.getCosmeticTag(itemStack);
        if (cosmeticTag.isPresent()) event.setCancelled(true);
    }

    private void handlePlayerInventoryClick(InventoryClickEvent event) {
        if (!isCosmetic(event.getCurrentItem()) && !isCosmetic(event.getCursor())) return;

        event.setCancelled(true);

        if (event.isLeftClick() && isCosmetic(event.getCurrentItem())) {
            if (!(event.getWhoClicked() instanceof Player player)) return;

            var equipment = player.getEquipment();
            var helmet = equipment.getHelmet();

            var currentTag = tagManager.getCosmeticTag(event.getCurrentItem());
            var helmetTag = tagManager.getCosmeticTag(helmet);

            if (isClickedItemCurrentEquippedCosmetic(currentTag, helmetTag)) {
                equipManager.unequip(player, null);
            }
        }
    }

    private boolean isClickedItemCurrentEquippedCosmetic(Optional<String> currentTag, Optional<String> helmetTag) {
        return currentTag.isPresent() && helmetTag.isPresent() && currentTag.get().equals(helmetTag.get());
    }

    private boolean isCosmetic(ItemStack item) {
        return item != null && !item.getType().isAir()
            && tagManager.getCosmeticTag(item).isPresent();
    }
}
