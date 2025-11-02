package me.Tonus_.hatCosmetics.message.generics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface IGenericsRetriever {
    @Nullable String getGeneric(@NotNull String key);
}
