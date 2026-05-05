package me.Tonus_.hatCosmetics.versionedAPICalls;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

class CustomModelDataTest {
    private final Logger logger = mock(Logger.class);
    private final CustomModelData sut = new CustomModelData(logger);

    private ItemMeta meta = mock(ItemMeta.class);
	private ItemStack item = createMockItemStack(meta);

    @Test
    void appendModelData_withNumericString_setsCustomModelDataOnItemMeta() {
        // Act
        sut.appendModelData(item, "1000101");

        // Assert
        verify(meta).setCustomModelData(1000101);
    }

    @Test
    void appendModelData_withNonNumericString_logsWarningAndSkipsModelData() {
        // Act
        sut.appendModelData(item, "non_numeric_key");

        // Assert
        verify(logger).log(
            Level.WARNING,
            "Non-numeric custom model data \"{0}\" is not supported on this version. Skipping.",
            "non_numeric_key"
        );
        verify(meta, never()).setCustomModelData(0);
    }

    @Test
    void appendModelData_clonesItemBeforeModifying() {
        // Arrange
        var cloneMeta = mock(ItemMeta.class);
        var clone = createMockItemStack(cloneMeta);

        var original = mock(ItemStack.class);
        when(original.clone()).thenReturn(clone);
        when(original.getItemMeta()).thenReturn(meta);

        // Act
        var result = sut.appendModelData(original, "999");

        // Assert
        assertSame(clone, result);
        verify(cloneMeta).setCustomModelData(999);
    }

    private static ItemStack createMockItemStack(ItemMeta meta) {
        var item = mock(ItemStack.class);
        when(item.getItemMeta()).thenReturn(meta);
        when(item.clone()).thenReturn(item);
        return item;
    }
}
