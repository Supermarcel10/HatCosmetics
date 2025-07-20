package me.Tonus_.hatCosmetics;

import me.Tonus_.hatCosmetics.events.InventoryEvents;
import me.Tonus_.hatCosmetics.events.MainHatsCommand;
import me.Tonus_.hatCosmetics.manager.ConfigManager;
import me.Tonus_.hatCosmetics.manager.InventoryManager;
import me.Tonus_.hatCosmetics.manager.MessageManager;
import me.Tonus_.hatCosmetics.networking.ModrinthAPIClient;
import me.Tonus_.hatCosmetics.networking.SemanticVersionChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {
    private final HashMap<Player, ItemStack> droppedCosmetic = new HashMap<>();
    public static HashMap<String, ItemStack> hats = new HashMap<>();

    private InventoryManager inventoryManager;

    public InventoryManager getInventoryManager() { return inventoryManager; }

    private MessageManager messageManager;

    public MessageManager getMessageManager() { return messageManager; }

    private ConfigManager configManager;

    public ConfigManager getConfigManager() { return configManager; }

    public static @NotNull Main getInstance() {
        return getPlugin(Main.class);
    }

    public @NotNull Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        inventoryManager = new InventoryManager(this);
        getServer().getPluginManager().registerEvents(new InventoryEvents(this), this);
        Objects.requireNonNull(getCommand("hatcosmetics")).setTabCompleter(new HatCosmeticTab(this));
        Objects.requireNonNull(getCommand("hatcosmetics")).setExecutor(new MainHatsCommand(this));
        new Metrics(this, 11075);

        var modrinthApiClient = new ModrinthAPIClient(this.getLogger(), "4h6EFh3D");
        new SemanticVersionChecker(this, modrinthApiClient, true).checkForUpdates();
    }

    // Ensure cosmetics don't drop if the player dies
    @EventHandler
    public void onDrop(PlayerDeathEvent event) {
        // Check if player had cosmetic before trying to remove drop
        if(event.getKeepInventory()) return;
        if(event.getEntity().getEquipment() == null) return;
        if(event.getEntity().getEquipment().getHelmet() == null) return;
        if(event.getEntity().getEquipment().getHelmet().getItemMeta() == null) return;
        if(event.getEntity().getEquipment().getHelmet().getItemMeta().getLore() == null) return;
        if(event.getEntity().getEquipment().getHelmet().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
            // Find cosmetic location and remove drop
            int i = 0;
            for(ItemStack item : event.getDrops()) {
                if(item.getItemMeta() == null) {
                    i++;
                    continue;
                }
                if(item.getItemMeta().getLore() == null) {
                    i++;
                    continue;
                }
                if(item.getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                    event.getDrops().remove(i);
                    // Prepare for retrieval
                    droppedCosmetic.put(event.getEntity(), item);
                    return;
                }
                else i++;
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(!droppedCosmetic.containsKey(event.getPlayer())) return;
        Player player = event.getPlayer();
        if(player.getEquipment() == null) return;
        player.getEquipment().setHelmet(droppedCosmetic.get(player));
        droppedCosmetic.remove(player);
    }
}