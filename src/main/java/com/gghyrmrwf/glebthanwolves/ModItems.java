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

    // --- Phase 2.3b shard items (one per block category) ---

    public static final RegistryObject<Item> DIRT_CHUNK = ITEMS.register("dirt_chunk",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SAND_PILE = ITEMS.register("sand_pile",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SNOW_CHUNK = ITEMS.register("snow_chunk",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_FRAGMENT = ITEMS.register("stone_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DEEPSTONE_FRAGMENT = ITEMS.register("deepstone_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_FRAGMENT = ITEMS.register("iron_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLD_FRAGMENT = ITEMS.register("gold_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> EMERALD_FRAGMENT = ITEMS.register("emerald_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_FRAGMENT = ITEMS.register("diamond_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> QUARTZ_FRAGMENT = ITEMS.register("quartz_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ANCIENT_FRAGMENT = ITEMS.register("ancient_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> NETHER_FRAGMENT = ITEMS.register("nether_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BRICK_FRAGMENT = ITEMS.register("brick_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HARD_BRICK_FRAGMENT = ITEMS.register("hard_brick_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GLASS_SHARD = ITEMS.register("glass_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CERAMIC_PIECE = ITEMS.register("ceramic_piece",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CONCRETE_DUST = ITEMS.register("concrete_dust",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COMPRESSED_METAL_FRAGMENT = ITEMS.register("compressed_metal_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PRECIOUS_FRAGMENT = ITEMS.register("precious_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MACHINE_SCRAP = ITEMS.register("machine_scrap",
            () -> new Item(new Item.Properties()));

    // --- Tools ---

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
