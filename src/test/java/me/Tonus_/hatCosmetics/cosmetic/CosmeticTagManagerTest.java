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

    @Test
    void storeOriginalModelData_returnsTaggedItem() {
        // Arrange
        when(editor.addTag(PersistentDataType.INTEGER, "originalModelData", 5)).thenReturn(editor);
        when(editor.apply()).thenReturn(item);

        // Act
        var result = sut.storeOriginalModelData(item, 5);

        // Assert
        assertEquals(item, result);
    }

    @Test
    void getOriginalModelData_whenTagExists_returnsValue() {
        // Arrange
        when(editor.getTag(PersistentDataType.INTEGER, "originalModelData")).thenReturn(Optional.of(5));

        // Act
        var result = sut.getOriginalModelData(item);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get());
    }

    @Test
    void getOriginalModelData_whenTagMissing_returnsEmpty() {
        // Arrange
        when(editor.getTag(PersistentDataType.INTEGER, "originalModelData")).thenReturn(Optional.empty());

        // Act
        var result = sut.getOriginalModelData(item);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void setOverlayTag_true_returnsTaggedItem() {
        // Arrange
        when(editor.addTag(PersistentDataType.BYTE, "hasOverlay", (byte) 1)).thenReturn(editor);
        when(editor.apply()).thenReturn(item);

        // Act
        var result = sut.setOverlayTag(item, true);

        // Assert
        assertEquals(item, result);
    }

    @Test
    void setOverlayTag_false_returnsTaggedItem() {
        // Arrange
        when(editor.addTag(PersistentDataType.BYTE, "hasOverlay", (byte) 0)).thenReturn(editor);
        when(editor.apply()).thenReturn(item);

        // Act
        var result = sut.setOverlayTag(item, false);

        // Assert
        assertEquals(item, result);
    }

    @Test
    void removeOverlayTags_returnsCleanedItem() {
        // Arrange
        when(editor.removeTag("cosmetic")).thenReturn(editor);
        when(editor.removeTag("hasOverlay")).thenReturn(editor);
        when(editor.removeTag("originalModelData")).thenReturn(editor);
        when(editor.apply()).thenReturn(item);

        // Act
        var result = sut.removeOverlayTags(item);

        // Assert
        assertEquals(item, result);
    }
}
