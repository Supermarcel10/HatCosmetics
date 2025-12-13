package me.Tonus_.hatCosmetics.events;

import de.tr7zw.nbtapi.NBTItem;
import me.Tonus_.hatCosmetics.manager.InventoryManager;
import me.Tonus_.hatCosmetics.Main;
import me.Tonus_.hatCosmetics.manager.MessageManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class InventoryEvents implements Listener {
    private final Main main;
    private final MessageManager messageManager;
    private final InventoryManager inventoryManager;

    public InventoryEvents(Main main) {
        this.main = main;
        this.messageManager = main.getMessageManager();
        this.inventoryManager = main.getInventoryManager();
    }

    @EventHandler
    public void onRightClickWithHelmet(PlayerInteractEvent event) {
        if (!isRightClickAction(event.getAction()) || !isHelmet(event.getMaterial())) return;

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack currentHelmet = inventory.getHelmet();
        ItemStack newHelmet = event.getItem();

        if (currentHelmet == null || newHelmet == null || !isHatCosmetic(currentHelmet)) return;

        event.setCancelled(true);

        player.playSound(player.getLocation(), getEquipSoundForMaterial(newHelmet.getType()), 1, 1);
        inventory.setHelmet(inventory.getItemInMainHand());
        inventory.setItemInMainHand(null);
    }

    private boolean isHelmet(Material material) {
        return material.name().endsWith("_HELMET");
    }

    private boolean isRightClickAction(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    private boolean isHatCosmetic(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;

        List<String> lore = meta.getLore();
        return lore != null && lore.get(0).contains("Hat Cosmetic");
    }

    private Sound getEquipSoundForMaterial(Material material) {
        try {
            String materialType = material.toString().replace("_HELMET", "");
            return Sound.valueOf("ITEM_ARMOR_EQUIP_" + materialType);
        } catch (IllegalArgumentException e) {
            return Sound.ITEM_ARMOR_EQUIP_GENERIC;
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Unequips hat if there is no helmet on the cursor
        if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
            if(event.getCursor() != null &&
                    event.getCursor().getItemMeta() != null &&
                    event.getCursor().getItemMeta().getLore() != null &&
                    event.getCursor().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                if(event.getCurrentItem() != null && !event.getCurrentItem().getType().toString().contains("HELMET")) {
                    event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();

        // Unequips hat if clicked on in invalid slot
        if(!event.getView().getTitle().contains(messageManager.getMessage("gui_title"))) {
            if(event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) return;
            if(event.getCurrentItem().getItemMeta().getLore() == null) return;
            if(event.getCurrentItem().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
                    player.sendMessage(messageManager.getPlayerMessage("hat_unequip_success", event.getCurrentItem()));
                }
                event.setCurrentItem(new ItemStack(Material.AIR));
                event.setCancelled(true);
            }
        }

        // GUI Management
        else {
            if(!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

            event.setCancelled(true);

            if(event.getSlot() >= 9 && event.getSlot() < event.getInventory().getSize()-9 && event.getCurrentItem().getItemMeta().getLore() != null &&
                    event.getCurrentItem().getItemMeta().getLore().get(0).contains("Hat Cosmetic")) {
                helmCheck: {
                    var helmet = Objects.requireNonNull(player.getEquipment()).getHelmet();
                    if (helmet != null && helmet.getType() != Material.AIR) {
                        if (helmet.getItemMeta() != null && helmet.getItemMeta().getLore() != null &&
                                helmet.getItemMeta().getLore().get(0).contains("Hat Cosmetic"))
                            break helmCheck;
                        player.sendMessage(messageManager.getPlayerMessage("helmet_exist", null));
                        player.closeInventory();
                        return;
                    }
                }
                ItemStack item = event.getCurrentItem();
                NBTItem nbti = new NBTItem(item);
                ItemMeta meta = item.getItemMeta();
                int lastLine = meta.getLore().size() - 1;
                if(meta.getLore().get(lastLine).equals(messageManager.getMessage("hat_equip"))) {
                    if (player.hasPermission(nbti.getString("Permission"))) {
                        List<String> lore = meta.getLore();
                        assert lore != null;
                        lore.remove(lore.size()-1);
                        lore.remove(lore.size()-1);
                        meta.setLore(lore);
                        item.setItemMeta(meta);

                        player.getEquipment().setHelmet(item);
                        player.sendMessage(messageManager.getPlayerMessage("hat_success", item));
                        player.closeInventory();
                        return;
                    }
                    player.sendMessage(messageManager.getPlayerMessage("no_hat_permission", item));
                } else if(meta.getLore().get(lastLine).equals(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("hat_unequip")))) {
                    player.closeInventory();
                    player.sendMessage(messageManager.getPlayerMessage("hat_unequip_success", item));
                    player.getEquipment().setHelmet(new ItemStack(Material.AIR));
                }
            } else if(event.getSlot() == event.getInventory().getSize()-5 && event.getCurrentItem().getType().equals(Material.BARRIER) && event.getCurrentItem().getItemMeta().getDisplayName().contains("Close Menu")) {
                player.closeInventory();
            } else if(event.getSlot() == event.getInventory().getSize()-4 && event.getCurrentItem().getType().equals(Material.ARROW) && event.getCurrentItem().getItemMeta().getDisplayName().contains("Next Page")) {
                int currentPage = inventoryManager.getPlayerPage(player.getUniqueId());
                inventoryManager.setPlayerPage(player.getUniqueId(), currentPage + 1);
                player.openInventory(inventoryManager.openInv(player));
            } else if(event.getSlot() == event.getInventory().getSize()-6 && event.getCurrentItem().getType().equals(Material.ARROW) && event.getCurrentItem().getItemMeta().getDisplayName().contains("Previous Page")) {
                int currentPage = inventoryManager.getPlayerPage(player.getUniqueId());
                inventoryManager.setPlayerPage(player.getUniqueId(), currentPage - 1);
                player.openInventory(inventoryManager.openInv(player));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Clears the current page the player was on when they close the GUI
        if(event.getView().getTitle().contains(messageManager.getMessage("gui_title"))) {
            Player player = (Player) event.getPlayer();
            Bukkit.getScheduler().runTaskLater(main, () -> {
                if(!player.getOpenInventory().getTitle().contains(messageManager.getMessage("gui_title"))) {
                    inventoryManager.removePlayerPage(player.getUniqueId());
                    inventoryManager.removePlayerHats(player.getUniqueId());
                }
            }, 1L);
        }
    }
}
