package me.Tonus_.hatCosmetics.message.color;

import org.jetbrains.annotations.NotNull;


public interface IColorParser {
    /**
     * Formats a message for color by replacing all '&' with '§'
     * @param msg Message to format
     * @return Formatted message
     */
    @NotNull String parse(@NotNull String msg);
}
