package me.Tonus_.hatCosmetics.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MessageReference {
    public static MessageReference VERSION = cr("version");


    // GUI
    public static MessageReference GUI_TITLE = cr("gui.title");
    public static MessageReference GUI_CLOSE = cr("gui.close");
    public static MessageReference GUI_NEXT = cr("gui.next");
    public static MessageReference GUI_PREV = cr("gui.prev");

    public static MessageReference COSMETIC_INVENTORY_EQUIP = cr("gui.cosmetic_inv_equip");
    public static MessageReference COSMETIC_INVENTORY_UNEQUIP = cr("gui.cosmetic_inv_unequip");
    public static MessageReference COSMETIC_INVENTORY_UNEQUIP_OVERLAYED = cr("gui.cosmetic_inv_unequip_overlayed");

    public static MessageReference COSMETIC_NO_PERMISSION_SHORT = cr("gui.cosmetic_no_permission");
    public static MessageReference COSMETIC_LIST_EMPTY = cr("gui.cosmetic_list_empty");


    // COMMAND
    public static MessageReference COSMETIC_EQUIP_SUCCESS = cr("command.cosmetic_equip_success");
    public static MessageReference COSMETIC_EQUIP_SUCCESS_INVOKER = cr("command.cosmetic_equip_success_other_invoker");
    public static MessageReference COSMETIC_EQUIP_SUCCESS_TARGET = cr("command.cosmetic_equip_success_other_target");
    public static MessageReference COSMETIC_EQUIP_FAIL = cr("command.cosmetic_equip_fail");
    public static MessageReference COSMETIC_EQUIP_FAIL_OTHER = cr("command.cosmetic_equip_fail_other");

    public static MessageReference COSMETIC_UNEQUIP_SUCCESS = cr("command.cosmetic_unequip_success");
    public static MessageReference COSMETIC_UNEQUIP_FAIL = cr("command.cosmetic_unequip_fail");
    public static MessageReference COSMETIC_UNEQUIP_SUCCESS_INVOKER = cr("command.cosmetic_unequip_success_other_invoker");
    public static MessageReference COSMETIC_UNEQUIP_SUCCESS_TARGET = cr("command.cosmetic_unequip_success_other_target");
    public static MessageReference COSMETIC_UNEQUIP_OTHER_FAIL = cr("command.cosmetic_unequip_fail_other");

    public static MessageReference COSMETIC_NOT_FOUND = cr("command.cosmetic_not_found");
    public static MessageReference COSMETIC_NO_PERMISSION_LONG = cr("command.cosmetic_no_permission");
    public static MessageReference COSMETIC_ARG_NOT_GIVEN = cr("command.cosmetic_arg_not_given");


    // MISC
    public static MessageReference PLAYER_NOT_ONLINE = cr("player_not_online");

    public static MessageReference RELOAD_SUCCESS = cr("reload_success");


    @Contract("_ -> new")
    static @NotNull MessageReference cr(@NotNull String yamlPath) {
        return new MessageReference(yamlPath);
    }

    @TestOnly
    @Contract("_ -> new")
    public static @NotNull MessageReference createReference(@NotNull String yamlPath) {
        return new MessageReference(yamlPath);
    }

    private final String yamlPath;
}
