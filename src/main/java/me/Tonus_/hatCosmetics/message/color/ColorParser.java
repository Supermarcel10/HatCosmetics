package me.Tonus_.hatCosmetics.message.color;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public class ColorParser implements IColorParser {
    /**
     * Formats a message for color by replacing all '&' with '§'
     * @param msg Message to format
     * @return Formatted message
     */
    @Contract(pure = true)
    public @NotNull String parse(@NotNull String msg) {
        return msg.replaceAll("&([0-9a-fA-FKkLlMmNnOoRr])", "§$1");
    }
}
