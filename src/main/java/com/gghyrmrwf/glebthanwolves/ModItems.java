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

    public static final RegistryObject<Item> PLANT_FIBER = ITEMS.register("plant_fiber",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PLANT_CORDAGE = ITEMS.register("plant_cordage",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WOOD_CHUNK = ITEMS.register("wood_chunk",
            () -> new Item(new Item.Properties()));

    /* Phase 2.5 — крафт-материал-осколок. 2 шт → 1 cobblestone (shapeless, без верстака).
     * Дропается из minecraft:stone вместо cobblestone через датапак-оверрайд лут-таблицы. */
    public static final RegistryObject<Item> COBBLESTONE_FRAGMENT = ITEMS.register("cobblestone_fragment",
            () -> new Item(new Item.Properties()));

    /* Phase 2.6 — крафт-материал-осколок железа. 4 шт → 1 raw_iron.
     * Дропается из iron_ore и deepslate_iron_ore. Fortune действует как на ваниль raw_iron. */
    public static final RegistryObject<Item> IRON_FRAGMENT = ITEMS.register("iron_fragment",
            () -> new Item(new Item.Properties()));

    /* Phase 2.7 — крафт-материал-осколок золота. 4 шт → 1 raw_gold.
     * Дропается из gold_ore, deepslate_gold_ore (1 шт), nether_gold_ore (2-6 шт). */
    public static final RegistryObject<Item> GOLD_FRAGMENT = ITEMS.register("gold_fragment",
            () -> new Item(new Item.Properties()));

    /* Phase 2.8 — крафт-материал-осколок алмаза. 4 шт → 1 diamond.
     * Дропается из diamond_ore и deepslate_diamond_ore. Fortune действует как на ваниль. */
    public static final RegistryObject<Item> DIAMOND_FRAGMENT = ITEMS.register("diamond_fragment",
            () -> new Item(new Item.Properties()));

    /* Phase 2.9 — крафт-материал-осколок меди. 4 шт → 1 raw_copper.
     * Дропается из copper_ore и deepslate_copper_ore (2-5 шт). */
    public static final RegistryObject<Item> COPPER_FRAGMENT = ITEMS.register("copper_fragment",
            () -> new Item(new Item.Properties()));

    /* Phase 2.10 — крафт-материал-осколок кварца. 4 шт → 1 nether_quartz.
     * Дропается из nether_quartz_ore. */
    public static final RegistryObject<Item> QUARTZ_FRAGMENT = ITEMS.register("quartz_fragment",
            () -> new Item(new Item.Properties()));

    /* Phase 2.11 — крафт-материал-осколок угля. 2 шт → 1 coal (другое соотношение!).
     * Дропается из coal_ore и deepslate_coal_ore. */
    public static final RegistryObject<Item> COAL_FRAGMENT = ITEMS.register("coal_fragment",
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
