package me.Tonus_.hatCosmetics.cosmetic;

import java.util.List;
import org.bukkit.Material;


public record Cosmetic(
    String name,
    String displayName,
    Material material,
    String customModelData,
    List<String> description
) {}
