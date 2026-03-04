package com.ItzChilletIgnis.horror.true_creative_mode.item;

import com.ItzChilletIgnis.horror.true_creative_mode.True_creative_mode;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item REMAINS = registerItem("remains", new Item(new Item.Settings()));
    public static final Item ASHES = registerItem("ashes", new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of("true_creative_mode", name), item);
    }

    public static void registerModItems() {
        // 触发类加载
    }
}