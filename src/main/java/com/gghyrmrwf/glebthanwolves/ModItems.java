package com.gghyrmrwf.glebthanwolves;

import com.gghyrmrwf.glebthanwolves.items.GtwTiers;
import com.gghyrmrwf.glebthanwolves.items.PrimitiveAxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GlebThanWolves.MODID);

    public static final RegistryObject<Item> ORGANIC_FIBER = ITEMS.register("organic_fiber",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PLANT_CORDAGE = ITEMS.register("plant_cordage",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WOOD_CHIP = ITEMS.register("wood_chip",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PRIMITIVE_AXE = ITEMS.register("primitive_axe",
            () -> new PrimitiveAxeItem(PrimitiveAxeItem.PRIMITIVE_TIER, 5.0f, -3.2f,
                    new Item.Properties().durability(8)));

    public static final RegistryObject<Item> STONE_PICKAXE = ITEMS.register("stone_pickaxe",
            () -> new PickaxeItem(GtwTiers.GTW_STONE, 1, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> STONE_AXE = ITEMS.register("stone_axe",
            () -> new AxeItem(GtwTiers.GTW_STONE, 7.0f, -3.2f, new Item.Properties()));

    public static final RegistryObject<Item> STONE_SWORD = ITEMS.register("stone_sword",
            () -> new SwordItem(GtwTiers.GTW_STONE, 3, -2.4f, new Item.Properties()));

    public static final RegistryObject<Item> STONE_SHOVEL = ITEMS.register("stone_shovel",
            () -> new ShovelItem(GtwTiers.GTW_STONE, 1.5f, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> STONE_HOE = ITEMS.register("stone_hoe",
            () -> new HoeItem(GtwTiers.GTW_STONE, -1, -2.0f, new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_PICKAXE = ITEMS.register("wooden_pickaxe",
            () -> new PickaxeItem(GtwTiers.GTW_WOODEN, 1, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_AXE = ITEMS.register("wooden_axe",
            () -> new AxeItem(GtwTiers.GTW_WOODEN, 6.0f, -3.2f, new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_SWORD = ITEMS.register("wooden_sword",
            () -> new SwordItem(GtwTiers.GTW_WOODEN, 3, -2.4f, new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_SHOVEL = ITEMS.register("wooden_shovel",
            () -> new ShovelItem(GtwTiers.GTW_WOODEN, 1.5f, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_HOE = ITEMS.register("wooden_hoe",
            () -> new HoeItem(GtwTiers.GTW_WOODEN, 0, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> IRON_PICKAXE = ITEMS.register("iron_pickaxe",
            () -> new PickaxeItem(GtwTiers.GTW_IRON, 1, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> IRON_AXE = ITEMS.register("iron_axe",
            () -> new AxeItem(GtwTiers.GTW_IRON, 6.0f, -3.1f, new Item.Properties()));

    public static final RegistryObject<Item> IRON_SWORD = ITEMS.register("iron_sword",
            () -> new SwordItem(GtwTiers.GTW_IRON, 3, -2.4f, new Item.Properties()));

    public static final RegistryObject<Item> IRON_SHOVEL = ITEMS.register("iron_shovel",
            () -> new ShovelItem(GtwTiers.GTW_IRON, 1.5f, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> IRON_HOE = ITEMS.register("iron_hoe",
            () -> new HoeItem(GtwTiers.GTW_IRON, -2, -1.0f, new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_PICKAXE = ITEMS.register("diamond_pickaxe",
            () -> new PickaxeItem(GtwTiers.GTW_DIAMOND, 1, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_AXE = ITEMS.register("diamond_axe",
            () -> new AxeItem(GtwTiers.GTW_DIAMOND, 5.0f, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_SWORD = ITEMS.register("diamond_sword",
            () -> new SwordItem(GtwTiers.GTW_DIAMOND, 3, -2.4f, new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_SHOVEL = ITEMS.register("diamond_shovel",
            () -> new ShovelItem(GtwTiers.GTW_DIAMOND, 1.5f, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_HOE = ITEMS.register("diamond_hoe",
            () -> new HoeItem(GtwTiers.GTW_DIAMOND, -3, 0.0f, new Item.Properties()));

    private ModItems() {}
}
