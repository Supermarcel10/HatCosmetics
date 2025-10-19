package me.Tonus_.hatCosmetics.utility.editor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NBTEditor {
	private final Plugin plugin;

	/**
	 * Abstract base class for NBT editors.
	 *
	 * @param <T> The type of object being edited.
	 * @param <E> The type of the editor itself (for method chaining).
	 */
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static abstract class BaseEditor<T, E extends BaseEditor<T, E>> {
		private final Plugin plugin;
		protected final PersistentDataContainer pdc;

		/**
		 * Returns the current instance of the editor.
		 *
		 * @return The current editor instance.
		 */
		protected abstract E self();

		/**
		 * Adds a tag to the persistent data container.
		 *
		 * @param <P>  The primitive type of the tag.
		 * @param <C>  The complex type of the tag.
		 * @param type The persistent data type.
		 * @param key  The key for the tag.
		 * @param val  The value to set.
		 * @return The current editor instance.
		 */
		@Contract("_, _, _ -> this")
		public <P, C> E addTag(PersistentDataType<P, C> type, String key, C val) {
			if (pdc != null) {
				pdc.set(new NamespacedKey(plugin, key), type, val);
			}

			return self();
		}

		/**
		 * Removes a tag from the persistent data container.
		 *
		 * @param key The key of the tag to remove.
		 * @return The current editor instance.
		 */
		@Contract("_ -> this")
		public E removeTag(String key) {
			if (pdc != null) {
				pdc.remove(new NamespacedKey(plugin, key));
			}

			return self();
		}

		/**
		 * Removes a tag from the persistent data container if it exists.
		 *
		 * @param type The persistent data type of the tag.
		 * @param key  The key of the tag to remove.
		 * @return The current editor instance.
		 */
		@Contract("_, _ -> this")
		public <P, C> E removeTagIfExists(PersistentDataType<P, C> type, String key) {
			if (pdc != null) {
				NamespacedKey nKey = new NamespacedKey(plugin, key);
				if (pdc.has(nKey, type)) {
					pdc.remove(nKey);
				}
			}

			return self();
		}

		/**
		 * Gets the value of a tag from the persistent data container.
		 *
		 * @param <P>  The primitive type of the tag.
		 * @param <C>  The complex type of the tag.
		 * @param type The persistent data type.
		 * @param key  The key of the tag.
		 * @return The value of the tag, or null if not found.
		 */
		public <P, C> @Nullable C getTag(PersistentDataType<P, C> type, String key) {
			return pdc != null ? pdc.get(new NamespacedKey(plugin, key), type) : null;
		}

		/**
		 * Gets the value of a tag from the persistent data container, or a default
		 * value if not found.
		 *
		 * @param <P>  The primitive type of the tag.
		 * @param <C>  The complex type of the tag.
		 * @param type The persistent data type.
		 * @param key  The key of the tag.
		 * @param def  The default value to return if the tag is not found.
		 * @return The value of the tag, or the default value if not found.
		 */
		public <P, C> C getOrDefault(PersistentDataType<P, C> type, String key, C def) {
			C val = getTag(type, key);
			return val != null ? val : def;
		}

		/**
		 * Gets the edited object.
		 *
		 * @return The edited object.
		 */
		public abstract T get();
	}

	/**
	 * Editor for ItemStack objects.
	 */
	public static class ItemStackEditor extends BaseEditor<ItemStack, ItemStackEditor> {
		private final ItemStack item;
		private final ItemMeta meta;

		/**
		 * Construct a new ItemStackEditor.
		 */
		private ItemStackEditor(Plugin plugin, ItemStack item, ItemMeta meta, PersistentDataContainer pdc) {
			super(plugin, pdc);
			this.item = item;
			this.meta = meta;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected ItemStackEditor self() {
			return this;
		}

		/**
		 * Applies the changes to the ItemStack.
		 *
		 * @return The modified ItemStack.
		 */
		public ItemStack apply() {
			if (item != null && meta != null) {
				item.setItemMeta(meta);
			}
			return item;
		}

		@Override
		public ItemStack get() {
			return apply();
		}
	}

	/**
	 * Editor for ItemMeta objects.
	 */
	public static class ItemMetaEditor extends BaseEditor<ItemMeta, ItemMetaEditor> {
		private final ItemMeta meta;

		/**
		 * Construct a new ItemMetaEditor.
		 */
		private ItemMetaEditor(Plugin plugin, ItemMeta meta, PersistentDataContainer pdc) {
			super(plugin, pdc);
			this.meta = meta;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected ItemMetaEditor self() {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ItemMeta get() {
			return meta;
		}
	}

	/**
	 * Editor for PersistentDataContainer objects.
	 */
	public static class PDCEditor extends BaseEditor<PersistentDataContainer, PDCEditor> {
		/**
		 * Construct a new PDCEditor.
		 */
		private PDCEditor(Plugin plugin, PersistentDataContainer pdc) {
			super(plugin, pdc);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected PDCEditor self() {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public PersistentDataContainer get() {
			return pdc;
		}
	}

	/**
	 * Creates an ItemStackEditor for the given ItemStack.
	 *
	 * @param item The ItemStack to edit.
	 * @return A new ItemStackEditor instance.
	 */
	@Contract("null -> new")
	public @NotNull ItemStackEditor of(@Nullable ItemStack item) {
		if (item == null) {
			return new ItemStackEditor(this.plugin, null, null, null);
		}

		ItemMeta meta = item.getItemMeta();
		return new ItemStackEditor(this.plugin, item, meta, meta != null ? meta.getPersistentDataContainer() : null);
	}

	/**
	 * Creates an ItemMetaEditor for the given ItemMeta.
	 *
	 * @param meta The ItemMeta to edit.
	 * @return A new ItemMetaEditor instance.
	 */
	@Contract("_ -> new")
	public @NotNull ItemMetaEditor of(@Nullable ItemMeta meta) {
		if (meta == null) {
			return new ItemMetaEditor(this.plugin, null, null);
		}

		return new ItemMetaEditor(this.plugin, meta, meta.getPersistentDataContainer());
	}

	/**
	 * Creates a PDCEditor for the given PersistentDataContainer.
	 *
	 * @param pdc The PersistentDataContainer to edit.
	 * @return A new PDCEditor instance.
	 */
	@Contract("_ -> new")
	public @NotNull PDCEditor of(@Nullable PersistentDataContainer pdc) {
		return new PDCEditor(this.plugin, pdc);
	}
}
