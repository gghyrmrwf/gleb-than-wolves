package com.gghyrmrwf.glebthanwolves;

import com.gghyrmrwf.glebthanwolves.items.PrimitiveAxeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GlebThanWolves.MODID);

    public static final RegistryObject<Item> PLANT_FIBER = ITEMS.register("plant_fiber",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PLANT_CORDAGE = ITEMS.register("plant_cordage",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PRIMITIVE_AXE = ITEMS.register("primitive_axe",
            () -> new PrimitiveAxeItem(PrimitiveAxeItem.PRIMITIVE_TIER, 5.0f, -3.2f,
                    new Item.Properties().durability(30)));

    private ModItems() {}
}
