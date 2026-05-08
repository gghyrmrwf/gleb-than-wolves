package com.gghyrmrwf.glebthanwolves;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GlebThanWolves.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + GlebThanWolves.MODID))
                    .icon(() -> new ItemStack(ModItems.PRIMITIVE_AXE.get()))
                    .displayItems((params, output) -> {
                        // Phase 2.3b shards
                        output.accept(ModItems.ORGANIC_FIBER.get());
                        output.accept(ModItems.DIRT_CHUNK.get());
                        output.accept(ModItems.SAND_PILE.get());
                        output.accept(ModItems.SNOW_CHUNK.get());
                        output.accept(ModItems.WOOD_CHIP.get());
                        output.accept(ModItems.STONE_FRAGMENT.get());
                        output.accept(ModItems.DEEPSTONE_FRAGMENT.get());
                        output.accept(ModItems.NETHER_FRAGMENT.get());
                        output.accept(ModItems.IRON_FRAGMENT.get());
                        output.accept(ModItems.GOLD_FRAGMENT.get());
                        output.accept(ModItems.EMERALD_FRAGMENT.get());
                        output.accept(ModItems.DIAMOND_FRAGMENT.get());
                        output.accept(ModItems.QUARTZ_FRAGMENT.get());
                        output.accept(ModItems.ANCIENT_FRAGMENT.get());
                        output.accept(ModItems.BRICK_FRAGMENT.get());
                        output.accept(ModItems.HARD_BRICK_FRAGMENT.get());
                        output.accept(ModItems.GLASS_SHARD.get());
                        output.accept(ModItems.CERAMIC_PIECE.get());
                        output.accept(ModItems.CONCRETE_DUST.get());
                        output.accept(ModItems.COMPRESSED_METAL_FRAGMENT.get());
                        output.accept(ModItems.PRECIOUS_FRAGMENT.get());
                        output.accept(ModItems.MACHINE_SCRAP.get());
                        // Phase 1.1 craft intermediate
                        output.accept(ModItems.PLANT_CORDAGE.get());
                        // Tools
                        output.accept(ModItems.PRIMITIVE_AXE.get());
                        output.accept(ModItems.WOODEN_PICKAXE.get());
                        output.accept(ModItems.WOODEN_AXE.get());
                        output.accept(ModItems.WOODEN_SWORD.get());
                        output.accept(ModItems.WOODEN_SHOVEL.get());
                        output.accept(ModItems.WOODEN_HOE.get());
                        output.accept(ModItems.STONE_PICKAXE.get());
                        output.accept(ModItems.STONE_AXE.get());
                        output.accept(ModItems.STONE_SWORD.get());
                        output.accept(ModItems.STONE_SHOVEL.get());
                        output.accept(ModItems.STONE_HOE.get());
                        output.accept(ModItems.IRON_PICKAXE.get());
                        output.accept(ModItems.IRON_AXE.get());
                        output.accept(ModItems.IRON_SWORD.get());
                        output.accept(ModItems.IRON_SHOVEL.get());
                        output.accept(ModItems.IRON_HOE.get());
                        output.accept(ModItems.DIAMOND_PICKAXE.get());
                        output.accept(ModItems.DIAMOND_AXE.get());
                        output.accept(ModItems.DIAMOND_SWORD.get());
                        output.accept(ModItems.DIAMOND_SHOVEL.get());
                        output.accept(ModItems.DIAMOND_HOE.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
