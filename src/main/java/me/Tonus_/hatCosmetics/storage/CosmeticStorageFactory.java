package me.Tonus_.hatCosmetics.storage;

import lombok.RequiredArgsConstructor;
import me.Tonus_.hatCosmetics.config.ConfigReference;
import me.Tonus_.hatCosmetics.config.IConfigRetriever;
import me.Tonus_.hatCosmetics.config.structures.StorageFormat;


@RequiredArgsConstructor
public class CosmeticStorageFactory {
    private static final StorageFormat DEFAULT_STORAGE_FORMAT = StorageFormat.YML;

    private final IConfigRetriever configRetriever;
    private final YmlCosmeticStorage ymlStorage;

    public ICosmeticStorage createFromConfig() {
        var format = configRetriever.getValue(ConfigReference.STORAGE_FORMAT, DEFAULT_STORAGE_FORMAT);

        return switch (format) {
            case YML -> ymlStorage;
        };
    }
}
