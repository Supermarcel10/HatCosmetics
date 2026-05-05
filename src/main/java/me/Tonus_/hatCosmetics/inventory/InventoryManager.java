package me.Tonus_.hatCosmetics.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticLoader;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticSelectionInventoryHolder;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticTagManager;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.utility.editor.NBTEditor;
import me.Tonus_.hatCosmetics.versionedAPICalls.CustomModelData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@RequiredArgsConstructor
public class InventoryManager implements IInventoryManager {
    private static final String CLOSE_TAG = "cl";
    private static final String PREV_TAG = "pv";
    private static final String NEXT_TAG = "nx";

    private static final int DEFAULT_HAT_ROWS = 3;
    private static final Material DEFAULT_BORDER_MATERIAL = Material.CYAN_STAINED_GLASS_PANE;
    private static final Material DEFAULT_CLOSE_MATERIAL = Material.BARRIER;
    private static final Material DEFAULT_NEXT_PAGE_MATERIAL = Material.ARROW;
    private static final Material DEFAULT_PREV_PAGE_MATERIAL = Material.SPECTRAL_ARROW;

    private final NBTEditor nbtEditor;
    private final IConfigRetriever configRetriever;
    private final IMessageRetriever messageRetriever;
    private final Plugin plugin;
    private final CosmeticTagManager cosmeticTagManager;
    private final CustomModelData customModelData;

    private final HashMap<Player, InventoryPlayerContext> playerContexts =
        new HashMap<>();

    public void openInventory(@NotNull Player player) {
        var hatRows = getValidatedHatRows();
        var cosmetics = buildCosmeticItems(player);
        var ipc = new InventoryPlayerContext(player, hatRows, cosmetics);
        playerContexts.put(player, ipc);

        var titleText = getTitleTextComponent(ipc, player);
        var inv = new CosmeticSelectionInventoryHolder(hatRows, titleText).getInventory();

        drawBorder(inv);
        drawExitButton(inv, player);
        drawPageMoveButtons(inv, ipc, player);
        drawCosmetics(inv, ipc, player);

        player.openInventory(inv);
    }

    public void closeInventory(@NotNull Player player) {
        playerContexts.remove(player);
    }

    public void handleCosmeticsSelectionClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        var player = (Player) event.getWhoClicked();
        if (playerContexts.get(player) == null) {
            player.closeInventory();
            return;
        }

