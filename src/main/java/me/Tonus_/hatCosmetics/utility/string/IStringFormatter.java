package me.Tonus_.hatCosmetics.utility.string;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public interface IStringFormatter {
    @NotNull String format(@NotNull String format, @Nullable String formatArg);
    @NotNull String format(@NotNull String format, @NotNull Map<String, String> formatArgs);
}
