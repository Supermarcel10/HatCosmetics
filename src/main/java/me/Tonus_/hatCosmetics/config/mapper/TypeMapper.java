package me.Tonus_.hatCosmetics.config.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface TypeMapper<T> {
    @Nullable T map(@NotNull String value);
}
