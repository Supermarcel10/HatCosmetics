package me.Tonus_.hatCosmetics.config.mapper;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;


public class MaterialMapper implements TypeMapper<Material> {
    @Override
    public Material map(@NotNull String value) {
        return Material.getMaterial(value);
    }
}
