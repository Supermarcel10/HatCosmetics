package me.Tonus_.hatCosmetics.versionedAPICalls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Handles applying custom model data to ItemStacks across different Minecraft versions.
 * <p>
 * This class provides version-agnostic functionality for setting custom model data on items.
 * For versions 1.16-1.21.3, it uses the traditional ItemMeta approach. For version 1.21.4+,
 * it uses the new Data Components API via reflection. The appropriate method is determined
 * at initialization time.
 * </p>
 */
public class CustomModelData {

    private final Logger logger;
    private final ReflectionCache reflectionCache;
    private final BiFunction<ItemStack, String, ItemStack> applyFunction;

    public CustomModelData(@NotNull Logger logger) {
        this.logger = logger;
        this.reflectionCache = initializeReflectionCache();

        if (this.reflectionCache.isValid()) {
            this.applyFunction = this::applyNewMethod;
            logger.fine(
                "Using Data Components API (1.21.4+) for custom model data"
            );
        } else {
            this.applyFunction = this::applyOldMethod;
            logger.warning(
                "Failed to initialize Data Components API reflection. Falling back to ItemMeta API."
            );
        }
    }

    /**
     * Applies custom model data to the given ItemStack.
     *
     * @param baseItem The ItemStack to modify
     * @param modelData The custom model data value to apply
     * @return A new ItemStack with the custom model data applied, or the original if an error occurs
     * @throws NullPointerException if baseItem is null
     */
    @NotNull
    public ItemStack appendModelData(
        @NotNull ItemStack baseItem,
        @NotNull String modelData
    ) {
        try {
            return applyFunction.apply(baseItem.clone(), modelData);
        } catch (Exception e) {
            logger.log(
                Level.SEVERE,
                "Failed to apply custom model data: " + e.getMessage(),
                e
            );
            return baseItem;
        }
    }

    /**
     * Traditional ItemMeta pathway for Minecraft 1.16 - 1.21.3.
     *
     * @param baseItem  The ItemStack to modify
     * @param modelData The custom model data value to apply
     * @return The modified ItemStack
     */
    @NotNull
    private ItemStack applyOldMethod(
        @NotNull ItemStack baseItem,
        @NotNull String modelData
    ) {
        int parsedModelData;
        try {
            parsedModelData = Integer.parseInt(modelData);
        } catch (NumberFormatException e) {
            logger.log(
                Level.WARNING,
                "Non-numeric custom model data \"{0}\" is not supported on this version. Skipping.",
                modelData
            );

            return baseItem;
        }

        var meta = baseItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(parsedModelData);
            baseItem.setItemMeta(meta);
        }
        return baseItem;
    }

    /**
     * Data Components API pathway for Minecraft 1.21.4+.
     *
     * @param baseItem  The ItemStack to modify
     * @param modelData The custom model data value to apply
     * @return The modified ItemStack
     */
    @NotNull
    private ItemStack applyNewMethod(
        @NotNull ItemStack baseItem,
        @NotNull String modelData
    ) {
        try {
            // Create builder instance
            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.html#customModelData()
            var builder = reflectionCache.builderMethod.invoke(null);

            // Add model data as string
            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.Builder.html#addString(java.lang.String)
            reflectionCache.addStringMethod.invoke(builder, modelData);

            // Build the component
            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/DataComponentBuilder.html#build()
            var customData = reflectionCache.buildMethod.invoke(builder);

            // Apply to ItemStack
            // https://jd.papermc.io/paper/1.21.4/org/bukkit/inventory/ItemStack.html#setData(io.papermc.paper.datacomponent.DataComponentType.Valued,io.papermc.paper.datacomponent.DataComponentBuilder)
            reflectionCache.setDataMethod.invoke(
                baseItem,
                reflectionCache.customModelDataComponent,
                customData
            );

            return baseItem;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(
                "Failed to apply custom model data via Data Components API",
                e
            );
        }
    }

    /**
     * Initializes and caches reflection objects for the Data Components API.
     *
     * @return A ReflectionCache object, which may be invalid if reflection fails
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    @NotNull
    private ReflectionCache initializeReflectionCache() {
        try {
            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.html
            var customModelDataClass = Class.forName(
                "io.papermc.paper.datacomponent.item.CustomModelData"
            );

            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.html#customModelData()
            var builderMethod = customModelDataClass.getMethod(
                "customModelData"
            );

            // Get builder and its methods
            var builder = builderMethod.invoke(null);
            var builderClass = builder.getClass();

            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.Builder.html#addString(java.lang.String)
            var addStringMethod = builderClass.getMethod(
                "addString",
                String.class
            );
            addStringMethod.setAccessible(true);

            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/DataComponentBuilder.html#build()
            var buildMethod = builderClass.getMethod("build");
            buildMethod.setAccessible(true);

            // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/DataComponentTypes.html
            var dataComponentTypesClass = Class.forName(
                "io.papermc.paper.datacomponent.DataComponentTypes"
            );
            var customModelDataComponentField =
                dataComponentTypesClass.getField("CUSTOM_MODEL_DATA");
            var customModelDataComponent = customModelDataComponentField.get(
                null
            );

            // https://jd.papermc.io/paper/1.21.4/org/bukkit/inventory/ItemStack.html#setData(io.papermc.paper.datacomponent.DataComponentType.Valued,io.papermc.paper.datacomponent.DataComponentBuilder)
            var valuedTypeClass = Class.forName(
                "io.papermc.paper.datacomponent.DataComponentType$Valued"
            );
            var setDataMethod = ItemStack.class.getMethod(
                "setData",
                valuedTypeClass,
                Object.class
            );

            return new ReflectionCache(
                builderMethod,
                addStringMethod,
                buildMethod,
                customModelDataComponent,
                setDataMethod
            );
        } catch (
            ClassNotFoundException
            | NoSuchMethodException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchFieldException e
        ) {
            logger.log(
                Level.WARNING,
                "Failed to initialize Data Components API reflection",
                e
            );
            return ReflectionCache.INVALID;
        }
    }

    /**
     * Flyweight pattern cache for reflection objects to avoid repeated lookups.
     */
    private static class ReflectionCache {

        static final ReflectionCache INVALID = new ReflectionCache();

        final Method builderMethod;
        final Method addStringMethod;
        final Method buildMethod;
        final Object customModelDataComponent;
        final Method setDataMethod;

        private final boolean valid;

        private ReflectionCache() {
            this.builderMethod = null;
            this.addStringMethod = null;
            this.buildMethod = null;
            this.customModelDataComponent = null;
            this.setDataMethod = null;
            this.valid = false;
        }

        ReflectionCache(
            @NotNull Method builderMethod,
            @NotNull Method addStringMethod,
            @NotNull Method buildMethod,
            @NotNull Object customModelDataComponent,
            @NotNull Method setDataMethod
        ) {
            this.builderMethod = builderMethod;
            this.addStringMethod = addStringMethod;
            this.buildMethod = buildMethod;
            this.customModelDataComponent = customModelDataComponent;
            this.setDataMethod = setDataMethod;
            this.valid = true;
        }

        boolean isValid() {
            return valid;
        }
    }
}
