package me.Tonus_.hatCosmetics.utility.editor;

import me.Tonus_.hatCosmetics.HatCosmetics;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


// TODO: See if this can be made into a builder NBTEditor instead
public class NBTEditor {
	public static <P, C> ItemStack addTag(ItemStack item, PersistentDataType<P, C> type, String key, C val) {
		if (item != null && item.getItemMeta() != null) {
			item.setItemMeta(addTag(item.getItemMeta(), type, key, val));
		}

		return item;
	}

	public static ItemStack removeTag(ItemStack item, String key) {
		if (item != null && item.getItemMeta() != null) {
			item.setItemMeta(removeTag(item.getItemMeta(), key));
		}

		return item;
	}

	public static ItemStack removeTagIfExists(ItemStack item, String key) {
		if (item != null && item.getItemMeta() != null) {
			item.setItemMeta(removeTagIfExists(item.getItemMeta(), key));
		}

		return item;
	}

	public static <P, C> @Nullable C getTag(ItemStack item, PersistentDataType<P, C> type, String key) {
		if (item != null && item.getItemMeta() != null) {
			return getTag(item.getItemMeta(), type, key);
		}

		return null;
	}

	public static <P, C> C getOrDefault(ItemStack item, PersistentDataType<P, C> type, String key, C def) {
		if (item != null && item.getItemMeta() != null) {
			C val = getTag(item.getItemMeta(), type, key);
			if (val != null) return val;
		}

		return def;
	}

	@Contract("_, _, _, _ -> param1")
	public static <P, C> @NotNull ItemMeta addTag(@NotNull ItemMeta meta, PersistentDataType<P, C> type, String key, C val) {
		addTag(meta.getPersistentDataContainer(), type, key, val);

		return meta;
	}

	public static @NotNull ItemMeta removeTag(@NotNull ItemMeta meta, String key) {
		removeTag(meta.getPersistentDataContainer(), key);

		return meta;
	}

	public static @NotNull ItemMeta removeTagIfExists(@NotNull ItemMeta meta, String key) {
		removeTagIfExists(meta.getPersistentDataContainer(), key);

		return meta;
	}

	public static <P, C> C getTag(@NotNull ItemMeta meta, PersistentDataType<P, C> type, String key) {
		return getTag(meta.getPersistentDataContainer(), type, key);
	}

	public static <P, C> C getOrDefault(@NotNull ItemMeta meta, PersistentDataType<P, C> type, String key, C def) {
		return getOrDefault(meta.getPersistentDataContainer(), type, key, def);
	}

	public static <P, C> void addTag(@NotNull PersistentDataContainer pdc, PersistentDataType<P, C> type, String key, C val) {
		pdc.set(new NamespacedKey(HatCosmetics.getInstance(), key), type, val);
	}

	public static void removeTag(@NotNull PersistentDataContainer pdc, String key) {
		pdc.remove(new NamespacedKey(HatCosmetics.getInstance(), key));
	}

	public static void removeTagIfExists(@NotNull PersistentDataContainer pdc, String key) {
		NamespacedKey nKey = new NamespacedKey(HatCosmetics.getInstance(), key);
		if (pdc.has(nKey)) {
			pdc.remove(nKey);
		}
	}

	public static <P, C> C getTag(@NotNull PersistentDataContainer pdc, PersistentDataType<P, C> type, String key) {
		return pdc.get(new NamespacedKey(HatCosmetics.getInstance(), key), type);
	}

	public static <P, C> C getOrDefault(@NotNull PersistentDataContainer pdc, PersistentDataType<P, C> type, String key, C def) {
		C val = getTag(pdc, type, key);
		if (val != null) return val;
		return def;
	}
}
