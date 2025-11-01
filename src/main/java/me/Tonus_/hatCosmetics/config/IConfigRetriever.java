package me.Tonus_.hatCosmetics.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IConfigRetriever {
    <T> @Nullable T getValue(@NotNull ConfigReference configReference);
    <T> @Nullable T getValue(@NotNull ConfigReference configReference, T defaultValue);
}
