package me.Tonus_.hatCosmetics.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import me.Tonus_.hatCosmetics.utility.editor.NBTEditor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class CosmeticTagManagerTest {
    private final NBTEditor nbtEditor = mock(NBTEditor.class);
    private final CosmeticTagManager sut = new CosmeticTagManager(nbtEditor);
    private final ItemStack item = mock(ItemStack.class);

    private NBTEditor.ItemStackEditor editor;

    @BeforeEach
    void setUp() {
        editor = mock(NBTEditor.ItemStackEditor.class);
        when(nbtEditor.of(item)).thenReturn(editor);
    }

    @Test
    void addCosmeticTag_returnsTaggedItem() {
        // Arrange
        when(editor.addTag(PersistentDataType.STRING, "cosmetic", "staffHat")).thenReturn(editor);
        when(editor.apply()).thenReturn(item);

        // Act
        var result = sut.addCosmeticTag(item, "staffHat");

        // Assert
        assertEquals(item, result);
    }

    @Test
    void getCosmeticTag_whenTagExists_returnsTagValue() {
        // Arrange
        when(editor.getTag(PersistentDataType.STRING, "cosmetic")).thenReturn(Optional.of("staffHat"));

        // Act
        var result = sut.getCosmeticTag(item);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("staffHat", result.get());
    }

    @Test
    void getCosmeticTag_whenTagMissing_returnsEmpty() {
        // Arrange
        when(editor.getTag(PersistentDataType.STRING, "cosmetic")).thenReturn(Optional.empty());

        // Act
        var result = sut.getCosmeticTag(item);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void hasOverlay_whenTagNotSet_returnsFalse() {
        // Arrange
        when(editor.getTag(PersistentDataType.BYTE, "hasOverlay")).thenReturn(Optional.empty());

        // Act
        var result = sut.hasOverlay(item);

        // Assert
        assertFalse(result);
    }

    @Test
    void hasOverlay_whenTagIsOne_returnsTrue() {
        // Arrange
        when(editor.getTag(PersistentDataType.BYTE, "hasOverlay")).thenReturn(Optional.of((byte) 1));

        // Act
        var result = sut.hasOverlay(item);

        // Assert
        assertTrue(result);
    }
}