        nbtEditor
            .of(event.getCurrentItem())
            .getTag(PersistentDataType.STRING, "menu")
            .ifPresent(tag -> handleMenuTag(player, tag));
    }

    private void handleMenuTag(@NotNull Player player, @NotNull String tag) {
        var inventory = player.getOpenInventory().getTopInventory();

        switch (tag) {
            case CLOSE_TAG -> {
                playerContexts.remove(player);
                player.closeInventory();
            }
            case PREV_TAG -> {
                var playerContext = playerContexts.get(player);
                if (playerContext.prevPage()) {
                    drawPageMoveButtons(inventory, playerContext, player);
                    player.updateInventory();
                }
            }
            case NEXT_TAG -> {
                var playerContext = playerContexts.get(player);
                if (playerContext.nextPage()) {
                    drawPageMoveButtons(inventory, playerContext, player);
                    player.updateInventory();
                }
            }
        }
    }

    private @NotNull ItemStack createMenuItem(
        Material material,
        Component itemName,
        @Nullable String tag
    ) {
        var itemStackEditor = nbtEditor
            .of(new ItemStack(material))
            .setName(itemName);

        if (tag != null) {
            itemStackEditor.addTag(PersistentDataType.STRING, "menu", tag);
        }

        return itemStackEditor.apply();
    }

    private @NotNull Component getTitleTextComponent(@NotNull InventoryPlayerContext ipc, Player player) {
        var baseText = new StringBuilder(messageRetriever.getMessage(player, MessageReference.GUI_TITLE));

        if (ipc.getMaxPage() > 1) {
            baseText.append(ChatColor.DARK_GRAY);
            baseText.append(" (");
            baseText.append(ipc.getCurrentPage());
            baseText.append("/");
            baseText.append(ipc.getMaxPage());
            baseText.append(")");
        }

        return Component.text(baseText.toString());
    }

    private void drawBorder(Inventory inventory) {
        var material = configRetriever.getValue(ConfigReference.GUI_BORDER_MATERIAL, DEFAULT_BORDER_MATERIAL);
        var item = createMenuItem(material, Component.empty(), null);

        var bottomRowStart = inventory.getSize() - 9;

        for (var i = 0; i < 9; ++i) {
            inventory.setItem(i, item);
            inventory.setItem(bottomRowStart + i, item);
        }
    }

    private void drawExitButton(@NotNull Inventory inventory, Player player) {
        var material = configRetriever.getValue(ConfigReference.GUI_CLOSE_MATERIAL, DEFAULT_CLOSE_MATERIAL);
        var text = Component.text(messageRetriever.getMessage(player, MessageReference.GUI_CLOSE));

        var closeButtonItem = createMenuItem(material, text, CLOSE_TAG);

        inventory.setItem(inventory.getSize() - 5, closeButtonItem);
    }

    private void drawPageMoveButtons(
        @NotNull Inventory inventory,
        @NotNull InventoryPlayerContext ipc,
        Player player
    ) {
        int currentPage = ipc.getCurrentPage();
        int maxPage = ipc.getMaxPage();

        var borderMaterial = configRetriever.getValue(
            ConfigReference.GUI_BORDER_MATERIAL,
            DEFAULT_BORDER_MATERIAL
        );

        // Previous page
        var material = configRetriever.getValue(ConfigReference.GUI_PREV_PAGE_MATERIAL, DEFAULT_PREV_PAGE_MATERIAL);
        var text = messageRetriever.getMessage(player, MessageReference.GUI_PREV);
        var prevItem = createPageButton(
            currentPage > 1,
            currentPage - 1,
            material,
            text,
            PREV_TAG,
            borderMaterial
        );

        inventory.setItem(inventory.getSize() - 6, prevItem);

        // Next page
        material = configRetriever.getValue(ConfigReference.GUI_NEXT_PAGE_MATERIAL, DEFAULT_NEXT_PAGE_MATERIAL);
        text = messageRetriever.getMessage(player, MessageReference.GUI_NEXT);
        var nextItem = createPageButton(
            currentPage < maxPage,
            currentPage + 1,
            material,
            text,
            NEXT_TAG,
            borderMaterial
        );

        inventory.setItem(inventory.getSize() - 4, nextItem);
    }

    private @NotNull ItemStack createPageButton(
        boolean isActive,
        int pageNumber,
        Material material,
        String buttonName,
        String tag,
        Material borderMaterial
    ) {
        if (isActive) {
            var text = Component.text(ChatColor.AQUA + buttonName + ChatColor.DARK_GRAY + pageNumber);
            return createMenuItem(material, text, tag);
        } else {
            return createMenuItem(borderMaterial, Component.empty(), null);
        }
    }

    private int getValidatedHatRows() {
        var rows = configRetriever.getValue(ConfigReference.GUI_ROWS);
        if (rows == null || rows < 1 || rows > 4 || rows == -1) {
            // TODO: Implement auto-calculation
            return DEFAULT_HAT_ROWS;
        }

        return rows;
    }

    private Set<ItemStack> buildCosmeticItems(Player player) {
        return CosmeticLoader.load()
            .stream()
            .map(c -> createCosmeticItem(c, player))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private @NotNull ItemStack createCosmeticItem(
        @NotNull Cosmetic cosmetic,
        Player player
    ) {
        var baseItem = new ItemStack(cosmetic.material());
        var modelDataItem = customModelData.appendModelData(baseItem,cosmetic.customModelData());

        var meta = modelDataItem.getItemMeta();
        if (meta != null) {
            var displayName = Component.text(ChatColor.translateAlternateColorCodes('&', cosmetic.displayName()));
            meta.displayName(displayName);

            var lore = new ArrayList<Component>();
            for (var line : cosmetic.description()) {
                lore.add(Component.text(ChatColor.translateAlternateColorCodes('&', line)));
            }

            lore.add(Component.empty());

            var inventoryEquipMessage = ChatColor.translateAlternateColorCodes(
                '&',
                messageRetriever.getMessage(
                    player,
                    MessageReference.COSMETIC_INVENTORY_EQUIP
                )
            );
            lore.add(Component.text(inventoryEquipMessage));

            meta.lore(lore);
            modelDataItem.setItemMeta(meta);
        }

        return cosmeticTagManager.addCosmeticTag(
            modelDataItem,
            cosmetic.name()
        );
    }

    private void drawCosmetics(
        @NotNull Inventory inventory,
        @NotNull InventoryPlayerContext ipc,
        Player player
    ) {
        var cosmetics = new ArrayList<>(ipc.getCosmetics());
        var hatRows = ipc.getHatRowsPerPage();
        var pageIndex = (ipc.getCurrentPage() - 1) * hatRows * 9;

        var slot = 9;
        for (
            var i = pageIndex;
            i < cosmetics.size() && slot - 8 <= hatRows * 9;
            i++
        ) {
            inventory.setItem(slot, cosmetics.get(i));
            slot++;
        }
    }
}
