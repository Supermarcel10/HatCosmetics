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

    public Optional<String> getCosmeticTag(ItemStack itemStack) {
        return nbtEditor.of(itemStack).getTag(PersistentDataType.STRING, "cosmetic");
    }

    public ItemStack addCosmeticTag(ItemStack itemStack, String name) {
        return nbtEditor.of(itemStack).addTag(PersistentDataType.STRING, "cosmetic", name).apply();
    }
}
