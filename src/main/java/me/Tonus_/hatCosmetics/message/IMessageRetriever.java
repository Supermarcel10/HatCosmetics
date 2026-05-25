package me.Tonus_.hatCosmetics.message;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.Map;


public interface IMessageRetriever {
    /**
     * Gets a message in the language of the server
     * @param messageReference Path to the message
     * @return String message
     */
    @NotNull String getMessage(MessageReference messageReference);

    /**
     * Gets a message in the language of the player
     * @param sender Player to get the message for
     * @param messageReference Path to the message
     * @return String message
     */
    @NotNull String getMessage(@NotNull CommandSender sender, MessageReference messageReference);

    void sendMessage(@NotNull CommandSender sender, @NotNull MessageReference messageReference);
    void sendMessage(@NotNull CommandSender sender, @NotNull MessageReference messageReference, String formatArg);
    void sendMessage(@NotNull CommandSender sender, @NotNull MessageReference messageReference, Map<String, String> formatArgs);
}
