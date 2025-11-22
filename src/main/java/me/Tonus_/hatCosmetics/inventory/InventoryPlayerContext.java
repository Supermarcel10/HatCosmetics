package me.Tonus_.hatCosmetics.inventory;

import java.util.HashSet;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


@Getter
public class InventoryPlayerContext {
    private final HashSet<ItemStack> cosmetics = new HashSet<>();
    private int currentPage = 1;
    private int maxPage;

    public InventoryPlayerContext(Player player) {
        parseHats(player);
        calculateMaxPage();
    }

    private void parseHats(Player player) {
    }

    public boolean nextPage() {
        var newPage = currentPage + 1;

        var changed = newPage <= maxPage;
        if (changed) currentPage = newPage;

        return changed;
    }

    public boolean prevPage() {
        var newPage = currentPage - 1;

        var changed = newPage > 0;
        if (changed) currentPage = newPage;

        return changed;
    }

    private void calculateMaxPage() {
        var rows = (cosmetics.size() + 8) / 9;

        // TODO: Is the below line even necessary?
        rows = rows == 0 ? 1 : rows;

        maxPage = (rows + 2) / 3;
    }
}
