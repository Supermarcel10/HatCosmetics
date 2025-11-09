package me.Tonus_.hatCosmetics.message;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.Map;


public interface IMessageRetriever {
    /**
     * Gets a message in the language of the server
     * @param path Path to the message
     * @return String message
     */
    @NotNull String getMessage(String path);

    /**
     * Gets a message in the language of the player
     * @param sender Player to get the message for
     * @param path Path to the message
     * @return String message
     */
    @NotNull String getMessage(@NotNull CommandSender sender, String path);

    void sendMessage(@NotNull CommandSender sender, @NotNull String path);
    void sendMessage(@NotNull CommandSender sender, @NotNull String path, String formatArg);
    void sendMessage(@NotNull CommandSender sender, @NotNull String path, Map<String, String> formatArgs);
}
