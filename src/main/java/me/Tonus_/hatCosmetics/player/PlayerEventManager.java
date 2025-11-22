package me.Tonus_.hatCosmetics.player;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticSelectionInventoryHolder;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticTagManager;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class PlayerEventManager implements Listener {
    private final IInventoryManager inventoryManager;
    private final IConfigRetriever configRetriever;
    private final CosmeticTagManager tagManager;

    private final HashMap<Player, ItemStack> droppedCosmetic = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        var inventoryHolder = event.getInventory().getHolder();
        if (inventoryHolder instanceof CosmeticSelectionInventoryHolder) {
            inventoryManager.handleCosmeticsSelectionClick(event);
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
}
