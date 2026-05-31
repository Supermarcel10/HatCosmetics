package me.Tonus_.hatCosmetics.cosmetic;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.utility.editor.NBTEditor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;


@RequiredArgsConstructor
public class CosmeticTagManager implements ICosmeticTagManager {
    private final NBTEditor nbtEditor;

    public boolean hasOverlay(ItemStack itemStack) {
        return nbtEditor.of(itemStack).getTag(PersistentDataType.BYTE, "hasOverlay").orElse((byte)0) == 1;
    }

    public ItemStack setOverlayTag(ItemStack itemStack, boolean hasOverlay) {
        return nbtEditor.of(itemStack).addTag(PersistentDataType.BYTE, "hasOverlay", (byte)(hasOverlay ? 1 : 0)).apply();
    }

    public Optional<String> getCosmeticTag(ItemStack itemStack) {
        return nbtEditor.of(itemStack).getTag(PersistentDataType.STRING, "cosmetic");
    }

    public ItemStack addCosmeticTag(ItemStack itemStack, String name) {
        return nbtEditor.of(itemStack).addTag(PersistentDataType.STRING, "cosmetic", name).apply();
    }

    public ItemStack storeOriginalModelData(ItemStack itemStack, int modelData) {
        return nbtEditor.of(itemStack).addTag(PersistentDataType.INTEGER, "originalModelData", modelData).apply();
    }

    public Optional<Integer> getOriginalModelData(ItemStack itemStack) {
        return nbtEditor.of(itemStack).getTag(PersistentDataType.INTEGER, "originalModelData");
    }

    public ItemStack removeOverlayTags(ItemStack itemStack) {
        return nbtEditor.of(itemStack)
            .removeTag("cosmetic")
            .removeTag("hasOverlay")
            .removeTag("originalModelData")
            .apply();
    }
}
