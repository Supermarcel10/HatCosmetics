package me.Tonus_.hatCosmetics.cosmetic;

import java.util.List;
import org.bukkit.Material;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CosmeticLoader implements ICosmeticLoader {
    public List<Cosmetic> load() {
        // TODO: Update to implement proper loading
        return List.of(
            new Cosmetic(
                "staffHat",
                "&fStaff Hat",
                Material.FEATHER,
                "1000101",
                List.of("&7A fancy staff hat")
            ),
            new Cosmetic(
                "disguise",
                "&aDisguise",
                Material.FEATHER,
                "1000102",
                List.of("&7Glasses and a moustache")
            )
        );
    }

    @Override
    public void reload() {
        // TODO: Reload cosmetics from config when file-based loading is implemented
    }
}
