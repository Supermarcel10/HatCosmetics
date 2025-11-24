package me.Tonus_.hatCosmetics.message.translations;

import me.Tonus_.hatCosmetics.message.MessageReference;
import org.jetbrains.annotations.Nullable;


public interface ITranslationRetriever {
    @Nullable String tryGetTranslation(String language, MessageReference messageReference);
}
