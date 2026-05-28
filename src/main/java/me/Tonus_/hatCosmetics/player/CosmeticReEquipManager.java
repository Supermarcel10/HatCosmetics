package me.Tonus_.hatCosmetics.player;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticTagManager;
import me.Tonus_.hatCosmetics.cosmetic.ICosmeticEquipManager;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import me.Tonus_.hatCosmetics.storage.ICosmeticStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


public class CosmeticReEquipManager implements Listener {
    private static final long FOLIA_RESPAWN_POLL_TICKS = 10L;
    private static final int MAX_QUIT_WHILE_DEAD = 10;

    private final Plugin plugin;
    private final IInventoryManager inventoryManager;
    private final IConfigRetriever configRetriever;
    private final CosmeticTagManager tagManager;
    private final ICosmeticEquipManager equipManager;
    private final ICosmeticStorage cosmeticStorage;
    private final Optional<FoliaSchedulerCache> foliaCache;

    public CosmeticReEquipManager(
        Plugin plugin,
        IInventoryManager inventoryManager,
        IConfigRetriever configRetriever,
        CosmeticTagManager tagManager,
        ICosmeticEquipManager equipManager,
        ICosmeticStorage cosmeticStorage,
        Logger logger
    ) {
        this.plugin = plugin;
        this.inventoryManager = inventoryManager;
        this.configRetriever = configRetriever;
        this.tagManager = tagManager;
        this.equipManager = equipManager;
        this.cosmeticStorage = cosmeticStorage;
        this.foliaCache = FoliaSchedulerCache.tryCreate(logger);
    }

    private final LinkedHashMap<UUID, ItemStack> droppedCosmetic = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, ItemStack> eldest) {
            return size() > MAX_QUIT_WHILE_DEAD;
        }
    };

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        if (event.getKeepInventory()) return;

        var keepOnDeath = configRetriever.getValue(ConfigReference.HATS_KEEP_ON_DEATH, true);
        if (Boolean.FALSE.equals(keepOnDeath)) return;

        var player = event.getEntity();
        var inventory = player.getInventory();
        var helmet = inventory.getHelmet();

        var cosmeticTag = tagManager.getCosmeticTag(helmet);
        if (cosmeticTag.isPresent()) {
            event.getDrops().remove(helmet);
            droppedCosmetic.put(player.getUniqueId(), helmet);
        }

        foliaCache.ifPresent(cache -> startFoliaRespawnPoll(player, cache));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        if (foliaCache.isPresent()) return;

        var player = event.getPlayer();
        var cosmetic = droppedCosmetic.remove(player.getUniqueId());
        player.getInventory().setHelmet(cosmetic);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        inventoryManager.closeInventory(player);

        if (foliaCache.isPresent()) return;

        var isPlayerAlive = Double.compare(player.getHealth(), 0.0F) != 0;
        if (isPlayerAlive) return;

        player.spigot().respawn();
        var cosmetic = droppedCosmetic.remove(player.getUniqueId());
        player.getInventory().setHelmet(cosmetic);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        unequipStaleCosmetic(player);

        if (foliaCache.isPresent()) {
            var dropped = droppedCosmetic.remove(player.getUniqueId());
            if (dropped != null) {
                player.getInventory().setHelmet(dropped);
                return;
            }
        }

        equipDefaultCosmetic(player);
    }

    private void startFoliaRespawnPoll(Player player, FoliaSchedulerCache cache) {
        try {
            var scheduler = cache.getScheduler.invoke(player);
            cache.runAtFixedRate.invoke(scheduler, plugin, (Consumer<Object>) task -> {
                if (player.isDead()) return;

                try {
                    task.getClass().getMethod("cancel").invoke(task);
                } catch (Exception ignored) {}

                var cosmetic = droppedCosmetic.remove(player.getUniqueId());
                if (cosmetic != null) {
                    player.getInventory().setHelmet(cosmetic);
                }
            }, null, 1L, FOLIA_RESPAWN_POLL_TICKS);
        } catch (Exception e) {
            plugin.getSLF4JLogger().error("Failed to start Folia respawn poll for {}", player.getName(), e);
        }
    }

    private void unequipStaleCosmetic(Player player) {
        var equipment = player.getEquipment();
        var helmet = equipment.getHelmet();

        var cosmeticTag = tagManager.getCosmeticTag(helmet);
        if (cosmeticTag.isEmpty()) return;

        var exists = cosmeticStorage
            .loadAll()
            .stream()
            .anyMatch(c -> c.name().equalsIgnoreCase(cosmeticTag.get()));

        if (!exists) {
            equipment.setHelmet(null);
        }
    }

    private void equipDefaultCosmetic(Player player) {
        var equipment = player.getEquipment();
        var helmet = equipment.getHelmet();
        if (helmet != null && !helmet.getType().isAir()) return;

        var defaultHat = configRetriever.getValue(ConfigReference.HATS_DEFAULT_HAT, "NONE");
        if (defaultHat == null || defaultHat.isBlank() || "NONE".equalsIgnoreCase(defaultHat)) return;

        equipManager.equip(player, defaultHat, player, true);
    }

    @RequiredArgsConstructor
    private static class FoliaSchedulerCache {
        final Method getScheduler;
        final Method runAtFixedRate;

        static Optional<FoliaSchedulerCache> tryCreate(Logger logger) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");

                var getScheduler = Player.class.getMethod("getScheduler");
                var runAtFixedRate = getScheduler.getReturnType().getMethod(
                    "runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class
                );

                logger.info("Folia scheduler cache created successfully.");
                return Optional.of(new FoliaSchedulerCache(getScheduler, runAtFixedRate));
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                logger.info("Server is not running Folia.");
                return Optional.empty();
            }
        }
    }
}
