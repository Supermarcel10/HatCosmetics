package me.Tonus_.hatCosmetics.versionedAPICalls;

import me.Tonus_.hatCosmetics.Main;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.InvocationTargetException;

public class CustomModelData {
    public static ItemStack appendModelData(ItemStack baseItem, int modelData) {
        try {
            return newMethod(baseItem, modelData);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException | NoSuchFieldException e) {
            Main.getInstance().getLogger().severe(e.toString());
            return oldMethod(baseItem, modelData);
        } catch (Exception e) {
            return oldMethod(baseItem, modelData);
        }
    }

    /**
     * Item Meta pathway for Minecraft 1.16 - 1.21.3
     * @param baseItem
     * @param modelData
     * @return Updated ItemStack
     */
    private static @NotNull ItemStack oldMethod(@NotNull ItemStack baseItem, int modelData) {
        var meta = baseItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(modelData);
            baseItem.setItemMeta(meta);
        }

        return baseItem;
    }

    /**
     * Custom Model Data pathway for Minecraft 1.21.4+
     * @param baseItem
     * @param modelData
     * @return Updated ItemStack
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws NoSuchFieldException
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static @NotNull ItemStack newMethod(
            ItemStack baseItem,
            int modelData
    ) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.html
        var customModelDataClass = Class.forName("io.papermc.paper.datacomponent.item.CustomModelData");

        // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.html#customModelData()
        var builderMethod = customModelDataClass.getMethod("customModelData");
        var builder = builderMethod.invoke(null);

        // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/item/CustomModelData.Builder.html#addString(java.lang.String)
        var addStringMethod = builder.getClass().getMethod("addString", String.class);
        addStringMethod.setAccessible(true);
        addStringMethod.invoke(builder, String.valueOf(modelData));

        // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/DataComponentBuilder.html#build()
        var buildMethod = builder.getClass().getMethod("build");
        buildMethod.setAccessible(true);
        var customData = buildMethod.invoke(builder);

        // https://jd.papermc.io/paper/1.21.4/io/papermc/paper/datacomponent/DataComponentTypes.html
        var dataComponentTypesClass = Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
        var customModelDataComponent = dataComponentTypesClass.getField("CUSTOM_MODEL_DATA").get(null);

        // https://jd.papermc.io/paper/1.21.4/org/bukkit/inventory/ItemStack.html#setData(io.papermc.paper.datacomponent.DataComponentType.Valued,io.papermc.paper.datacomponent.DataComponentBuilder)
        var setDataMethod = ItemStack.class.getMethod("setData",
                Class.forName("io.papermc.paper.datacomponent.DataComponentType$Valued"),
                Object.class
        );
        setDataMethod.invoke(baseItem, customModelDataComponent, customData);

        return baseItem;
    }
}
