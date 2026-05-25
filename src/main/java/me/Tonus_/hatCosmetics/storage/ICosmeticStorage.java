package me.Tonus_.hatCosmetics.storage;

import java.util.List;
import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;


public interface ICosmeticStorage {
    List<Cosmetic> loadAll();
    void reload();
}
