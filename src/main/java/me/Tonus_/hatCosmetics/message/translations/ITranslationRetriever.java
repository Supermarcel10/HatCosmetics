package me.Tonus_.hatCosmetics.message.translations;

import me.Tonus_.hatCosmetics.message.MessageReference;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;


public interface ITranslationRetriever {
    @Nullable String tryGetTranslation(Locale locale, MessageReference messageReference);
}
