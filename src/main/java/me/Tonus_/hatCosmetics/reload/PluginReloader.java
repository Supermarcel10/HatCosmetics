package me.Tonus_.hatCosmetics.reload;

import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.inventory.IInventoryManager;
import me.Tonus_.hatCosmetics.message.IMessageRetriever;
import me.Tonus_.hatCosmetics.message.MessageReference;
import me.Tonus_.hatCosmetics.message.translations.ITranslationRetriever;
import me.Tonus_.hatCosmetics.storage.ICosmeticStorage;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class PluginReloader implements IPluginReloader {
    private final IInventoryManager inventoryManager;
    private final IConfigRetriever configRetriever;
    private final ITranslationRetriever translationRetriever;
    private final ICosmeticStorage cosmeticStorage;
    private final @NotNull Runnable versionCheck;
    private final IMessageRetriever messageRetriever;

    @Override
    public void reload(@NotNull final CommandSender sender) {
        inventoryManager.closeAllInventories();
        configRetriever.reload();
        translationRetriever.reload();
        cosmeticStorage.reload();
        versionCheck.run();

        messageRetriever.sendMessage(sender, MessageReference.RELOAD_SUCCESS);
    }
}
