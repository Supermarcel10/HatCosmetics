package me.Tonus_.hatCosmetics.player;

import java.util.HashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticSelectionInventoryHolder;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticTagManager;
import me.Tonus_.hatCosmetics.cosmetic.ICosmeticEquipManager;
import me.Tonus_.hatCosmetics.cosmetic.ICosmeticLoader;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class PlayerEventManager implements Listener {
    private final IInventoryManager inventoryManager;
    private final IConfigRetriever configRetriever;
    private final CosmeticTagManager tagManager;
    private final ICosmeticEquipManager equipManager;
    private final ICosmeticLoader cosmeticLoader;

    private final HashMap<Player, ItemStack> droppedCosmetic = new HashMap<>();

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
        if (isCosmetic(((Player) event.getWhoClicked()).getEquipment().getHelmet())) {
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        if (event.getKeepInventory()) return;

        var keepOnDeath = configRetriever.getValue(ConfigReference.HATS_KEEP_ON_DEATH, true);
        if (Boolean.FALSE.equals(keepOnDeath)) return;

        var player = event.getEntity();
        var inventory = player.getInventory();
        var possibleCosmetics = inventory.getArmorContents();

        for(var possibleCosmetic : possibleCosmetics) {
            var cosmeticTag = tagManager.getCosmeticTag(possibleCosmetic);
            if (cosmeticTag.isPresent()) {
                event.getDrops().remove(possibleCosmetic);
                droppedCosmetic.put(player, possibleCosmetic);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        var player = event.getPlayer();
        var cosmetic = droppedCosmetic.remove(player);
        player.getInventory().setHelmet(cosmetic);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();

        // Delete context
        inventoryManager.closeInventory(player);

        var isPlayerAlive = Double.compare(player.getHealth(), 0.0F) != 0;
        if (isPlayerAlive) return;

        // Respawn & set dropped cosmetic if present
        player.spigot().respawn();
        var cosmetic = droppedCosmetic.remove(player);
        player.getInventory().setHelmet(cosmetic);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var helmet = player.getEquipment().getHelmet();
        var cosmeticTag = tagManager.getCosmeticTag(helmet);

        if (cosmeticTag.isEmpty()) return;

        var exists = cosmeticLoader.load().stream()
            .anyMatch(c -> c.name().equalsIgnoreCase(cosmeticTag.get()));

        // Unequip any hats no longer present
        if (!exists) {
            player.getEquipment().setHelmet(null);
        }
    }

    private void handlePlayerInventoryClick(InventoryClickEvent event) {
        if (!isCosmetic(event.getCurrentItem()) && !isCosmetic(event.getCursor())) return;

        event.setCancelled(true);

        if (event.isLeftClick() && isCosmetic(event.getCurrentItem())) {
            var player = (Player) event.getWhoClicked();
            var helmet = player.getEquipment().getHelmet();
            var currentTag = tagManager.getCosmeticTag(event.getCurrentItem());
            var helmetTag = tagManager.getCosmeticTag(helmet);

            if (isClickedItemCurrentEquippedCosmetic(currentTag, helmetTag)) {
                equipManager.unequip(player);
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
