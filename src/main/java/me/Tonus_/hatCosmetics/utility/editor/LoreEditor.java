package me.Tonus_.hatCosmetics.utility.editor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.message.MessageRetriever;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
public class LoreEditor {
	private final MessageRetriever messageRetriever;

	/**
	 * Abstract base class for Lore editors.
	 *
	 * @param <T> The type of object being edited.
	 * @param <E> The type of the editor itself (for method chaining).
	 */
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static abstract class BaseLoreEditor<T, E extends BaseLoreEditor<T, E>> {
		private final MessageRetriever messageRetriever;
		protected final List<Component> lore;

		/**
		 * Returns the current instance of the editor.
		 *
		 * @return The current editor instance.
		 */
		protected abstract E self();

		/**
		 * Adds a lore line to the list.
		 *
		 * @param loreLine The Component to add as a lore line.
		 * @return The current editor instance.
		 */
		@Contract("_ -> this")
		public E addLore(Component loreLine) {
			if (lore != null) {
				lore.add(loreLine);
			}
			return self();
		}

		/**
		 * Adds a lore line from a message path.
		 *
		 * @param player The player to get the localized message for.
		 * @param messageReference The message path.
		 * @return The current editor instance.
		 */
		@Contract("_, _ -> this")
		public E addLoreMessage(@NotNull Player player, MessageReference messageReference) {
			return addLore(Component.text(messageRetriever.getMessage(player, messageReference)));
		}

		/**
		 * Sets the entire lore list.
		 *
		 * @param newLore The new lore list.
		 * @return The current editor instance.
		 */
		@Contract("_ -> this")
		public E setLore(List<Component> newLore) {
			if (lore != null) {
				lore.clear();
				lore.addAll(newLore);
			}
			return self();
		}

		/**
		 * Clears the lore list.
		 *
		 * @return The current editor instance.
		 */
		@Contract("-> this")
		public E clearLore() {
			if (lore != null) {
				lore.clear();
			}
			return self();
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
	public static class ItemStackLoreEditor extends BaseLoreEditor<ItemStack, ItemStackLoreEditor> {
		private final ItemStack item;
		private final ItemMeta meta;

		/**
		 * Construct a new ItemStackLoreEditor.
		 */
		private ItemStackLoreEditor(MessageRetriever messageRetriever, ItemStack item, ItemMeta meta, List<Component> lore) {
			super(messageRetriever, lore);
			this.item = item;
			this.meta = meta;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected ItemStackLoreEditor self() {
			return this;
		}

		/**
		 * Applies the changes to the ItemStack.
		 *
		 * @return The modified ItemStack.
		 */
		public ItemStack apply() {
			if (item != null && meta != null) {
				meta.lore(lore);
				item.setItemMeta(meta);
			}
			return item;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ItemStack get() {
			return apply();
		}
	}

	/**
	 * Editor for ItemMeta objects.
	 */
	public static class ItemMetaLoreEditor extends BaseLoreEditor<ItemMeta, ItemMetaLoreEditor> {
		private final ItemMeta meta;

		/**
		 * Construct a new ItemMetaLoreEditor.
		 */
		private ItemMetaLoreEditor(MessageRetriever messageRetriever, ItemMeta meta, List<Component> lore) {
			super(messageRetriever, lore);
			this.meta = meta;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected ItemMetaLoreEditor self() {
			return this;
		}

		/**
		 * Applies the changes to the ItemMeta.
		 *
		 * @return The modified ItemMeta.
		 */
		public ItemMeta apply() {
			if (meta != null) {
				meta.lore(lore);
			}
			return meta;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ItemMeta get() {
			return apply();
		}
	}

	/**
	 * Creates an ItemStackLoreEditor for the given ItemStack.
	 *
	 * @param item The ItemStack to edit.
	 * @return A new ItemStackLoreEditor instance.
	 */
	@Contract("null -> new")
	public @NotNull ItemStackLoreEditor of(@Nullable ItemStack item) {
		if (item == null) {
			return new ItemStackLoreEditor(messageRetriever, null, null, null);
		}

		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta != null ? meta.lore() : null;
		if (lore == null) {
			lore = new ArrayList<>();
		}

		return new ItemStackLoreEditor(messageRetriever, item, meta, lore);
	}

	/**
	 * Creates an ItemMetaLoreEditor for the given ItemMeta.
	 *
	 * @param meta The ItemMeta to edit.
	 * @return A new ItemMetaLoreEditor instance.
	 */
	@Contract("null -> new")
	public @NotNull ItemMetaLoreEditor of(@Nullable ItemMeta meta) {
		if (meta == null) {
			return new ItemMetaLoreEditor(messageRetriever, null, null);
		}

		List<Component> lore = meta.lore();
		if (lore == null) {
			lore = new ArrayList<>();
		}

		return new ItemMetaLoreEditor(messageRetriever, meta, lore);
	}
}
