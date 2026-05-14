package me.Tonus_.hatCosmetics.permissions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PermissionNode {
    private static final String BASE = "hatcosmetics";
    private static final String HAT_PREFIX = BASE + ".hat.";

    public static final String RELOAD = BASE + ".reload";
    public static final String HAT_WILDCARD = HAT_PREFIX + "*";

    public static String forHat(String name) {
        return HAT_PREFIX + name;
    }
}
