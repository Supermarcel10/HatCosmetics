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

    public static MessageReference GUI_TITLE = cr("gui_title");
    public static MessageReference GUI_CLOSE = cr("gui_close");
    public static MessageReference GUI_NEXT = cr("gui_next");
    public static MessageReference GUI_PREV = cr("gui_prev");

    public static MessageReference HAT_UNEQUIP = cr("hat_unequip");
    public static MessageReference HAT_UNEQUIP_OVERLAYED = cr("hat_unequip_overlayed");
    public static MessageReference HAT_UNEQUIP_SUCCESS = cr("hat_unequip_success");
    public static MessageReference HAT_UNEQUIP_SUCCESS_OTHER = cr("hat_unequip_success_other");
    public static MessageReference HAT_UNEQUIP_SUCCESS_FORCE = cr("hat_unequip_success_force");

    public static MessageReference NO_PERMISSION = cr("no_permission");
    public static MessageReference NOT_ONLINE = cr("not_online");

    public static MessageReference HAT_EQUIP = cr("hat_equip");

    public static MessageReference HAT_NOT_EXIST = cr("hat_not_exist");
    public static MessageReference HAT_SUCCESS = cr("hat_success");
    public static MessageReference HAT_SUCCESS_OTHER = cr("hat_success_other");
    public static MessageReference HAT_SUCCESS_FORCE = cr("hat_success_force");
    public static MessageReference NO_HAT = cr("no_hat");
    public static MessageReference NO_HAT_GIVEN = cr("no_hat_given");
    public static MessageReference NO_HAT_OTHER = cr("no_hat_other");
    public static MessageReference NO_HAT_PERMISSION = cr("no_hat_permission");
    public static MessageReference NO_HAT_PERMISSION_OTHER = cr("no_hat_permission_other");
    public static MessageReference NO_HATS_GUI = cr("no_hats_gui");

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

    final String yamlPath;
}