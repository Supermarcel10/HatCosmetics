package me.Tonus_.hatCosmetics.config.mapper;

import me.Tonus_.hatCosmetics.config.structures.StorageFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StorageFormatMapper implements TypeMapper<StorageFormat> {
    @Override
    @Nullable
    public StorageFormat map(@NotNull String value) {
        try {
            return StorageFormat.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
