package me.Tonus_.hatCosmetics.utility.editor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.HatCosmetics;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("unused")
public class NBTEditor {
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ItemStackEditor {
		private final ItemStack item;
		private final ItemMeta meta;
		private final PersistentDataContainer pdc;

		@Contract("_, _, _ -> this")
		public <P, C> ItemStackEditor addTag(PersistentDataType<P, C> type, String key, C val) {
			if (pdc != null) {
				pdc.set(new NamespacedKey(HatCosmetics.getInstance(), key), type, val);
			}
			return this;
		}

		@Contract("_ -> this")
		public ItemStackEditor removeTag(String key) {
			if (pdc != null) {
				pdc.remove(new NamespacedKey(HatCosmetics.getInstance(), key));
			}
			return this;
		}

		@Contract("_ -> this")
		public ItemStackEditor removeTagIfExists(String key) {
			if (pdc != null) {
				NamespacedKey nKey = new NamespacedKey(HatCosmetics.getInstance(), key);
				if (pdc.has(nKey)) {
					pdc.remove(nKey);
				}
			}
			return this;
		}

		public <P, C> @Nullable C getTag(PersistentDataType<P, C> type, String key) {
			return pdc != null ? pdc.get(new NamespacedKey(HatCosmetics.getInstance(), key), type) : null;
		}

		public <P, C> C getOrDefault(PersistentDataType<P, C> type, String key, C def) {
			C val = getTag(type, key);
			return val != null ? val : def;
		}

		public ItemStack apply() {
			if (item != null && meta != null) {
				item.setItemMeta(meta);
			}
			return item;
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ItemMetaEditor {
		private final ItemMeta meta;
		private final PersistentDataContainer pdc;

		@Contract("_, _, _ -> this")
		public <P, C> ItemMetaEditor addTag(PersistentDataType<P, C> type, String key, C val) {
			if (pdc != null) {
				pdc.set(new NamespacedKey(HatCosmetics.getInstance(), key), type, val);
			}
			return this;
		}

		@Contract("_ -> this")
		public ItemMetaEditor removeTag(String key) {
			if (pdc != null) {
				pdc.remove(new NamespacedKey(HatCosmetics.getInstance(), key));
			}
			return this;
		}

		@Contract("_ -> this")
		public ItemMetaEditor removeTagIfExists(String key) {
			if (pdc != null) {
				NamespacedKey nKey = new NamespacedKey(HatCosmetics.getInstance(), key);
				if (pdc.has(nKey)) {
					pdc.remove(nKey);
				}
			}
			return this;
		}

		public <P, C> @Nullable C getTag(PersistentDataType<P, C> type, String key) {
			return pdc != null ? pdc.get(new NamespacedKey(HatCosmetics.getInstance(), key), type) : null;
		}

		public <P, C> C getOrDefault(PersistentDataType<P, C> type, String key, C def) {
			C val = getTag(type, key);
			return val != null ? val : def;
		}

		public ItemMeta get() {
			return meta;
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class PDCEditor {
		private final PersistentDataContainer pdc;

		@Contract("_, _, _ -> this")
		public <P, C> PDCEditor addTag(PersistentDataType<P, C> type, String key, C val) {
			if (pdc != null) {
				pdc.set(new NamespacedKey(HatCosmetics.getInstance(), key), type, val);
			}
			return this;
		}

		@Contract("_ -> this")
		public PDCEditor removeTag(String key) {
			if (pdc != null) {
				pdc.remove(new NamespacedKey(HatCosmetics.getInstance(), key));
			}
			return this;
		}

		@Contract("_ -> this")
		public PDCEditor removeTagIfExists(String key) {
			if (pdc != null) {
				NamespacedKey nKey = new NamespacedKey(HatCosmetics.getInstance(), key);
				if (pdc.has(nKey)) {
					pdc.remove(nKey);
				}
			}
			return this;
		}

		public <P, C> @Nullable C getTag(PersistentDataType<P, C> type, String key) {
			return pdc != null ? pdc.get(new NamespacedKey(HatCosmetics.getInstance(), key), type) : null;
		}

		public <P, C> C getOrDefault(PersistentDataType<P, C> type, String key, C def) {
			C val = getTag(type, key);
			return val != null ? val : def;
		}

		public PersistentDataContainer get() {
			return pdc;
		}
	}

	@Contract("null -> new")
	public static @NotNull ItemStackEditor of(@Nullable ItemStack item) {
		if (item == null) {
			return new ItemStackEditor(null, null, null);
		}
		ItemMeta meta = item.getItemMeta();
		return new ItemStackEditor(item, meta, meta != null ? meta.getPersistentDataContainer() : null);
	}

	@Contract("_ -> new")
	public static @NotNull ItemMetaEditor of(@Nullable ItemMeta meta) {
		if (meta == null) {
			return new ItemMetaEditor(null, null);
		}
		return new ItemMetaEditor(meta, meta.getPersistentDataContainer());
	}

	@Contract("_ -> new")
	public static @NotNull PDCEditor of(@Nullable PersistentDataContainer pdc) {
		return new PDCEditor(pdc);
	}
}