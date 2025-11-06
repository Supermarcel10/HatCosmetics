package me.Tonus_.hatCosmetics.message.translations;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;


public interface ITranslationRetriever {
    @Nullable FileConfiguration tryGetTranslation(String language);
}
