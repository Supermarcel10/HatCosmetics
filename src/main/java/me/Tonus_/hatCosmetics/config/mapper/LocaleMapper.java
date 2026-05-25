package me.Tonus_.hatCosmetics.config.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;


public class LocaleMapper implements TypeMapper<Locale> {
    @Override
    @Nullable
    public Locale map(@NotNull String value) {
        var split = value.split("_");
        if (split.length < 2) {
            return null;
        }

        var language = split[0].trim();
        var country = split[1].trim();

        if (language.isEmpty() || country.isEmpty()) {
            return null;
        }

        return new Locale(language, country);
    }
}
