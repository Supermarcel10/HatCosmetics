package me.Tonus_.hatCosmetics.message.translations;

import org.jetbrains.annotations.Nullable;


public interface ITranslationRetriever {
    @Nullable String tryGetTranslation(String language, String path);
}
