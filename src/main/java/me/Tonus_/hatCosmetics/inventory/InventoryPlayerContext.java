package me.Tonus_.hatCosmetics.inventory;

import java.util.HashSet;
import java.util.LinkedHashSet;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import java.util.Set;


@Getter
public class InventoryPlayerContext {
    private final LinkedHashSet<ItemStack> cosmetics = new LinkedHashSet<>();
    private final int hatRowsPerPage;
    private int currentPage = 1;
    private int maxPage;

    public InventoryPlayerContext(int hatRowsPerPage, @NotNull Set<ItemStack> cosmetics) {
        this.hatRowsPerPage = hatRowsPerPage;
        this.cosmetics.addAll(cosmetics);
        calculateMaxPage();
    }

    @TestOnly
    InventoryPlayerContext(HashSet<ItemStack> cosmetics, int hatRowsPerPage) {
        this.hatRowsPerPage = hatRowsPerPage;
        this.cosmetics.addAll(cosmetics);

        calculateMaxPage();
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

        // Default 1 screen for no items
        rows = rows == 0 ? 1 : rows;

        maxPage = (rows + hatRowsPerPage - 1) / hatRowsPerPage;
    }
}
