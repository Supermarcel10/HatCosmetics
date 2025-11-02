package me.Tonus_.hatCosmetics.message;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


@AllArgsConstructor
public class ColorParser implements IColorParser {
    /**
     * Formats a message for color by replacing all '&' with '§'
     * @param msg Message to format
     * @return Formatted message
     */
    @Contract(pure = true)
    public @NotNull String parse(@NotNull String msg) {
        return msg.replaceAll("&([1-9a-eA-EKkLlMmNnOoRr])", "§$1");
    }
}
