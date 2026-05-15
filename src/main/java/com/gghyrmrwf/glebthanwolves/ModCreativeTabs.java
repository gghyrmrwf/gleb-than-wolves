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
                        output.accept(ModItems.PLANT_FIBER.get());
                        output.accept(ModItems.PLANT_CORDAGE.get());
                        output.accept(ModItems.WOOD_CHUNK.get());
                        output.accept(ModItems.COBBLESTONE_FRAGMENT.get());
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
